package com.intgroup.htmlcheck.feature.taskcheck;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.taskcheck.stat.TaskRequestStatService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStat;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v2/task")
public class TaskCheckApiController {
    @Autowired
    private TaskCheckService taskCheckService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTaskStatService userTaskStatService;

    @Autowired
    private DateService dateService;

    @Autowired
    private TaskRequestStatService taskRequestStatService;

    @RequestMapping(value = "/fastCheck", method = {RequestMethod.GET, RequestMethod.POST})
    public Object fastCheck(@RequestParam(required = false, defaultValue = "") String token,
            @RequestParam(required = false, defaultValue = "") String taskId,
            @RequestParam(required = false, defaultValue = "") String html,
            @RequestParam(required = false, defaultValue = "") String css,
            @RequestParam(required = false, defaultValue = "") String code) {

        if (!userService.isUserWithTokenPresent(token)) {
            return RequestResult.fail("Invalid token");
        }

        if (taskCheckService.isInvalidTaskId(taskId)) {
            return RequestResult.fail("Task with id " + taskId + " not present");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("html", html);
            params.put("css", css);
            return taskCheckService.check(taskId, code, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestResult.fail(ex.getMessage());
        }
    }

    @RequestMapping(value = "/check", method = {RequestMethod.GET, RequestMethod.POST})
    public Object check(@RequestParam(required = false, defaultValue = "") String token,
                        @RequestParam(required = false, defaultValue = "") String taskId,
                        @RequestParam(required = false, defaultValue = "") String html,
                        @RequestParam(required = false, defaultValue = "") String css,
                        @RequestParam(required = false, defaultValue = "") String code,
                        @RequestParam(required = false, defaultValue = "1") int solveTimeSeconds) {
        if (userService.findUserByToken(token) == null) {
            return RequestResult.fail("Invalid token");
        }

        if (taskCheckService.isInvalidTaskId(taskId)) {
            return RequestResult.fail("Task with id " + taskId + " not present");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("html", html);
            params.put("css", css);
            TaskCheckResult taskCheckResult = taskCheckService.check(taskId, code, params);
            User user = userService.findUserByToken(token);

            //Gather stats
            UserTaskStat taskStat = userTaskStatService.getOrCreate(user.getEmail(), taskId);
            if (!taskStat.isSolved()) { //Gather info only if task not solved
                taskStat.setSolveTryCount(taskStat.getSolveTryCount() + 1);

                if (taskCheckResult.isTaskSuccessfullyPassed()) {
                    taskStat.setSolved(true);

                    //Fix solve date and time
                    taskStat.setSolveDateTime(dateService.getCurrentDateTime());

                    //Save total time to solve
                    if (solveTimeSeconds <= 0) { //Fix if frontend sends us invalid solve time in seconds
                        solveTimeSeconds = 5;
                    }
                    taskStat.setSolveTimeSeconds(solveTimeSeconds);
                }

                userTaskStatService.save(taskStat);
            }

            return taskCheckResult;
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestResult.fail(ex.getMessage());
        }
    }

    @GetMapping("/getTaskData")
    public Object getTaskData(@RequestParam(required = false, defaultValue = "") String token,
                              @RequestParam(required = false, defaultValue = "") String taskId ) {
        //Gather stats
        taskRequestStatService.addSample(taskId, "", null);

        User user = userService.findUserByToken(token);
        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        user.setLastActionTime(LocalDateTime.now());
        userService.updateUser(user);

        if (taskCheckService.isInvalidTaskId(taskId)) {
            return RequestResult.fail("Task with id " + taskId + " not present");
        }

        return taskCheckService.getUserSpecificTaskData(taskId, user);
    }

    @GetMapping("/getTaskDataList")
    public Object getTaskDataList(@RequestParam(required = false, defaultValue = "") String token,
                              @RequestParam(required = false, defaultValue = "") String filter) {
        if (userService.findUserByToken(token) == null) {
            return RequestResult.fail("Invalid token");
        }

        return taskCheckService.getTaskDataList(filter);
    }

    @GetMapping("/getPassedTasks")
    public Object getPassedTasks(@RequestParam(required = false, defaultValue = "") String token) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        return userTaskStatService.getPassedTasks(user.getEmail());
    }
}
