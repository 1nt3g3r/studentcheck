package com.intgroup.htmlcheck.feature.telegram.service;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.referral.ReferralService;
import com.intgroup.htmlcheck.service.util.NormalizeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class TelegramBotPayloadParseService {
    @Autowired
    private NormalizeDataService normalizeDataService;

    @Autowired
    private ReferralService referralService;

    public Map<String, String> parsePayload(String payload) {
        Map<String, String> result = new LinkedHashMap<>();

        if (payload == null) {
            return result;
        }

        String[] payloadParts = payload.split("__");
        for(String payloadPart: payloadParts) {
            String[] keyValue = payloadPart.split("-");
            if (keyValue.length >= 2) {
                String key = keyValue[0];

                StringJoiner value = new StringJoiner("-");
                for(int i = 1; i < keyValue.length; i++) {
                    value.add(keyValue[i]);
                }

                result.put(key, value.toString());
            }
        }

        return result;
    }

    public void applyPayload(String payload, TelegramUser user) {
        user.setPayload(payload);

        parsePayload(payload).forEach((key, value) -> {
            if (!value.isEmpty()) { //Skip empty values
                switch (key) {
                    case "ED": user.setEventDate(normalizeDate(value)); break; //Event date
                    case "PH": user.setPhone(normalizeDataService.normalizePhone(value)); break; //User phone number
                    case "EM": user.setEmail(value); break; //User email
                    case "FN": user.setFirstName(fromBase64(value)); break; //User first name
                    case "LN": user.setLastName(fromBase64(value)); break; //User last name
                    case "TG": user.setTag(value); break; //User tag
                    case "FROM": referralService.processReferral(Long.parseLong(value), user); break; //referral
                }
            }
        });
    }

    //ED-20200509__TG-MARATHON__PH-093-98-10458__EM-melnichuk.cadet@gmail.com
    private String normalizeDate(String dateString) {
        if (dateString.equals("NOW")) {
            LocalDate now = LocalDate.now();
            return now.format(DateTimeFormatter.ISO_DATE);
        }

        StringBuilder result = new StringBuilder();

        int index = 0;
        for(char c: dateString.toCharArray()) {
            result.append(c);

            index++;
            if (index == 4 || index == 6) {
                result.append('-');
            }
        }

        return result.toString();
    }

    private String fromBase64(String value) {
        return value;
    }
}
