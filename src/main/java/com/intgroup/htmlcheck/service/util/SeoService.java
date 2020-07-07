package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class SeoService {
    public void setTitle(ModelAndView modelAndView, String title) {
        modelAndView.addObject("seoTitle", title);
    }
}
