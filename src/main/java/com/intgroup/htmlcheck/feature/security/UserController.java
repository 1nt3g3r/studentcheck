package com.intgroup.htmlcheck.feature.security;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import com.intgroup.htmlcheck.domain.logic.TaskType;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.certificate.SendEmailCertificateService;
import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TaskCheckService taskCheckService;

    @Autowired
    private UserTaskStatService userTaskStatService;

    @Autowired
    private SendEmailCertificateService sendEmailCertificateService;

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public Object login(@RequestParam String email,
                        @RequestParam String password) {
        User user = userService.getUser(email, password);

        if (user == null) {
            return RequestResult.fail("Invalid user params");
        } else {
            Map<String, String> result = new HashMap<>();
            result.put("success", "true");
            try {
                if (user.getToken() == null) {
                    user.setToken(userService.generateToken());
                    userService.saveUser(user);
                }
                result.put("token", user.getToken());
            } catch (Exception e) {
                e.printStackTrace();
                return RequestResult.fail("Error while generation token");
            }
            return result;
        }
    }

    @RequestMapping(value = "/register", method = {RequestMethod.GET, RequestMethod.POST})
    public Object register(@RequestParam(required = false) String email,
                           @RequestParam(required = false) String password) {
        if (email == null || email.isBlank()) {
            return RequestResult.fail("Email not passed");
        }

        if (userService.findUserByEmail(email) != null) {
            return RequestResult.fail("User with this email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setName("");
        user.setLastName("");
        try {
            user.setToken(userService.generateToken());
        } catch (Exception e) {
            return RequestResult.fail("Error generating token");
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        } else {
            user.setPassword(email);
        }

        try {
            userService.saveUser(user);

            Map<String, String> result = new HashMap<>();
            result.put("success", "true");
            try {
                result.put("token", user.getToken());
            } catch (Exception e) {
                e.printStackTrace();
                return RequestResult.fail("Error while generation token");
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestResult.fail(ex.getMessage());
        }
    }

    @GetMapping("/getProgress")
    public Object getProgress(@RequestParam(name = "token", required = false, defaultValue = "") String token) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        UserProgress userProgress = new UserProgress();
        userProgress.setTotalTaskCount(taskCheckService.getTotalTaskCount());
        userProgress.setPassedTaskCount(userTaskStatService.getPassedTasks(user).size());
        userProgress.setCanReceiveCertificate(userProgress.getTotalTaskCount() == userProgress.getPassedTaskCount());

        if (userProgress.getTotalTaskCount() > 0) {
            userProgress.setProgressPercent(100 * userProgress.getPassedTaskCount() / userProgress.getTotalTaskCount());
        }

        return userProgress;
    };

    @RequestMapping(value = "/get", method = {RequestMethod.GET, RequestMethod.POST})
    public Object get(@RequestParam String token) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid user params");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("name", user.getName());
        result.put("lastName", user.getLastName());

        return result;
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.GET, RequestMethod.POST})
    public Object delete(@RequestParam String token) {

        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid user params");
        }

        try {
            String email = user.getEmail();
            userService.deleteUserById(user.getId());
            return RequestResult.success(email);
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestResult.fail(ex.getMessage());
        }
    }

    @RequestMapping(value = "/update", method = {RequestMethod.GET, RequestMethod.POST})
    public Object update(
            @RequestParam String token,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String resolvedTasksId,
            @RequestParam(required = false) String currentTaskId,
            @RequestParam(required = false) TaskType currentTheme,
            @RequestParam(required = false) String isCompleted,
            @RequestParam(required = false) String isReceivedCertificate) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid user params");
        }


        if (name != null) {
            user.setName(name);
        }

        if (lastName != null) {
            user.setLastName(lastName);
        }


        if (isReceivedCertificate != null) {
            //User requested certificate
            if (Boolean.parseBoolean(isReceivedCertificate)) {
                String email = user.getEmail();
                String fullName = user.getName();
                if (user.getLastName() != null) {
                    fullName = fullName + " " + user.getLastName();
                }

                try {
                    sendEmailCertificateService.sendCertificate(email, fullName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            userService.updateUser(user);
            return RequestResult.success(user.getEmail());
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestResult.fail(ex.getMessage());
        }
    }
   
}
