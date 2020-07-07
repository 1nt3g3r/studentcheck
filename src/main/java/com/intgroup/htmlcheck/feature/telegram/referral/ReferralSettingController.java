package com.intgroup.htmlcheck.feature.telegram.referral;

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
@RequestMapping("/admin/tguser/referral-setting")
public class ReferralSettingController {
    @Autowired
    private UserService userService;

    @Autowired
    private ReferralService referralService;
    
    @Autowired
    private SeoService seoService;

    @Autowired
    private PaginationService paginationService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage) {
        ModelAndView result = new ModelAndView("admin/tguser/referral-setting/list");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Настройки рефералки");

        //Construct specifications
        Specification<ReferralSetting> referralSettingSpecification = ReferralSettingSpecifications.any();

        int recordCount = referralService.count(referralSettingSpecification);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        //Users page
        List<ReferralSetting> referralSettings = new ArrayList<>(referralService.queryPage(currentPage, referralSettingSpecification));

        result.addObject("referralSettings", referralSettings);

        return result;
    }

    @GetMapping("/create-update")
    public ModelAndView getCreateUpdate(@RequestParam(name = "id", required = false, defaultValue = "-1") long id) {
        ModelAndView result = new ModelAndView("admin/tguser/referral-setting/create-update");
        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Настройки рефералки");

        ReferralSetting setting;
        if (id < 0) {
            setting = new ReferralSetting();
            setting.setId(id);
        } else {
            setting = referralService.getById(id);
        }

        result.addObject("setting", setting);

        return result;
    }

    @PostMapping("/create-update")
    public String postCreateUpdate(@RequestParam(required = false, defaultValue = "-1") long id,
                                   @RequestParam String sourceTag,
                                   @RequestParam String targetTag) {
        ReferralSetting setting;
        if (id < 0) {
            setting = new ReferralSetting();
        } else {
            setting = referralService.getById(id);
        }
        
        setting.setSourceTag(sourceTag);
        setting.setTargetTag(targetTag);
        referralService.save(setting);

        return "redirect:/admin/tguser/referral-setting/list";
    }

    @GetMapping("/delete")
    public String delete(long id) {
        referralService.deleteById(id);

        return "redirect:/admin/tguser/referral-setting/list";
    }
}
