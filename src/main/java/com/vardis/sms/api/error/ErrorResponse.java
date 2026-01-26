package com.vardis.sms.api.error;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    public Instant timestamp = Instant.now();
    public int status;
    public String error;
    public String message;
    public String path;
    public List<FieldError> details;

    public static class FieldError {
        public String field;
        public String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
