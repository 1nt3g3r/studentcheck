package com.intgroup.htmlcheck.feature.mq.enums;

public enum Event {
    newUserSubscribed("Подписался новый пользователь", 0),
    dailyNotifyHour("Ежедневное напоминание", 1),
    userWrote("Пользователь написал сообщение", 2),
    userPressInlineButton("Пользователь нажал на кнопку", 3),
    scheduledTime("Относительный день и время", 4),
    concreteDate("Конкретная дата и время", 5);

    private String description;
    private int sortOrder;

    Event(String description, int sortOrder) {
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
