package com.intgroup.htmlcheck.feature.block;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskBlockHashNamingService {
    private List<Character> chars;

    @PostConstruct
    public void init() {
        initCharacterMap();
    }

    private void initCharacterMap() {
        chars = new ArrayList<>();

        for(char c = 'a'; c <= 'z'; c++) {
            chars.add(c);
        }
        for(int i = 0; i <= 9; i++) {
            chars.add(Integer.toString(i).charAt(0));
        }

        //Pseudo shuffle
        for(int i = 0; i < 100; i++) {
            int p1 = i % chars.size();

            int p2 = (i + 100) % chars.size();

            char tmp = chars.get(p1);
            chars.set(p1, chars.get(p2));
            chars.set(p2, tmp);
        }
    }

    public String getUniqueBlockHash(int number) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            int charPosition = (number + i) % chars.size();
            result.append(chars.get(charPosition));
        }
        result.append(number);

        return result.toString();
    }
}
