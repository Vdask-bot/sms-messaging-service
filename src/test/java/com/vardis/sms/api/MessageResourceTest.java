package com.vardis.sms.api;

import com.vardis.sms.kafka.SmsEventPublisher;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class MessageResourceTest {

    @InjectMock
    SmsEventPublisher publisher;

    @Test
    void post_validMessage_returns201_andCallsPublisher() {
        String body = """
        {
          "sourceNumber": "+35799123456",
          "destinationNumber": "+306912345678",
          "content": "Hello test"
        }
        """;

        Number idNum =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/messages")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("status", equalTo("PENDING"))
                        .extract()
                        .path("id");

        Long id = idNum.longValue();

        verify(publisher, times(1)).publishMessageCreated(id);
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void get_unknownId_returns404_withDescriptiveError() {
        given()
                .when()
                .get("/messages/999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("NOT_FOUND"))
                .body("status", equalTo(404))
                .body("message", containsString("not found"))
                .body("path", containsString("messages/999999"));
    }

    @Test
    void post_invalidMessage_returns400_withValidationDetails() {
        String body = """
    {
      "sourceNumber": "+35799123456",
      "destinationNumber": "123",
      "content": ""
    }
    """;

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/messages")
                .then()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }


}
