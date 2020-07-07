package com.intgroup.htmlcheck.feature.taskcheck.remoteserver;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/remoteCheckServers")
public class RemoteCheckServerApiController {
    public static final String API_TOKEN = "f5346hyebwvegrhntmji7564uye543546795";

    @Autowired
    private RemoteCheckServers remoteCheckServers;

    @GetMapping("/invalidateAllTasks")
    public Object invalidateAllTasks(@RequestParam(name = "apiToken") String apiToken,
            @RequestParam(name = "tasksPrefix") String tasksPrefix) {

        if (!isApiTokenValid(apiToken)) {
            return RequestResult.fail("Invalid API token");
        }

        remoteCheckServers.invalidateCache(tasksPrefix);

        return RequestResult.success("Invalidate cache for tasks with prefix " + tasksPrefix + " started");
    }

    private boolean isApiTokenValid(String apiToken) {
        return apiToken != null && !apiToken.isBlank() && apiToken.equals(API_TOKEN);
    }
}
