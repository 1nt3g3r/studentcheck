package com.intgroup.htmlcheck.feature.mq.enums;

public enum KeyboardType {
    inline("Прикрепленная к сообщению"),
    bottom("Под строкой ввода текста")
    ;

    KeyboardType(String description) {
        this.description = description;
    }
    private String description;

    public String getDescription() {
        return description;
    }
}
