package com.intgroup.htmlcheck.feature.taskcheck.remoteserver;

import com.intgroup.htmlcheck.feature.bgtask.BgTaskService;
import com.intgroup.htmlcheck.feature.block.CheckTaskBlockService;
import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckResult;
import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
public class RemoteCheckServers {
    public static final String HTML_CSS_SERVERS = "remoteHtmlCssCheckServerConfigs";
    public static final String JS_SERVERS = "remoteJsCheckServerConfigs";
    public static final String JAVA_SERVERS = "remoteJavaCheckServerConfigs";
    public static final String PYTHON_SERVERS = "remotePythonCheckServerConfigs";

    public static final String CACHE_FOLDER = "taskcache";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GlobalPrefService globalPrefService;

    @Autowired
    private RemoteServerCacheFilesService remoteServerCacheFilesService;

    @Autowired
    private TaskCheckService taskCheckService;

    @Autowired
    private BgTaskService bgTaskService;

    @Autowired
    private CheckTaskBlockService checkTaskBlockService;

    private Set<String> validTaskIds;

    private Map<String, RemoteCheckServerService> serviceMap;

    public void init() {
        serviceMap = new HashMap<>();
        validTaskIds = new LinkedHashSet<>();

        try {
            setupAllRemoteServers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setupAllRemoteServers() {
        setupHtmlCssRemoteServers();
        setupJsRemoteServers();
        setupJavaRemoteServers();
        setupPythonRemoteServers();
    }

    public void setupJsRemoteServers() {
        String prefix = "js";

        String jsConfig = globalPrefService.getOrDefault(JS_SERVERS, "");
        if (!jsConfig.isEmpty()) {
            setupRemoteServer(prefix, jsConfig);
            invalidateCache(prefix);
        }
    }

    public void setupJavaRemoteServers() {
        String prefix = "java";

        String javaConfig = globalPrefService.getOrDefault(JAVA_SERVERS, "");
        if (!javaConfig.isEmpty()) {
            setupRemoteServer(prefix, javaConfig);
            invalidateCache(prefix);
        }
    }

    public void setupPythonRemoteServers() {
        String prefix = "python";

        String pythonConfig = globalPrefService.getOrDefault(PYTHON_SERVERS, "");
        if (!pythonConfig.isEmpty()) {
            setupRemoteServer(prefix, pythonConfig);
            invalidateCache(prefix);
        }
    }

    public void setupHtmlCssRemoteServers() {
        String prefix = "html";

        String htmlCssConfig = globalPrefService.getOrDefault(HTML_CSS_SERVERS, "");
        if (!htmlCssConfig.isEmpty()) {
            setupRemoteServer(prefix, htmlCssConfig);
            invalidateCache(prefix);
        }
    }

    private void setupRemoteServer(String prefix, String remoteServersConfig) {
        //Setup remote server
        RemoteCheckServerService jsService = context.getBean(RemoteCheckServerService.class);
        jsService.setTextProcessors(html -> {
            //Remove relative path
            html = html.replace("<img src=\"./", "<img src=\"");

            String pattern = "<img src=\"";
            String replace = pattern + "${domainHost}/" +CACHE_FOLDER + "/" + prefix + "/";
            return html.replace(pattern, replace);
        });
        jsService.parseConfigs(remoteServersConfig);
        serviceMap.put(prefix, jsService);

        //Setup caching
        RemoteServerCacheFilesService.PrefixConfig config = new RemoteServerCacheFilesService.PrefixConfig();
        config.setBasePath("./data/" +CACHE_FOLDER + "/" + prefix);
        remoteServerCacheFilesService.setPrefixConfig(prefix, config);
    }



    public TaskCheckResult check(String taskId, String code, Map<String, Object> params) {
        String prefix = getTaskPrefix(taskId);

        if (!serviceMap.containsKey(prefix)) {
            return null;
        }

        try {
            return getRemoteServer(prefix).check(taskId, code, params);
        } catch (Exception ex) {
            return null;
        }
    }

    public TaskCheckService.TaskData getTaskData(String taskId) {
        String prefix = getTaskPrefix(taskId);

        if (!serviceMap.containsKey(prefix)) {
            return null;
        }

        try {
            return getRemoteServer(prefix).getTaskData(taskId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getTaskPrefix(String taskId) {
        String prefix = taskId.split("-")[0];

        if (prefix.equals("css")) {
            prefix = "html";
        }

        return prefix;
    }

    public boolean isValidTaskId(String taskId) {
        return validTaskIds.contains(taskId);
    }

    public Set<String> getValidTaskIds() {
        return validTaskIds;
    }

    public void invalidateAllCaches() {
        serviceMap.keySet().forEach(this::invalidateCache);
    }

    public int getLoadCacheQueueSize() {
        return bgTaskService.getTaskCount("Download image");
    }

    public void invalidateCache(String prefix) {
        //Remove all prefix tasks
        Set<String> toRemoveTaskIds = new HashSet<>();
        validTaskIds.forEach(taskId -> {
            if (taskId.startsWith(prefix) || (prefix.equals("html") && taskId.startsWith("css"))) {
                taskCheckService.clearTaskDataCache(taskId);
                toRemoveTaskIds.add(taskId);
            }
        });
        validTaskIds.removeAll(toRemoveTaskIds);

        RemoteCheckServerService remoteCheckServerService = getRemoteServer(prefix);
        if (remoteCheckServerService == null) {
            return;
        }

        try {
            List<String> taskIds = remoteCheckServerService.getTaskIds();
            validTaskIds.addAll(taskIds);

            //Cache images
            for(String taskId: taskIds) {
                remoteServerCacheFilesService.cacheImages(remoteCheckServerService.getServerTaskData(taskId));
            }

            //Validate tasks
            for(String taskId: taskIds) {
                remoteServerCacheFilesService.validateTask(taskId);
            }

            for(String taskId: taskIds) {
                checkTaskBlockService.addSingleTaskAsDebugBlock(taskId);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public RemoteCheckServerService getRemoteServer(String prefix) {
        if (prefix.equals("css")) {
            prefix = "html";
        }

        return serviceMap.get(prefix);
    }
}
