package com.vardis.sms.kafka;

import com.vardis.sms.message.MessageEntity;
import com.vardis.sms.message.MessageStatus;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class SmsStatusProcessor {

    private static final Logger LOG = Logger.getLogger(SmsStatusProcessor.class);

    @Incoming("sms-in")
    @Blocking
    @Transactional
    public void onMessageCreated(Long messageId) {

        LOG.infof(
                "Received SMS event from Kafka for messageId=%d",
                messageId
        );

        MessageEntity message = MessageEntity.findById(messageId);
        if (message == null) {
            LOG.warnf(
                    "Message with id=%d not found in database. Skipping processing.",
                    messageId
            );
            return;
        }

        try {
            int delay = ThreadLocalRandom.current().nextInt(500, 1501);
            LOG.debugf(
                    "Simulating SMS delivery delay of %d ms for messageId=%d",
                    delay,
                    messageId
            );
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.errorf(
                    "SMS delivery simulation interrupted for messageId=%d",
                    messageId
            );
            return;
        }

        boolean delivered = ThreadLocalRandom.current().nextInt(100) < 85;

        if (delivered) {
            message.status = MessageStatus.DELIVERED;
            message.failureReason = null;
        } else {
            message.status = MessageStatus.FAILED;
            message.failureReason = randomFailureReason();
        }

        LOG.infof(
                "Message id=%d updated in database with status=%s%s",
                messageId,
                message.status,
                message.failureReason != null
                        ? " (reason=" + message.failureReason + ")"
                        : ""
        );
    }

    private String randomFailureReason() {
        String[] reasons = {
                "NETWORK_TIMEOUT",
                "DESTINATION_UNREACHABLE",
                "PROVIDER_ERROR"
        };
        return reasons[
                ThreadLocalRandom.current().nextInt(reasons.length)
                ];
    }
}

