package com.vardis.sms.api;

import com.vardis.sms.kafka.SmsEventPublisher;
import com.vardis.sms.message.MessageEntity;
import com.vardis.sms.message.MessageStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class MessageResourceTest {

    @InjectMock
    SmsEventPublisher publisher;

    private Long idPending;
    private Long idDelivered;

    @BeforeEach
    @Transactional
    void setup() {
        // reset mock interactions so tests don't affect each other
        reset(publisher);

        MessageEntity.deleteAll();

        MessageEntity m1 = new MessageEntity();
        m1.sourceNumber = "+35799123456";
        m1.destinationNumber = "+306912345678";
        m1.content = "Hello 1";
        m1.status = MessageStatus.PENDING;
        m1.createdAt = Instant.now().minusSeconds(60);
        m1.persist();
        idPending = m1.id;

        MessageEntity m2 = new MessageEntity();
        m2.sourceNumber = "+35799123456";
        m2.destinationNumber = "+306900000000";
        m2.content = "Hello 2";
        m2.status = MessageStatus.DELIVERED;
        m2.createdAt = Instant.now();
        m2.persist();
        idDelivered = m2.id;
    }

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

        verifyNoInteractions(publisher);
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

        verifyNoInteractions(publisher);
    }

    @Test
    void get_existingId_returns200_andMessage() {
        given()
                .when()
                .get("/messages/" + idPending)
                .then()
                .statusCode(200)
                .body("content", equalTo("Hello 1"))
                .body("status", equalTo("PENDING"))
                .body("sourceNumber", equalTo("+35799123456"));

        verifyNoInteractions(publisher);
    }

    @Test
    void list_withoutFilters_returnsMessages() {
        given()
                .when()
                .get("/messages")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));

        verifyNoInteractions(publisher);
    }

    @Test
    void list_withStatusFilter_returnsOnlyMatching() {
        given()
                .queryParam("status", "DELIVERED")
                .when()
                .get("/messages")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].status", equalTo("DELIVERED"))
                .body("[0].content", equalTo("Hello 2"));

        verifyNoInteractions(publisher);
    }

    @Test
    void list_withSourceNumber_normalizesAndFilters() {
        // no '+' and includes whitespace -> normalizePhone() should turn it into +35799123456
        given()
                .queryParam("sourceNumber", "357 99123456")
                .when()
                .get("/messages")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].sourceNumber", equalTo("+35799123456"))
                .body("[1].sourceNumber", equalTo("+35799123456"));

        verifyNoInteractions(publisher);
    }

    @Test
    void list_withDestinationNumber_filtersCorrectly() {
        given()
                .queryParam("destinationNumber", "+306900000000")
                .when()
                .get("/messages")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].content", equalTo("Hello 2"))
                .body("[0].destinationNumber", equalTo("+306900000000"));

        verifyNoInteractions(publisher);
    }
}
