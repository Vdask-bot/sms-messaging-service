package com.vardis.sms.api.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception ex) {
        ErrorResponse err = new ErrorResponse();
        err.status = 500;
        err.error = "INTERNAL_ERROR";
        err.message = "Unexpected server error";
        err.path = uriInfo.getPath();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
    }
}
