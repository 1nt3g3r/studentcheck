package com.intgroup.htmlcheck.feature.taskcheck.remoteserver;

import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/remotecheckservers")
public class RemoteCheckServersController {
    @Autowired
    private RemoteCheckServers remoteCheckServers;

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalPrefService globalPrefService;

    @GetMapping("/settings")
    public ModelAndView getSettings() {
        ModelAndView result = new ModelAndView("admin/remotecheckservers/settings");
        result.addObject("user", userService.getUser());

        result.addObject("loadCacheQueueSize", remoteCheckServers.getLoadCacheQueueSize());

        result.addObject(RemoteCheckServers.JS_SERVERS, globalPrefService.getOrDefault(RemoteCheckServers.JS_SERVERS, ""));
        result.addObject(RemoteCheckServers.JAVA_SERVERS, globalPrefService.getOrDefault(RemoteCheckServers.JAVA_SERVERS, ""));
        result.addObject(RemoteCheckServers.PYTHON_SERVERS, globalPrefService.getOrDefault(RemoteCheckServers.PYTHON_SERVERS, ""));
        result.addObject(RemoteCheckServers.HTML_CSS_SERVERS, globalPrefService.getOrDefault(RemoteCheckServers.HTML_CSS_SERVERS, ""));

        return result;
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam(name = RemoteCheckServers.JS_SERVERS, defaultValue = "", required = false) String jsServers,
                               @RequestParam(name = RemoteCheckServers.JAVA_SERVERS, defaultValue = "", required = false) String javaServers,
                               @RequestParam(name = RemoteCheckServers.PYTHON_SERVERS, defaultValue = "", required = false) String pythonServers,
                               @RequestParam(name = RemoteCheckServers.HTML_CSS_SERVERS, defaultValue = "", required = false) String htmlCssServers) {

        globalPrefService.save(RemoteCheckServers.HTML_CSS_SERVERS, htmlCssServers);
        globalPrefService.save(RemoteCheckServers.JS_SERVERS, jsServers);
        globalPrefService.save(RemoteCheckServers.JAVA_SERVERS, javaServers);
        globalPrefService.save(RemoteCheckServers.PYTHON_SERVERS, pythonServers);

        remoteCheckServers.setupAllRemoteServers();

        return "redirect:/admin/remotecheckservers/settings";
    }

    @GetMapping("/invalidateCache")
    public String invalidateCache(String prefix) {
        remoteCheckServers.invalidateCache(prefix);

        return "redirect:/admin/remotecheckservers/settings";
    }
}
