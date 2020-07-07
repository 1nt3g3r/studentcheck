package com.intgroup.htmlcheck.feature.taskcheck.stat;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AggregatedTaskRequestStatData {
    private long totalTime;
    private int totalRequestCount;

    private Map<String, Integer> prefixRequestCount = new LinkedHashMap<>();

    public int getPrefixRequestCount(String prefix) {
        return prefixRequestCount.getOrDefault(prefix, 0);
    }

    public float getRequestPerTime(int secondCount) {
        float result = getRPS() * (float) secondCount;
        return round(result, 1);
    }

    public float getRPS() {
        if (totalRequestCount == 0) {
            return 0;
        }

        if (totalTime == 0) {
            return 0;
        }

        float result = (float) totalRequestCount / ((float) (totalTime) / 1000f);
        return round(result, 1);
    }

    private float round(float value, int digitCount) {
        float multiplier = (float) Math.pow(10, digitCount);

        int multiplied = (int) (value * multiplier);
        return (float) multiplied / multiplier;
    }
}
