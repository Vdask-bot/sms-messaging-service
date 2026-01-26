package com.vardis.sms.api.error;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ResteasyReactiveViolationException ex) {
        ErrorResponse err = new ErrorResponse();
        err.status = 400;
        err.error = "VALIDATION_ERROR";
        err.message = "Request validation failed";
        err.path = uriInfo.getPath();

        err.details = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldError(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
    }
}
