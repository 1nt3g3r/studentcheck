package com.intgroup.htmlcheck.feature.telegram.stat;

import com.intgroup.htmlcheck.feature.block.CheckTaskBlockService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.security.UserService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin/tguser/stat")
public class TelegramUserTaskStatController {
    @Autowired
    private UserService userService;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private TelegramUserTaskStatService statService;

    @Autowired
    private CheckTaskBlockService checkTaskBlockService;

    @GetMapping
    public ModelAndView showSelectPage() {
        ModelAndView result = new ModelAndView("admin/tguser/stat/select-page");
        result.addObject("user", userService.getUser());

        result.addObject("userTags", telegramUserService.getUniqueTags());
        result.addObject("userEventDates", telegramUserService.getUniqueEventDates());
        result.addObject("allBlockIds", checkTaskBlockService.getBlockIds());

        return result;
    }

    @GetMapping("/createReport")
    public ModelAndView createReport(@RequestParam(required = false) String[] filterTags,
                                     @RequestParam(required = false) String[] filterDates,
                                     @RequestParam(required = false) int[] filterBlockIds) {
        ModelAndView result = new ModelAndView("admin/tguser/stat/report");
        result.addObject("user", userService.getUser());

        List<String> tags = new ArrayList<>();
        if (filterTags != null) {
            tags.addAll(Arrays.asList(filterTags));
        }

        List<String> dates = new ArrayList<>();
        if (filterDates != null) {
            dates.addAll(Arrays.asList(filterDates));
        }

        List<Integer> blockIds = new ArrayList<>();
        if (filterBlockIds != null) {
            for(int blockId: filterBlockIds) {
                blockIds.add(blockId);
            }
        }

        TelegramUserTaskStatService.AggregatedStatData report = statService.createReport(tags, dates, blockIds);
        result.addObject("report", report);
        result.addObject("tags", tags);
        result.addObject("dates", dates);
        result.addObject("blockIds", blockIds);

        //Calc additional ui
        int maxTaskCount = 0;
        for(int blockId : blockIds) {
            int taskCountInBlock = checkTaskBlockService.getBlockTasksIds(blockId).size();
            if (taskCountInBlock > maxTaskCount) {
                maxTaskCount = taskCountInBlock;
            }
        }
        result.addObject("maxTaskCount", maxTaskCount);

        return result;
    }

    @RequestMapping(value = "/exportCsv", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ByteArrayResource> download(@RequestParam(required = false) String filterTags,
                                                      @RequestParam(required = false) String filterDates,
                                                      @RequestParam(required = false) String filterBlockIds,
                                                      @RequestParam(name = "calculatedField", required = false, defaultValue = "") String calculatedField) {

        String filename = "Report_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String charset = "windows-1251";

        List<String> tags = new ArrayList<>();
        if (filterTags != null) {
            filterTags = filterTags.strip();
            tags.addAll(Arrays.asList(filterTags.split(",")));
        }

        List<String> dates = new ArrayList<>();
        if (filterDates != null) {
            filterDates = filterDates.strip();
            Arrays.asList(filterDates.split(",")).forEach(filterDate -> {
                if (filterDate.length() == 10) {
                    dates.add(filterDate);
                }
            });
        }

        List<Integer> blockIds = new ArrayList<>();
        if (filterBlockIds != null) {
            filterBlockIds = filterBlockIds.strip();
            Arrays.asList(filterBlockIds.split(",")).forEach(blockId -> {
                blockIds.add(Integer.parseInt(blockId));
            });
        }

        Map<String, String> calculatedFieldsMap = new LinkedHashMap<>();
        if (!calculatedField.isBlank()) {
            calculatedFieldsMap.put("CF", calculatedField);
        }
        String csv = statService.createCsvReport(tags, dates, blockIds, calculatedFieldsMap);

        byte[] data = csv.getBytes(Charset.forName(charset));
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + ".xls\"")
                // Content-Type
                .contentType(MediaType.TEXT_PLAIN) //
                // Content-Lengh
                .contentLength(data.length) //
                .body(resource);
    }
}
