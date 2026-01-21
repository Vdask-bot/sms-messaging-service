package com.vardis.sms.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank(message = "sourceNumber is required")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "sourceNumber must be a valid phone number (E.164-like)"
    )
    public String sourceNumber;

    @NotBlank(message = "destinationNumber is required")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "destinationNumber must be a valid phone number (E.164-like)"
    )
    public String destinationNumber;

    @NotBlank(message = "content is required")
    @Size(max = 160, message = "content must be at most 160 characters")
    public String content;
}
