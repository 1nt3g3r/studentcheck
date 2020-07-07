package com.intgroup.htmlcheck.feature.telegram.service;

import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    public boolean isValidMd(String text) {
        if (text == null) {
            return true;
        }
        int starCount = 0;
        for(char c: text.toCharArray()) {
            if (c == '*') {
                starCount++;
            }
        }

        if (starCount%2 != 0) {
            return false;
        }

        return true;
    }
}