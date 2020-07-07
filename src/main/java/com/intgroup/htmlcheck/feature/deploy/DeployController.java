package com.intgroup.htmlcheck.feature.deploy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/deploy")
public class DeployController {
    private static final String DEPLOY_KEY = "fsad65ejhgvraeQqfrtbgh";

    @GetMapping
    public String deploy(@RequestParam(name = "key") String key) {
        if (key.equals(DEPLOY_KEY)) {

            try {
                Runtime.getRuntime().exec("/bin/bash /root/git/htmlcheck/launch-deploy.sh");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Restarting, wait...";
        } else {
            return "Invalid key";
        }
    }
}
