package com.intgroup.htmlcheck.controller.admin;

import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    @Autowired
    private SeoService seoService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ModelAndView dashboard() {
        ModelAndView result = new ModelAndView("admin/dashboard");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Панель администратора");

        return result;
    }
}
