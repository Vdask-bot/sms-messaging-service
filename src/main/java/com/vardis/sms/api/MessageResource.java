package com.vardis.sms.api;

import com.vardis.sms.message.MessageEntity;
import com.vardis.sms.message.MessageStatus;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class MessageResource {

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

        return Response
                .status(Response.Status.CREATED)
                .entity(message)
                .build();
    }

}