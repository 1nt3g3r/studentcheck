package com.intgroup.htmlcheck.feature.taskcheck;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class TaskCheckResult {
    private boolean checkSuccess = true;
    private String checkMessage = "";

    private List<String> successConditions = new ArrayList<>();
    private List<String> failedConditions = new ArrayList<>();

    private Map<String, Object> metadata = new LinkedHashMap<>();

    public void failed(String condition) {
        removeCondition(condition);
        failedConditions.add(condition);
    }

    public void successIfNotFailedPresent(String success, String ... failedConditions) {
        if (!isAnyFailedConditionPresent(failedConditions)) {
            success(success);
        }
    }

    public void success(String condition) {
        removeCondition(condition);
        successConditions.add(condition);
    }

    public void removeCondition(String condition) {
        failedConditions.remove(condition);
        successConditions.remove(condition);
    }

    public boolean isTaskSuccessfullyPassed() {
        return checkSuccess && failedConditions.isEmpty();
    }

    public boolean isSuccessConditionPresent(String condition) {
        return successConditions.contains(condition);
    }

    public boolean isFailedConditionPresent(String condition) {
        return failedConditions.contains(condition);
    }

    public boolean isAnyFailedConditionPresent(String ... conditions) {
        for(String condition: conditions) {
            if (isFailedConditionPresent(condition)) {
                return true;
            }
        }
        return false;
    }
}
