package com.intgroup.htmlcheck.feature.doc;

import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/doc")
public class DocController {
    @Autowired
    private DocService docService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ModelAndView get(@RequestParam(name = "docName") String docName) {
        ModelAndView result = new ModelAndView("admin/doc/doc.html");

        result.addObject("user", userService.getUser());

        result.addObject("content", docService.getDoc(docName));

        return result;
    }
}
