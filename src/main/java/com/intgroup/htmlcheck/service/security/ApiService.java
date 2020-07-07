package com.intgroup.htmlcheck.service.security;

import org.springframework.stereotype.Service;

@Service
public class ApiService {
    private String apiKey = "abcf-fas-3453-fsda3-fds";

    public String getApiKey() {
        return apiKey;
    }

    public boolean isApiKeyOk(String apiKey) {
        return this.apiKey.equals(apiKey);
    }
}
