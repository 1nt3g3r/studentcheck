package com.intgroup.htmlcheck.domain.logic;

public enum TaskType {
    html("HTML"),
    css("CSS"),
    js("JavaScript"),
    react("React");

    TaskType(String description) {
        this.description = description;
    }

    private String description;

    @Override
    public String toString() {
        return description;
    }
}
