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
    private Map<Integer, String> legacyHashes;

    @PostConstruct
    public void init() {
        initCharacterMap();
        fillLegacyHashes();
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

    private void fillLegacyHashes() {
        legacyHashes = new LinkedHashMap<>();
        legacyHashes.put(0, "645hg4y7");
        legacyHashes.put(1, "juf31geq");
        legacyHashes.put(2, "74gfhj7l");
        legacyHashes.put(3, "jgfhk855");
        legacyHashes.put(4, "f5368942");
        legacyHashes.put(5, "fsa64rij");
        legacyHashes.put(6, "546jh547");
        legacyHashes.put(7, "54he6yju");
    }

    public String getUniqueBlockHash(int number) {
        if (legacyHashes.containsKey(number)) {
            return legacyHashes.get(number);
        }

        StringBuilder result = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            int charPosition = (number + i) % chars.size();
            result.append(chars.get(charPosition));
        }
        result.append(number);

        return result.toString();
    }
}
