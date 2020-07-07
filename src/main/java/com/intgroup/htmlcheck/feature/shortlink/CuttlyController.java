package com.intgroup.htmlcheck.feature.shortlink;

import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/cuttly")
public class CuttlyController {
    @Autowired
    private UserService userService;

    @Autowired
    private CuttlyService cuttlyService;

    @Autowired
    private GlobalPrefService globalPrefService;

    @Autowired
    private SeoService seoService;

    @GetMapping("/settings")
    public ModelAndView getSettings() {
        ModelAndView result = new ModelAndView("admin/cuttly/settings");

        seoService.setTitle(result, "Настройки Cutt.ly");

        result.addObject("user", userService.getUser());

        result.addObject(CuttlyService.CUTTLY_API_KEY, globalPrefService.getOrDefault(CuttlyService.CUTTLY_API_KEY, ""));

        return result;
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam(name = CuttlyService.CUTTLY_API_KEY) String cuttlyApiKey) {
        globalPrefService.save(CuttlyService.CUTTLY_API_KEY, cuttlyApiKey);
        cuttlyService.loadSettings();
        return "redirect:/admin/cuttly/settings";
    }
}
