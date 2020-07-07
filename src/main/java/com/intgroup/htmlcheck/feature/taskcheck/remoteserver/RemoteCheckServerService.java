package com.intgroup.htmlcheck.feature.taskcheck.remoteserver;

import com.google.gson.Gson;
import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckResult;
import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.HighlightLineService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.MarkdownDescriptionRenderService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.NotEditableMarkupService;
import lombok.Data;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
@Scope("prototype")
public class RemoteCheckServerService {
    protected Gson gson;
    protected List<ServerConfig> configs;
    protected int currentConfigIndex;

    protected MarkdownDescriptionRenderService.TextProcessor[] textProcessors = new MarkdownDescriptionRenderService.TextProcessor[0];

    private Set<String> allowedFileExtensions;

    @Value("${domainHost}")
    private String domainHost;

    @Autowired
    protected MarkdownDescriptionRenderService markdownDescriptionRenderService;

    @Autowired
    private HighlightLineService highlightLineService;

    @Autowired
    private NotEditableMarkupService notEditableMarkupService;

    @PostConstruct
    public void init() {
        gson = new Gson();
        allowedFileExtensions = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "svg", "gif", "pdf", "tiff"));
    }

    public void setTextProcessors(MarkdownDescriptionRenderService.TextProcessor... textProcessors) {
        this.textProcessors = textProcessors;
    }

    public ServerTaskData getServerTaskData(String taskId) throws IOException {
        String json = get("/task/get", "taskId", taskId);
        ServerTaskData serverTaskData = gson.fromJson(json, ServerTaskData.class);

        serverTaskData.setTaskId(taskId);
        serverTaskData.setServerConfig(configs.get(currentConfigIndex));
        return serverTaskData;
    }

    public TaskCheckService.TaskData getTaskData(String taskId) throws IOException {
        ServerTaskData serverTaskData = getServerTaskData(taskId);

        TaskCheckService.TaskData result = new TaskCheckService.TaskData();

        //Fill initial data - code
        String initialCode = getOrEmpty(serverTaskData.getInitialCode());
        result.setCodeHlLines(highlightLineService.getHlLines(initialCode));
        initialCode = highlightLineService.clean(initialCode);
        result.setNotEditableCodeBlocks(notEditableMarkupService.makeNotEditableBlocks(initialCode));
        initialCode = notEditableMarkupService.cleanText(initialCode);
        result.setInitialCode(initialCode);

        //Fill initial data - html
        String initialHtml = getOrEmpty(serverTaskData.getMetadata().getOrDefault("initialHtml", "").toString());
        result.setHtmlHlLines(highlightLineService.getHlLines(initialHtml));
        initialHtml = highlightLineService.clean(initialHtml);
        result.setNotEditableHtmlBlocks(notEditableMarkupService.makeNotEditableBlocks(initialHtml));
        result.setInitialHtml(notEditableMarkupService.cleanText(initialHtml));

        //Fill initial data - css
        String initialCss = getOrEmpty(serverTaskData.getMetadata().getOrDefault("initialCss", "").toString());
        result.setCssHlLines(highlightLineService.getHlLines(initialCss));
        initialCss = highlightLineService.clean(initialCss);
        result.setNotEditableCssBlocks(notEditableMarkupService.makeNotEditableBlocks(initialCss));
        result.setInitialCss(notEditableMarkupService.cleanText(initialCss));

        //Fill cursors
        try {
            int[] htmlCursor = gson.fromJson(serverTaskData.getMetadata().getOrDefault("htmlCursor", "[0, 0]").toString(), int[].class);
            int[] cssCursor = gson.fromJson(serverTaskData.getMetadata().getOrDefault("cssCursor", "[0, 0]").toString(), int[].class);
            int[] codeCursor = gson.fromJson(serverTaskData.getMetadata().getOrDefault("codeCursor", "[0, 0]").toString(), int[].class);

            result.setHtmlCursor(htmlCursor);
            result.setCssCursor(cssCursor);
            result.setCodeCursor(codeCursor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //Metadata
        Map<String, Object> metaMap = serverTaskData.getMetadata();
        String prefix = getPrefix(taskId);
        try {
            result.setMetadata(new LinkedHashMap<>());

            metaMap.forEach((key, value) -> {
                String processedValue = value.toString();
                if (isRelativeImgPath(processedValue)) {

                    if (processedValue.startsWith("./")) {
                        processedValue = processedValue.substring(2);
                    }

                    if (!processedValue.startsWith(taskId)) {
                        processedValue = taskId + "/" + processedValue;
                    }

                    String fullImgPath = domainHost + "/" + RemoteCheckServers.CACHE_FOLDER + "/" + prefix + "/" + processedValue;
                    result.getMetadata().put(key, fullImgPath);
                } else {
                    result.getMetadata().put(key, value);
                }

            });

        } catch (Exception ex) {
            ex.printStackTrace();

            result.setMetadata(new HashMap<>());
            result.getMetadata().put("info", "Error parsing config file");
        }

        //Handle description
        String description = serverTaskData.taskDescription;
        String htmlDescription = markdownDescriptionRenderService.renderHtml(description, textProcessors);

        result.setHtmlDescription(htmlDescription);

        result.setTaskId(taskId);

        return result;
    }


    private String getPrefix(String taskId) {
        String prefix = taskId.split("-")[0];

        if (prefix.equals("css")) {
            prefix = "html";
        }

        return prefix;
    }

    private boolean isRelativeImgPath(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return false;
        }

        String[] valueParts = value.split("\\.");

        if (valueParts.length < 2) {
            return false;
        }

        String extension = valueParts[valueParts.length - 1];
        return allowedFileExtensions.contains(extension);
    }

    private String getOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public List<String> getTaskIds() throws IOException {
        String json = get("/task/list");
        return gson.fromJson(json, List.class);
    }

    public String get(String url, String... params) throws IOException {
        ServerConfig config = getNextConfig();

        Map<String, String> data = new HashMap<>();
        data.put("token", config.token);
        for (int i = 0; i < params.length; i += 2) {
            data.put(params[i], params[i + 1]);
        }

        String fullUrl = config.host + ":" + config.port + url;
        return Jsoup.connect(fullUrl).ignoreContentType(true).data(data).execute().body();
    }

    public TaskCheckResult check(String taskId, String code) throws IOException {
        return check(taskId, code, Collections.emptyMap());
    }

    public TaskCheckResult check(String taskId, String code, Map<String, Object> params) throws IOException {
        ServerConfig config = getNextConfig();

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("token", config.getToken());
        requestBody.put("taskId", taskId);
        requestBody.put("code", code);
        if (params == null) {
            params = Collections.emptyMap();
        }
        requestBody.put("params", params);

        String fullUrl = config.host + ":" + config.port + "/task/check";
        String requestBodyString = gson.toJson(requestBody);
        String json = Jsoup.connect(fullUrl)
                .ignoreContentType(true)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .requestBody(requestBodyString)
                .post()
                .text();

        return gson.fromJson(json, TaskCheckResult.class);
    }

    protected ServerConfig getNextConfig() {
        currentConfigIndex++;

        if (currentConfigIndex >= configs.size()) {
            currentConfigIndex = 0;
        }

        return configs.get(currentConfigIndex);
    }

    public void parseConfigs(String data) {
        configs = new ArrayList<>();
        currentConfigIndex = 0;

        data = data.replace("\r", "");
        Arrays.asList(data.split("\n")).forEach(line -> {
            try {
                String[] parts = line.split(";");

                String token = parts[parts.length - 1];

                String[] hostParts = parts[0].split(":");
                int port = Integer.parseInt(hostParts[hostParts.length - 1]);

                StringJoiner host = new StringJoiner(":");
                for (int i = 0; i < hostParts.length - 1; i++) {
                    host.add(hostParts[i]);
                }

                ServerConfig config = new ServerConfig();
                config.setToken(token);
                config.setHost(host.toString());
                config.setPort(port);

                configs.add(config);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Data
    public static class ServerTaskData {
        private ServerConfig serverConfig;

        private boolean success;
        private String taskId;
        private String taskDescription;
        private String initialCode;
        private String expectedResult;
        private Map<String, Object> metadata;
    }

    @Data
    public static class ServerConfig {
        private String host;
        private int port;
        private String token;
    }
}
