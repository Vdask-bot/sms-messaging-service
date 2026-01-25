package com.vardis.sms.kafka;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SmsEventPublisherTest {

    @Test
    void publish_sendsToEmitter() {
        @SuppressWarnings("unchecked")
        Emitter<Long> emitter = mock(Emitter.class);

        SmsEventPublisher publisher = new SmsEventPublisher(emitter);
        publisher.publishMessageCreated(10L);

        verify(emitter, times(1)).send(10L);
    }
}
