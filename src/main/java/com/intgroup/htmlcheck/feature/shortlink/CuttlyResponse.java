package com.intgroup.htmlcheck.feature.shortlink;

import java.util.Map;

public class CuttlyResponse {
    private Map<String, String> url;

    public boolean isOk() {
        return url.getOrDefault("status", "0").equals("7");
    }

    public String getShortlink() {
        return url.get("shortLink");
    }
}
