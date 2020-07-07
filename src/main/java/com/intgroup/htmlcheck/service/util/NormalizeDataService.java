package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;

@Service
public class NormalizeDataService {
    public String normalizePhone(String phoneNumber) {
        StringBuilder result = new StringBuilder();
        for(char c: phoneNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                result.append(c);
            }
        }

        String finalResult = result.toString();
        if (finalResult.length() == 10) { // phone number starts from 093 - add 38
            finalResult = "38" + finalResult;
        } else if (finalResult.length() == 9) {
            finalResult = "380" + finalResult; //phone number starts from 93 (939810458) - add 380
        }

        return finalResult;
    }
}
