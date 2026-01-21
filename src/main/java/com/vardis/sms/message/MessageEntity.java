package com.vardis.sms.message;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;


@Entity
public class MessageEntity extends PanacheEntity{

    @Column(nullable = false)
    public String sourceNumber;

    @Column(nullable = false)
    public String destinationNumber;

    @Column(nullable = false, length = 160)
    public String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MessageStatus status;

    @Column(nullable = false)
    public Instant createdAt;

}