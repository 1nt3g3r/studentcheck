package com.intgroup.htmlcheck.feature.mq.settings;

import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/event-driven-message/setting")
public class SettingController {
    @Autowired
    private UserService userService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private PaginationService paginationService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage,
                             @RequestParam(name = "query", required = false) String query) {
        ModelAndView result = new ModelAndView("admin/event-driven-message/setting/list");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Настройки");

        //Construct specifications
        List<Specification<Setting>> specs = new ArrayList<>();

        if (query != null) {
            result.addObject("query", query);
            specs.add(SettingSpecifications.query(query));
        }

        if (specs.isEmpty()) {
            specs.add(SettingSpecifications.any());
        }

        Specification<Setting> userSpec = SettingSpecifications.and(specs);

        int recordCount = settingService.count(userSpec);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        //Users page
        List<Setting> settings = new ArrayList<>(settingService.queryPage(currentPage, userSpec));

        //Sort messages - by tag, by event, by day, by hour, by minute and by priority
        settings.sort((m1, m2) -> {
            int tagCompare = m1.getTag().compareTo(m2.getTag());
            if (tagCompare == 0) {
               return m1.getName().compareTo(m2.getName());
            } else {
                return tagCompare;
            }
        });

        result.addObject("settings", settings);

        return result;
    }

    @GetMapping("/copy")
    public String copy(@RequestParam long id) {
        Setting original = settingService.getById(id);
        if (original != null) {
            Setting copy = new Setting();
            copy.setName("Копия " + original.getName());
            copy.setValue(original.getValue());
            copy.setDate(original.getDate());
            copy.setTag(original.getTag());

            settingService.save(copy);
        }

        return "redirect:/admin/event-driven-message/setting/list";
    }

    @GetMapping("/create-update")
    public ModelAndView getCreateUpdate(@RequestParam(name = "id", required = false, defaultValue = "-1") long id) {
        ModelAndView result = new ModelAndView("admin/event-driven-message/setting/create-update");
        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Опция");

        Setting setting;
        if (id < 0) {
            setting = new Setting();
            setting.setId(id);
        } else {
            setting = settingService.getById(id);
        }

        result.addObject("setting", setting);

        return result;
    }

    @PostMapping("/create-update")
    public String postCreateUpdate(@RequestParam(required = false, defaultValue = "-1") long id,
                                   @RequestParam String name,
                                   @RequestParam String value,
                                   @RequestParam String tag,
                                   @RequestParam String date) {
        Setting setting;
        if (id < 0) {
            setting = new Setting();
        } else {
            setting = settingService.getById(id);
        }
        setting.setName(name);
        setting.setValue(value);
        setting.setTag(tag);
        setting.setDate(date);

        settingService.save(setting);

        return "redirect:/admin/event-driven-message/setting/list";
    }

    @GetMapping("/delete")
    public String delete(long id) {
        settingService.deleteById(id);

        return "redirect:/admin/event-driven-message/setting/list";
    }
}
