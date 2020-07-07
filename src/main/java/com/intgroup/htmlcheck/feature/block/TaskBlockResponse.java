package com.intgroup.htmlcheck.feature.block;

import lombok.Data;

import java.util.List;

@Data
public class TaskBlockResponse {
    private boolean success = true;
    private int blockIndex;
    private String currentTask;
    private int currentTaskIndex;
    private List<String> blockTasks;
    private List<String> passedTasks;
    private boolean userInMarathon;
    private int marathonDay;
}
