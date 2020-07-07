package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.mq.enums.KeyboardType;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class EventDrivenMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column
    private int priority = 0;

    @Column
    @Enumerated(EnumType.STRING)
    private Event event = Event.scheduledTime;

    @Column(length = 10)
    private String concreteDate;

    @Column
    private int day = 0;

    @Column
    private int hour = 19;

    @Column
    private int minute = 0;

    @Column(length = 100)
    private String title = "";

    @Column(length = 10000)
    private String text = "";

    @Column(length = 100)
    private String tag;

    @Column
    private String userWroteMessagePattern;

    @Column(length = 600)
    private String inlineKeyboard;

    @Column(length = 100)
    private String userButtonPayload;

    @Column
    @Enumerated(EnumType.STRING)
    private KeyboardType keyboardType = KeyboardType.inline;

    public String getHumanReadableDescription() {
        String hourString = Integer.toString(hour);
        if (hour < 10) {
            hourString = "0" + hourString;
        }

        String minuteString = Integer.toString(minute);
        if (minute < 10) {
            minuteString = "0" + minuteString;
        }

        switch (event) {
            case scheduledTime:
                return "+ день " + day + " в " + hourString + ":" + minuteString;
            case newUserSubscribed:
                return "После подписки нового пользователя";
            case userWrote:
                return "Пользователь написал [" + userWroteMessagePattern + "]";
            case userPressInlineButton:
                return "Пользователь нажал на кнопку [" + userButtonPayload + "]";
            case dailyNotifyHour:
                return "Ежедневное напоминание пользователю";
            case concreteDate:
                return concreteDate + " в "  + hourString + ":" + minuteString;
        }

        return event.toString();
    }

    public String getExcerpt() {
        if (text == null) {
            return "";
        }

        if (text.length() < 100) {
            return text;
        }

        return text.substring(0, 97) + "...";
    }
}
