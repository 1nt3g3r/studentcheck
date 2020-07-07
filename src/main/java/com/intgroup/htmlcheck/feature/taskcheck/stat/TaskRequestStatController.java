package com.intgroup.htmlcheck.feature.taskcheck.stat;

import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecord;
import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecordService;
import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecordSpecifications;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/task/requestStat")
public class TaskRequestStatController {
    @Autowired
    private UserService userService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private TaskRequestStatService taskRequestStatService;

    @Autowired
    private TaskRequestStatRecordService taskRequestStatRecordService;

    @GetMapping
    public ModelAndView getTaskRequestStats() {
        ModelAndView result = new ModelAndView("admin/task/request-stat");

        result.addObject("user", userService.getUser());
        result.addObject("stats", taskRequestStatService.getAggregatedStatData());
        seoService.setTitle(result, "Нагрузка по проверке задач");

        result.addObject("aggregateRpsTypes", TaskRequestStatService.AggregateRpsType.values());

        return result;
    }

    @ResponseBody
    @GetMapping("/getRecords")
    public Object getRecords(@RequestParam String from,
                             @RequestParam String to,
                             @RequestParam TaskRequestStatService.AggregateRpsType aggregateRpsType) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime fromDate = LocalDateTime.parse(from + " 00:00:00", formatter);
        LocalDateTime toDate = LocalDateTime.parse(to + " 23:59:59", formatter);

        List<TaskRequestStatRecord> records = taskRequestStatRecordService.queryAll(
                TaskRequestStatRecordSpecifications.and(
                        TaskRequestStatRecordSpecifications.dateGreaterOrEqual(fromDate),
                        TaskRequestStatRecordSpecifications.dateLowerOrEqual(toDate)
                )
        );

        Map<String, Float> data = taskRequestStatService.aggregate(aggregateRpsType, records);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", data.keySet());
        result.put("values", data.values());
        return result;
    }
}
