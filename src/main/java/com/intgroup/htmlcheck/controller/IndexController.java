package com.intgroup.htmlcheck.controller;

import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    @Autowired
    private SeoService seoService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ModelAndView get() {
        ModelAndView result = new ModelAndView("index");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Автопроверка HTML");

        return result;
    }
}
