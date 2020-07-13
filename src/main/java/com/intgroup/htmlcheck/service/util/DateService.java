package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

@Service
public class DateService {
    public LocalDate parseFromHtml(String text) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(text, formatter);
    }

    public String toString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public LocalDateTime getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int second = calendar.get(Calendar.SECOND);

        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }
    
    public LocalDateTime parseFromDateString(String dateString) {
        String[] parts = dateString.split("-");
        
        return LocalDateTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 0, 0);
    }
}
