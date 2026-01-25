package com.vardis.sms.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class SmsEventPublisher {

    private final Emitter<Long> smsOut;

    @Inject
    public SmsEventPublisher(@Channel("sms-out") Emitter<Long> smsOut) {
        this.smsOut = smsOut;
    }

    public void publishMessageCreated(Long messageId) {
        smsOut.send(messageId);
    }
}

