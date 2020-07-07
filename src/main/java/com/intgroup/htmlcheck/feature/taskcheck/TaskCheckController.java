package com.intgroup.htmlcheck.feature.taskcheck;

import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/task/check")
public class TaskCheckController {
    @Autowired
    private TaskCheckService taskService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ModelAndView list() {
        ModelAndView result = new ModelAndView("admin/task/list");

        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Список задач");

        return result;
    }

    @GetMapping
    public ModelAndView get(@RequestParam(name = "taskId") String taskId) {
        ModelAndView result = new ModelAndView("admin/task/check");

        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Проверка задачи");

        result.addObject("taskId", taskId);

        TaskCheckService.TaskData taskData = taskService.getTaskData(taskId);
        result.addObject("taskData", taskData);

        result.addObject("html", taskData.getInitialHtml());
        result.addObject("css", taskData.getInitialCss());
        result.addObject("js", taskData.getInitialCode());

        return result;
    }

    @PostMapping
    public ModelAndView post(@RequestParam(name = "taskId") String taskId,
                             @RequestParam(name = "html", required = false, defaultValue = "") String html,
                             @RequestParam(name = "css", required = false, defaultValue = "") String css,
                             @RequestParam(name = "js", required = false, defaultValue = "") String js) {
        ModelAndView result = new ModelAndView("admin/task/check");

        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Проверка задачи");

        result.addObject("taskId", taskId);
        result.addObject("taskData", taskService.getTaskData(taskId));

        Map<String, Object> params = new HashMap<>();
        params.put("html", html);
        params.put("css", css);
        TaskCheckResult taskCheckResult = taskService.check(taskId, js, params);
        result.addObject("taskCheckResult", taskCheckResult);

        result.addObject("html", html);
        result.addObject("css", css);
        result.addObject("js", js);

        return result;
    }

}
