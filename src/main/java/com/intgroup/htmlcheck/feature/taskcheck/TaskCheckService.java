package com.intgroup.htmlcheck.feature.taskcheck;

import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.log.LogItemService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.HighlightLineService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.NotEditableMarkupService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.RemoteCheckServers;
import com.intgroup.htmlcheck.feature.taskcheck.stat.TaskRequestStatService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import lombok.Data;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class TaskCheckService {
    @Autowired
    private RemoteCheckServers remoteCheckServers;

    @Autowired
    private LogItemService logItemService;

    @Autowired
    private TaskRequestStatService taskRequestStatService;

    @Autowired
    private UserTaskStatService userTaskStatService;

    private Map<String, TaskData> taskDataCache;

    @PostConstruct
    public void init() {
        taskDataCache = new HashMap<>();
    }

    public int getTotalTaskCount() {
        return remoteCheckServers.getValidTaskIds().size();
    }

    public Set<String> getAllTaskIdList() {
        return new HashSet<>(remoteCheckServers.getValidTaskIds());
    }

    public boolean isInvalidTaskId(String taskId) {
        return !remoteCheckServers.isValidTaskId(taskId);
    }

    public void clearTaskDataCache(String taskId) {
        if (taskDataCache != null) {
            taskDataCache.remove(taskId);
        }
    }

    public TaskCheckResult check(String taskId, String code, Map<String, Object> params) {
        //Gather stats
        taskRequestStatService.addSample(taskId, code, params);

        if (!remoteCheckServers.isValidTaskId(taskId)) {
            TaskCheckResult result = new TaskCheckResult();
            result.setCheckSuccess(false);
            result.setCheckMessage("Task with id " + taskId + " not found");
            return result;
        }

        try {
            TaskCheckResult result = remoteCheckServers.check(taskId, code, params);

            if (result == null) {
                TaskCheckResult checkResult = new TaskCheckResult();
                checkResult.setCheckSuccess(false);
                checkResult.failed("Unknown error on remote server");
                return checkResult;
            } else {
                if (result.isTaskSuccessfullyPassed()) {
                    postHandleTask(taskId, code, params);
                }

                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            TaskCheckResult result = new TaskCheckResult();
            result.setCheckSuccess(false);
            result.setCheckMessage("Unknown error");
            return result;
        }
    }

    private void postHandleTask(String taskId, String js, Map<String, Object> params) {
        try {
            if (taskId.equals("css-netlify")) {
                String html = params.getOrDefault("html", "").toString();
                Jsoup.parse(html).select("a").forEach(link -> {
                    if (link.hasAttr("href")) {
                        String href = link.attr("href");
                        if (href.toLowerCase().contains("netlify")) {
                            logItemService.log("netlifyLink", href);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public List<TaskData> getTaskDataList(String filter) {
        List<String> idList = new ArrayList<>();
        if (filter.isBlank()) {
            idList.addAll(remoteCheckServers.getValidTaskIds());
        } else {
            remoteCheckServers.getValidTaskIds().forEach(id -> {
                if (id.contains(filter)) {
                    idList.add(id);
                }
            });
        }

        List<TaskData> result = new ArrayList<>();
        idList.forEach(id -> result.add(getTaskData(id)));

        return result;
    }

    public UserSpecificTaskData getUserSpecificTaskData(String taskId, User user) {
        UserSpecificTaskData result = UserSpecificTaskData.from(getTaskData(taskId));
        result.isPassed = userTaskStatService.getPassedTasks(user.getEmail()).contains(taskId);
        return result;
    }

    public TaskData getTaskData(String taskId) {
        if (taskDataCache.containsKey(taskId)) {
            return taskDataCache.get(taskId);
        }

        TaskData result = remoteCheckServers.getTaskData(taskId);
        taskDataCache.put(taskId, result);
        return result;
    }

    @Data
    public static class UserSpecificTaskData {
        private String taskId;
        private String htmlDescription;
        private String mdDescription;
        private String initialHtml;
        private String initialCss;
        private String initialCode;
        private boolean isPassed;
        private Map<String, Object> metadata = new LinkedHashMap<>();

        private List<NotEditableMarkupService.NotEditableBlock> notEditableHtmlBlocks;
        private List<NotEditableMarkupService.NotEditableBlock> notEditableCssBlocks;
        private List<NotEditableMarkupService.NotEditableBlock> notEditableJsBlocks;

        private List<HighlightLineService.HlLine> htmlHlLines;
        private List<HighlightLineService.HlLine> cssHlLines;
        private List<HighlightLineService.HlLine> codeHlLines;

        private int[] htmlCursor = new int[2];
        private int[] cssCursor = new int[2];
        private int[] codeCursor = new int[2];

        public static UserSpecificTaskData from(TaskData data) {
            UserSpecificTaskData result = new UserSpecificTaskData();
            result.taskId = data.taskId;
            result.htmlDescription = data.htmlDescription;
            result.mdDescription = data.mdDescription;
            result.initialHtml = data.initialHtml;
            result.initialCss = data.initialCss;
            result.initialCode = data.initialCode;
            result.metadata = new LinkedHashMap<>(data.metadata == null ? Collections.EMPTY_MAP : data.metadata);

            result.notEditableHtmlBlocks = data.notEditableHtmlBlocks;
            result.notEditableCssBlocks = data.notEditableCssBlocks;
            result.notEditableJsBlocks = data.notEditableCodeBlocks;

            result.htmlHlLines = data.htmlHlLines;
            result.cssHlLines = data.cssHlLines;
            result.codeHlLines = data.codeHlLines;

            result.htmlCursor = data.htmlCursor;
            result.cssCursor = data.cssCursor;
            result.codeCursor = data.codeCursor;

            return result;
        }
    }

    @Data
    public static class TaskData {
        private String taskId;
        private String htmlDescription;
        private String mdDescription;
        private String initialHtml;
        private String initialCss;
        private String initialCode;
        private Map<String, Object> metadata = new LinkedHashMap<>();

        private List<NotEditableMarkupService.NotEditableBlock> notEditableHtmlBlocks = new ArrayList<>();
        private List<NotEditableMarkupService.NotEditableBlock> notEditableCssBlocks = new ArrayList<>();
        private List<NotEditableMarkupService.NotEditableBlock> notEditableCodeBlocks = new ArrayList<>();

        private List<HighlightLineService.HlLine> htmlHlLines = new ArrayList<>();
        private List<HighlightLineService.HlLine> cssHlLines = new ArrayList<>();
        private List<HighlightLineService.HlLine> codeHlLines = new ArrayList<>();

        private int[] htmlCursor = new int[2];
        private int[] cssCursor = new int[2];
        private int[] codeCursor = new int[2];
    }
}
