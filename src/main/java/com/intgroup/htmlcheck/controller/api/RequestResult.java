package com.intgroup.htmlcheck.controller.api;

import lombok.Data;

@Data
public class RequestResult {
    private boolean success;
    private String message;

    public static RequestResult success() {
        return success("");
    }

    public static RequestResult success(String message) {
        RequestResult result = new RequestResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static RequestResult fail() {
        return fail("");
    }

    public static RequestResult fail(String message) {
        RequestResult result = new RequestResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static RequestResult failBadApiKey() {
        return fail("Invalid API key");
    }
}
