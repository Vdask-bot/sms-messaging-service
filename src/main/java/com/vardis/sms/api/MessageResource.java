package com.vardis.sms.api;

import com.vardis.sms.kafka.SmsEventPublisher;
import com.vardis.sms.message.MessageEntity;
import com.vardis.sms.message.MessageStatus;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.NotFoundException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Messages", description = "Endpoints for sending and retrieving SMS messages")
public class MessageResource {

    @Inject
    SmsEventPublisher publisher;

    @POST
    @Transactional
    @Operation(
            summary = "Send a new message",
            description = "Creates a message with status PENDING and publishes an event for asynchronous processing."
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Message created"),
            @APIResponse(responseCode = "400", description = "Validation error")
    })
    public Response sendMessage(@Valid SendMessageRequest request) {

        MessageEntity message = new MessageEntity();
        message.sourceNumber = request.sourceNumber;
        message.destinationNumber = request.destinationNumber;
        message.content = request.content;
        message.status = MessageStatus.PENDING;
        message.createdAt = Instant.now();

        message.persist();
        publisher.publishMessageCreated(message.id);

        return Response
                .status(Response.Status.CREATED)
                .entity(message)
                .build();
    }

    @GET
    @Operation(
            summary = "List messages",
            description = "Returns messages filtered by optional query params: sourceNumber, destinationNumber, and status. Results are ordered by createdAt descending."
    )
    @APIResponse(responseCode = "200", description = "List of messages")
    public List<MessageEntity> listMessages(
            @QueryParam("sourceNumber") String sourceNumber,
            @QueryParam("destinationNumber") String destinationNumber,
            @QueryParam("status") MessageStatus status
    ) {

        // Base query
        var query = "1=1";
        var params = new io.quarkus.panache.common.Parameters();

        if (sourceNumber != null) {
            sourceNumber = normalizePhone(sourceNumber);
            query += " AND sourceNumber = :sourceNumber";
            params.and("sourceNumber", sourceNumber);
        }

        if (destinationNumber != null) {
            destinationNumber = normalizePhone(destinationNumber);
            query += " AND destinationNumber = :destinationNumber";
            params.and("destinationNumber", destinationNumber);
        }

        if (status != null) {
            query += " AND status = :status";
            params.and("status", status);
        }

        return MessageEntity.find(query, Sort.by("createdAt").descending(), params)
                .list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get message by id", description = "Returns a single message by its database id.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Message found"),
            @APIResponse(responseCode = "404", description = "Message not found")
    })
    public Response getMessageById(@PathParam("id") Long id) {

        Optional<MessageEntity> message = MessageEntity.findByIdOptional(id);

        if (message.isEmpty()) {
            throw new NotFoundException("Message with id=" + id + " not found");
        }

        return Response.ok(message.get()).build();
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", "");

        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }

        return normalized;
    }
}
