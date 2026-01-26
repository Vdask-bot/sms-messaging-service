package com.vardis.sms.api.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException ex) {
        int status = ex.getResponse().getStatus();

        ErrorResponse err = new ErrorResponse();
        err.status = status;
        err.error = (status == 404) ? "NOT_FOUND" : "REQUEST_ERROR";
        err.message = ex.getMessage() != null ? ex.getMessage() : "Request failed";
        err.path = uriInfo.getPath();

        return Response.status(status).entity(err).build();
    }
}
