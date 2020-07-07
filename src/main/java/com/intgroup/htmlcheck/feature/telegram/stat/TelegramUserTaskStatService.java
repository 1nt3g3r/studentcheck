package com.intgroup.htmlcheck.feature.telegram.stat;

import com.intgroup.htmlcheck.feature.block.CheckTaskBlockService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStat;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatSpecifications;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import groovy.lang.GroovyShell;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TelegramUserTaskStatService {
    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private UserTaskStatService userTaskStatService;

    @Autowired
    private CheckTaskBlockService checkTaskBlockService;

    private GroovyShell groovyShell;

    @PostConstruct
    public void init() {
        groovyShell = new GroovyShell();
    }

    public String createCsvReport(List<String> tags, List<String> dates, List<Integer> blockIds, Map<String, String> calculatedFields) {
        //Get users
        List<TelegramUser> users = getTelegramUsers(tags, dates);

        //Get user stats
        List<UserTaskStat> stats = getUserTaskStats(users);

        //Filter stats
        List<String> taskIds = new ArrayList<>();

        blockIds.forEach(blockId ->
            taskIds.addAll(checkTaskBlockService.getBlockTasksIds(blockId))
        );

        //Get stats for each user
        Map<Long, List<UserTaskStat>> userStatMap = new HashMap<>();
        users.forEach(user -> {
            List<UserTaskStat> userStats = new ArrayList<>();
            stats.forEach(stat -> {
                String userId = user.getUserId() + "@telegram-user";
                if (stat.getUserId().equals(userId)) {
                    userStats.add(stat);
                }
            });
            userStatMap.put(user.getUserId(), userStats);
        });

        //Make CSV
        StringWriter s = new StringWriter();
        ICSVWriter writer = new CSVWriterBuilder(s).withSeparator('\t').withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER).build();

        //Prepare header
        List<String> header = new ArrayList<>();
        header.addAll(Arrays.asList("ID", "Имя", "Телефон", "Telegram ник"));
        for (String taskId : taskIds) {
            header.add(taskId);
            header.add("Дата решения");
            header.add("Время решения");
        }

        //Add calculated fields to header
        calculatedFields.keySet().forEach(header::add);

        String[] headerArray = new String[header.size()];
        header.toArray(headerArray);
        writer.writeNext(headerArray);

        //Fill data
        users.forEach(user -> {
            List<String> reportRow = new ArrayList<>();

            //Header
            reportRow.add(Long.toString(user.getUserId()));
            reportRow.add(user.getFullName());
            reportRow.add(user.getPhone());
            reportRow.add(user.getUserName());

            Map<String, String> solveTaskDates = new LinkedHashMap<>();

            //Data
            taskIds.forEach(taskId -> {
                reportRow.add(taskId);

                boolean taskFound = false;
                for (UserTaskStat taskIdStat : userStatMap.get(user.getUserId())) {
                    if (taskIdStat.getTaskId().equals(taskId)) {
                        String date = "-";
                        String time = "-";

                        LocalDateTime solveDateTime = taskIdStat.getSolveDateTime();
                        if (solveDateTime != null) {
                            date = solveDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
                            time = solveDateTime.toLocalTime().format(DateTimeFormatter.ISO_TIME);
                        }

                        reportRow.add(date);
                        reportRow.add(time);
                        taskFound = true;

                        solveTaskDates.put(taskId, date);
                        break;
                    }
                }

                if (!taskFound) {
                    String date = "-";
                    String time = "-";

                    reportRow.add(date);
                    reportRow.add(time);

                    solveTaskDates.put(taskId, "-");
                }
            });

            //Add calculated fields
            if (!calculatedFields.isEmpty()) {
                calculatedFields.values().forEach(program -> {
                    String value = calculateField(program, user, solveTaskDates);
                    reportRow.add(value);
                });
            }

            //Write row
            String[] reportRowArray = new String[reportRow.size()];
            reportRow.toArray(reportRowArray);
            writer.writeNext(reportRowArray);

        });

        return s.toString();
    }

    private String calculateField(String script, TelegramUser telegramUser, Map<String, String> solveTaskDates) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("telegramUser", telegramUser);
            params.put("solveTaskDates", solveTaskDates);
            return execute(script, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error!";
        }
    }

    public String execute(String script, Map<String, Object> params) {
        try {
            params.forEach((key, value) -> {
                groovyShell.setVariable(key, value);
            });
            return groovyShell.evaluate(script) + "";
        } catch (Exception ex) {
            return "Error!";
        }
    }

    public AggregatedStatData createReport(List<String> tags, List<String> dates, List<Integer> blockIds) {
        AggregatedStatData report = new AggregatedStatData();

        //Get users
        List<TelegramUser> users = getTelegramUsers(tags, dates);

        //Get user stats
        List<UserTaskStat> stats = getUserTaskStats(users);

        //Calc stats
        report.totalUserCount = users.size();

        Map<String, List<Integer>> solveTryCount = new LinkedHashMap<>();
        Map<String, List<Integer>> solveSeconds = new LinkedHashMap<>();

        Map<String, Set<String>> userSolvedTaskCount = new LinkedHashMap<>();

        //Fill initial task ids
        List<String> allReportTaskIds = new ArrayList<>();
        blockIds.forEach(blockId -> {
            report.getTaskIdMap().put(blockId, checkTaskBlockService.getBlockTasksIds(blockId));

            allReportTaskIds.addAll(checkTaskBlockService.getBlockTasksIds(blockId));
        });

        allReportTaskIds.forEach(taskId -> {
            solveTryCount.put(taskId, new ArrayList<>());
            solveSeconds.put(taskId, new ArrayList<>());
            userSolvedTaskCount.put(taskId, new HashSet<>());
        });

        //Gather single stats stats
        stats.forEach(stat -> {
            if (stat.isSolved()) {
                String taskId = stat.getTaskId();
                if (allReportTaskIds.contains(taskId)) {
                    solveTryCount.get(taskId).add(stat.getSolveTryCount());

                    int solveTimeInSeconds = stat.getSolveTimeSeconds();
                    if (solveTimeInSeconds <= 0) {
                        solveTimeInSeconds = 5;
                    }
                    solveSeconds.get(taskId).add(solveTimeInSeconds);

                    userSolvedTaskCount.get(taskId).add(stat.getUserId());
                }
            }
        });

        //Gather stats
        allReportTaskIds.forEach(taskId -> {
            SingleTaskStat singleTaskStat = new SingleTaskStat();

            singleTaskStat.setTaskId(taskId);
            singleTaskStat.setSolveCount(userSolvedTaskCount.get(taskId).size());

            //Calc avg time
            float[] solveTimeStats = calculateMinMaxAvg(solveSeconds.get(taskId), 10);
            singleTaskStat.setMinSolveTimeSeconds((int) solveTimeStats[0]);
            singleTaskStat.setMaxSolveTimeSeconds((int) solveTimeStats[1]);
            singleTaskStat.setAvgSolveTimeSeconds((int) solveTimeStats[2]);

            //Calc solve try count
            float[] solveTryCountStats = calculateMinMaxAvg(solveTryCount.get(taskId), 0);
            singleTaskStat.setMinTryCount((int) solveTryCountStats[0]);
            singleTaskStat.setMaxTryCount((int) solveTryCountStats[1]);
            singleTaskStat.setAvgTryCount(solveTryCountStats[2]);

            report.getTaskStats().put(singleTaskStat.getTaskId(), singleTaskStat);
        });

        //Find easiest and hardest tasks based on solve time
        markHardestAndEasiestTasks(new ArrayList<>(report.getTaskStats().values()), stats, 10);

        return report;
    }

    private List<TelegramUser> getTelegramUsers(List<String> tags, List<String> dates) {
        List<Specification<TelegramUser>> tagSpecifications = new ArrayList<>();
        tags.forEach(tagName -> {
            tagSpecifications.add(TelegramUserSpecifications.tagIs(tagName));
        });

        List<Specification<TelegramUser>> dateSpecifications = new ArrayList<>();
        dates.forEach(date -> {
            dateSpecifications.add(TelegramUserSpecifications.eventDateIs(date));
        });

        Specification<TelegramUser> combinedUserSpec;
        if (tagSpecifications.size() > 0 && dateSpecifications.size() > 0) {
            combinedUserSpec = TelegramUserSpecifications.and(
                    TelegramUserSpecifications.or(tagSpecifications),
                    TelegramUserSpecifications.or(dateSpecifications)
            );
        } else if (tagSpecifications.size() > 0) {
            combinedUserSpec = TelegramUserSpecifications.or(tagSpecifications);
        } else if (dateSpecifications.size() > 0) {
            combinedUserSpec = TelegramUserSpecifications.or(dateSpecifications);
        } else {
            combinedUserSpec = TelegramUserSpecifications.any();
        }

        return telegramUserService.queryAll(combinedUserSpec);
    }

    private List<UserTaskStat> getUserTaskStats(List<TelegramUser> telegramUsers) {
        if (telegramUsers.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> userIds = new ArrayList<>();
        telegramUsers.forEach(telegramUser -> {
            String userId = telegramUser.getUserId() + "@telegram-user";
            userIds.add(userId);
        });
        return userTaskStatService.queryAll(UserTaskStatSpecifications.userIdInList(userIds));
    }

    private float[] calculateMinMaxAvg(List<Integer> source, int percentCut) {
        List<Integer> data = new ArrayList<>(source);
        data.sort(Integer::compareTo);

        int min = 0;
        int max = 0;
        if (data.size() > 0) {
            min = data.get(0);
            max = data.get(data.size() - 1);

            if (percentCut > 0) {
                int percent = data.size() * percentCut / 100;
                int minIndex = percent;
                int maxIndex = data.size() - percent;

                data = data.subList(minIndex, maxIndex);
            }

            return new float[]{
                    min,
                    max,
                    calculateGeometricMean(data)
            };
        } else {
            return new float[]{0, 0, 0};
        }
    }

    private float calculateGeometricMean(List<Integer> data) {
        float sum = 0;

        for (int i = 0; i < data.size(); i++) {
            sum = sum + (float) Math.log(data.get(i));
        }

        sum = sum / (float) data.size();

        float result = (float) Math.exp(sum);

        return ((int) (result * 100)) / 100f;
    }

    private void markHardestAndEasiestTasks(List<SingleTaskStat> toMark, List<UserTaskStat> taskStats, int percentCut) {
        //Gather solve times & sort it
        List<Integer> solveTimes = new ArrayList<>();
        for (UserTaskStat stat : taskStats) {
            if (stat.isSolved() && stat.getSolveTimeSeconds() > 0) {
                solveTimes.add(stat.getSolveTimeSeconds());
            }
        }
        solveTimes.sort(Integer::compareTo);

        //Cut upper and lower
        if (percentCut > 0) {
            int percent = solveTimes.size() * percentCut / 100;
            int minIndex = percent;
            int maxIndex = solveTimes.size() - percent;

            solveTimes = solveTimes.subList(minIndex, maxIndex);
        }

        float avgTaskTime = calculateGeometricMean(solveTimes);

        //Mark each task as hardest or easiest
        toMark.forEach(taskStat -> {
            float diff = (taskStat.getAvgSolveTimeSeconds() - avgTaskTime) / avgTaskTime;
            if (diff >= 0.5) {
                taskStat.setHardest(true);
            } else if (diff <= -0.5) {
                taskStat.setEasiest(true);
            }
        });
    }

    @Data
    public static class AggregatedStatData {
        private int totalUserCount;
        private Map<String, SingleTaskStat> taskStats = new LinkedHashMap<>();

        //Additional help data
        private Map<Integer, List<String>> taskIdMap = new LinkedHashMap<>();

        public SingleTaskStat getTaskStat(int blockIndex, int taskIndex) {
            if (!taskIdMap.containsKey(blockIndex)) {
                return null;
            }

            if (taskIdMap.get(blockIndex).size() <= taskIndex) {
                return null;
            }

            String taskId = taskIdMap.get(blockIndex).get(taskIndex);
            return taskStats.get(taskId);
        }

        public int getSolvedUserCount(int blockIndex, int taskIndex) {
            SingleTaskStat taskStat = getTaskStat(blockIndex, taskIndex);
            if (taskStat == null) {
                return -1;
            }

            return taskStat.getSolveCount();
        }

        public int getTaskProgressInPercents(int blockIndex, int taskIndex) {
            SingleTaskStat taskStat = getTaskStat(blockIndex, taskIndex);
            if (taskStat == null) {
                return -1;
            }

            return taskStat.getSolveCount() * 100 / totalUserCount;
        }
    }

    @Data
    public static class SingleTaskStat {
        private String taskId;
        private int solveCount;

        private int avgSolveTimeSeconds;
        private int minSolveTimeSeconds;
        private int maxSolveTimeSeconds;

        private float avgTryCount;
        private int minTryCount;
        private int maxTryCount;

        private boolean hardest;
        private boolean easiest;

        public boolean isEmpty() {
            return solveCount == 0;
        }

        public String formatTime(int fullTimeInSeconds) {
            int seconds = fullTimeInSeconds % 60;
            int minutes = fullTimeInSeconds / 60;

            String secondsString = seconds < 10 ? "0" + seconds : Integer.toString(seconds);
            String minutesString = minutes < 10 ? "0" + minutes : Integer.toString(minutes);

            return minutesString + ":" + secondsString;
        }
    }

    public static void main(String[] args) {
        List<Integer> result = new ArrayList<>(Arrays.asList(1, 5, 3, 4, 7, 20, 6, 5, 4, 4, 100000));
        int percent = result.size() / 10;
        int minIndex = percent;
        int maxIndex = result.size() - percent;

        System.out.println(result.size() + " " + percent + " " + minIndex + " " + maxIndex);
        result.sort(Integer::compareTo);
        System.out.println(new TelegramUserTaskStatService().calculateGeometricMean(result));

        System.out.println("Cut");
        result = result.subList(minIndex, maxIndex);
        System.out.println(new TelegramUserTaskStatService().calculateGeometricMean(result));


    }
}
