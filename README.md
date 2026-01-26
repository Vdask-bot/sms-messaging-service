# SMS Messaging Service

A microservice implemented in **Java (Quarkus)** that simulates an SMS messaging platform.  
The system demonstrates RESTful API design, validation, asynchronous processing, persistence, and testing.

---

## Architecture Overview

The application provides the following functionality:

- **REST endpoints** to:
  - Send SMS messages
  - Retrieve a message by ID
  - **Search and list stored messages**
- **Synchronous validation** of message parameters:
  - Source number
  - Destination number
  - Message content
- **Asynchronous message processing** using Kafka
- **Message delivery simulation** resulting in:
  - `DELIVERED`
  - `FAILED`
- **Persistence** of messages and status updates in PostgreSQL
- **Descriptive error handling** with structured error responses

### Message Flow

1. Client submits an SMS via REST API
2. Input is validated synchronously
3. Message is stored in PostgreSQL with status `PENDING`
4. A message-created event is published to Kafka
5. A Kafka consumer simulates delivery
6. Message status is updated to `DELIVERED` or `FAILED`

---

## Technology Stack

- Java 17
- Quarkus
- Apache Kafka
- PostgreSQL
- Hibernate ORM (Panache)
- Docker & Docker Compose
- JUnit 5 & Mockito
- OpenAPI / Swagger UI

---

## Running the System Locally

### Prerequisites

- Docker
- Docker Compose

### Start all services

From the project root:

```bash
docker compose up --build
````

This starts:

* PostgreSQL
* Zookeeper
* Kafka
* SMS Messaging Service

API base URL:

```
http://localhost:8080
```

Swagger UI:

```
http://localhost:8080/q/swagger-ui
```

---

## API Endpoints

### Send SMS

**POST** `/messages`

```json
{
  "sourceNumber": "+306900000000",
  "destinationNumber": "+306900000001",
  "content": "Hello SMS"
}
```

Response contains the created message with status `PENDING`.

---

### Get Message by ID

**GET** `/messages/{id}`

Returns the stored message or a `404 NOT FOUND` error.

---

### List / Search Messages

**GET** `/messages`

Messages can be **listed and filtered** using optional query parameters:

* `sourceNumber`
* `destinationNumber`
* `status`

Example:

```
GET /messages?sourceNumber=+306900000000&status=DELIVERED
```

Results are ordered by creation time (descending).

---

## Validation & Error Handling

* Input validation is enforced using Bean Validation
* Invalid requests return `400 BAD REQUEST`
* Missing resources return `404 NOT FOUND`
* Errors follow a consistent, descriptive JSON structure

---

## Testing

### Automated Tests

Unit and resource tests are implemented using **JUnit 5** and **Mockito**.

Run tests locally:

```bash
./mvnw test
```

Tests cover:

* REST API behavior
* Validation rules
* Error responses
* Kafka publisher interactions (mocked)

---

### Manual / Integration Testing

End-to-end behavior can be tested by running the system with Docker Compose and using:

* Swagger UI
* Postman / curl

Service logs:

```bash
docker compose logs -f sms-service
```

---

## Stopping the System

Stop services:

```bash
docker compose down
```

Stop services and remove volumes (clean database):

```bash
docker compose down -v
```

---

## Notes

* The service is designed to run in **production mode** inside Docker
* Java version used: **Java 17**
* Kafka connectivity is configured for Docker networking


