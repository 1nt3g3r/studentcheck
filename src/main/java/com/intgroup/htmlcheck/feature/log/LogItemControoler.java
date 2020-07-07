package com.intgroup.htmlcheck.feature.log;

import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/log")
public class LogItemControoler {
    @Autowired
    private UserService userService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private LogItemService logItemService;

    @Autowired
    private PaginationService paginationService;

    @GetMapping
    public ModelAndView showLeelooSubscribesList(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage,
                                                 @RequestParam(name = "query", required = false, defaultValue = "") String query) {
        ModelAndView result = new ModelAndView("admin/log/list");

        result.addObject("user", userService.getUser());

        List<Specification<LogItem>> specs = new ArrayList<>();

        specs.add(LogItemSpecifications.any());

        if (!query.isBlank()) {
            result.addObject("query", query);
            specs.add(LogItemSpecifications.tagOrValueLike("%" + query.toLowerCase() + "%"));
        }

        Specification<LogItem> logItemSpec = LogItemSpecifications.and(specs);

        int recordCount = logItemService.count(logItemSpec);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        List<LogItem> logItems = logItemService.queryPage(currentPage, logItemSpec);
        result.addObject("logItems", logItems);

        return result;
    }
}
