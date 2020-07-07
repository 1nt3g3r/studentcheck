package com.intgroup.htmlcheck.feature.taskcheck.stat;

import com.intgroup.htmlcheck.feature.mq.EventDrivenMessage;
import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecord;
import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecordRepository;
import com.intgroup.htmlcheck.feature.taskcheck.stat.jpa.TaskRequestStatRecordService;
import com.intgroup.htmlcheck.service.util.DateService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TaskRequestStatService {
    @Autowired
    private DateService dateService;

    @Autowired
    private TaskRequestStatRecordService taskRequestStatRecordService;

    private static final int MAX_SAMPLE_COUNT = 200;

    private List<TaskRequestStat> stats;

    @PostConstruct
    public void init() {
        stats = new CopyOnWriteArrayList<>();
    }

    public void addSample(String taskId, String code, Map<String, Object> params) {
        if (stats.size() >= MAX_SAMPLE_COUNT) {
            stats.remove(0);
        }

        TaskRequestStat stat = new TaskRequestStat();
        stat.setTime(System.currentTimeMillis());
        stat.setTaskId(taskId);
        stats.add(stat);
    }

    public AggregatedTaskRequestStatData getAggregatedStatData() {
        AggregatedTaskRequestStatData result = new AggregatedTaskRequestStatData();

        if (stats.isEmpty()) {
            return result;
        }

        long totalTime = stats.get(stats.size() - 1).getTime() - stats.get(0).getTime();
        result.setTotalTime(totalTime);
        result.setTotalRequestCount(stats.size());

        for(int i = 0; i < stats.size(); i++) {
            TaskRequestStat stat = stats.get(i);
            String prefix = stat.getTaskId().split("-")[0];

            int count = result.getPrefixRequestCount().getOrDefault(prefix, 0);
            count++;
            result.getPrefixRequestCount().put(prefix, count);
        }

        return result;
    }

    @Scheduled(fixedDelay = 60_000)
    public void update() {
        removeOutdatedRecords();
        saveRecordToDatabase();
    }

    private void removeOutdatedRecords() {
        long currentTime = System.currentTimeMillis();
        long maxAllowedTime = currentTime - 60_000; //1 minute ago

        List<TaskRequestStat> toDelete = new ArrayList<>();
        for(TaskRequestStat stat: stats) {
            if (stat.getTime() <= maxAllowedTime) {
                toDelete.add(stat);
            }
        }
        stats.removeAll(toDelete);
    }

    private void saveRecordToDatabase() {
        AggregatedTaskRequestStatData statData = getAggregatedStatData();

        TaskRequestStatRecord record = new TaskRequestStatRecord();
        record.setDateTime(dateService.getCurrentDateTime());
        record.setRps(statData.getRPS());

        taskRequestStatRecordService.save(record);
    }

    public enum AggregateRpsType {
        day("По дням"),
        hour("По часам"),
        minute("По минутам");

        private String description;

        AggregateRpsType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Map<String, Float> aggregate(AggregateRpsType aggregateType, List<TaskRequestStatRecord> stats) {
        Map<String, Float> result = new LinkedHashMap<>();

        String dateFormatPattern = "";
        switch (aggregateType) {
            case day:
                dateFormatPattern = "yyyy-MM-dd";
                break;
            case hour:
                dateFormatPattern = "yyyy-MM-dd HH:00";
                break;
            case minute:
                dateFormatPattern = "yyyy-MM-dd HH:mm";
                break;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatPattern);

        Map<String, List<Float>> tempData = new LinkedHashMap<>();
        stats.forEach(stat -> {
            String label = stat.getDateTime() .format(formatter);

            if (!tempData.containsKey(label)) {
                tempData.put(label, new ArrayList<>());
            }

            tempData.get(label).add(stat.getRps());
        });

        tempData.forEach((label, list) -> {
            result.put(label, calculateAverage(list));
        });

        return result;
    }

    private float calculateAverage(List<Float> data) {
        float result = 0;

        for(Float item: data) {
            result += item;
        }

        if (data.size() > 0) {
            result /= data.size();
        }

        return result;
    }
}
