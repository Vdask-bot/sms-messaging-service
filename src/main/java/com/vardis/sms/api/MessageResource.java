package com.vardis.sms.api;

import com.vardis.sms.message.MessageEntity;
import com.vardis.sms.message.MessageStatus;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    @Channel("sms-out")
    Emitter<Long> smsOut;

    @POST
    @Transactional
    public Response sendMessage(@Valid SendMessageRequest request) {

        MessageEntity message = new MessageEntity();
        message.sourceNumber = request.sourceNumber;
        message.destinationNumber = request.destinationNumber;
        message.content = request.content;
        message.status = MessageStatus.PENDING;
        message.createdAt = Instant.now();

        message.persist();
        smsOut.send(message.id);

        return Response
                .status(Response.Status.CREATED)
                .entity(message)
                .build();
    }

    @GET
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
    public Response getMessageById(@PathParam("id") Long id) {

        Optional<MessageEntity> message = MessageEntity.findByIdOptional(id);

        if (message.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.ok(message.get()).build();
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }

        // trim και αφαίρεση whitespace
        String normalized = value.trim().replaceAll("\\s+", "");

        // αν ξεκινάει με digit, πρόσθεσε +
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }

        return normalized;
    }
}

