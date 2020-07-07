package com.intgroup.htmlcheck.feature.telegram.domain;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.StringJoiner;

@Data
@Entity
@Table(name = "telegram_user")
public class TelegramUser {
    @Id
    @Column
    private long userId;
    
    @Column
    private long fromUserId;

    @Column(length = 100)
    private String sourceBotName;

    @Column
    private String userName;

    @Column
    private String chatId;

    @Column
    private String languageCode;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private boolean isBot = false;

    @Column
    private LocalDateTime registerDate = LocalDateTime.now();

    //All this info we can gather later
    @Column
    private String role = "";

    @Column
    private String payload;

    @Column(length = 100)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 10)
    private String eventDate;

    @Column(length = 20)
    private String tag;

    @Column
    private int dailyNotifyHour = 19;

    @Column(length = 2000)
    private String metadata;

    public String getFullName() {
        StringJoiner result = new StringJoiner(" ");
        if (firstName != null) {
            result.add(firstName);
        }
        if (lastName != null) {
            result.add(lastName);
        }
        return result.toString();
    }
}
