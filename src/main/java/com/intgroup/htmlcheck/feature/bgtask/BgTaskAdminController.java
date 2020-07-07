package com.intgroup.htmlcheck.feature.bgtask;

import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/bgtask")
public class BgTaskAdminController {
    @Autowired
    private BgTaskService bgTaskService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ModelAndView list(String query) {
        ModelAndView result = new ModelAndView("admin/bgtask/list");

        result.addObject("user", userService.getUser());
        result.addObject("tasks", bgTaskService.getTasks());

        return result;
    }
}
