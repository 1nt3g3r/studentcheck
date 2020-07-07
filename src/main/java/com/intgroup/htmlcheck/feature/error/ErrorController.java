package com.intgroup.htmlcheck.feature.error;

import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/error")
public class ErrorController {
    @Autowired
    private ErrorService errorService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ModelAndView getAll() {
        ModelAndView result = new ModelAndView("admin/error/list");
        result.addObject("user", userService.getUser());
        result.addObject("errors", errorService.getAll());
        return result;
    }

    @GetMapping("/clear")
    public String clear() {
        errorService.clear();
        return "redirect:/admin/error/list";
    }
}
