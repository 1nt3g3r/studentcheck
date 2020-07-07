package com.intgroup.htmlcheck.feature.taskcheck.validate;

import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/task/validate")
public class TaskCheckValidateController {
    @Autowired
    private TaskCheckValidateService validateService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ModelAndView validate(@RequestParam(required = false) String taskId) {
        ModelAndView result = new ModelAndView("admin/validate/validate");

        result.addObject("user", userService.getUser());

        if (taskId != null) {
            result.addObject("validateResult", validateService.validate(taskId));
            result.addObject("taskId", taskId);
        }

        return result;
    }
}
