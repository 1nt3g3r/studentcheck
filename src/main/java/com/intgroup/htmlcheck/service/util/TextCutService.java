package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;

@Service
public class TextCutService {
    public String cut(String line, int maxSize) {
        if (line == null) {
            return "";
        }

        if (line.length() < maxSize) {
            return line;
        }

        return line.substring(0, maxSize - 4) + "...";
    }
}
