package com.intgroup.htmlcheck.feature.telegram.im;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class InstantMessage {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private LocalDateTime time = LocalDateTime.now();

    @Column
    private long telegramUserId;

    @Column
    private boolean seen;

    @Column
    @Enumerated(EnumType.STRING)
    private InstantMessageType messageType = InstantMessageType.fromUser;

    @Column(length = 5000)
    private String message;
}
