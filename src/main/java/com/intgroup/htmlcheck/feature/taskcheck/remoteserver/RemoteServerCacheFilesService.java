package com.intgroup.htmlcheck.feature.taskcheck.remoteserver;

import com.intgroup.htmlcheck.feature.bgtask.BgTaskService;
import com.intgroup.htmlcheck.feature.error.ErrorService;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.MarkdownDescriptionRenderService;
import com.intgroup.htmlcheck.feature.taskcheck.validate.TaskCheckValidateService;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class RemoteServerCacheFilesService {
    @Autowired
    private MarkdownDescriptionRenderService markdownDescriptionRenderService;

    @Autowired
    private TaskCheckValidateService taskCheckValidateService;

    @Autowired
    private ErrorService errorService;

    private Map<String, PrefixConfig> prefixConfigs;

    private Set<String> allowedFileExtensions;

    @Autowired
    private BgTaskService bgTaskService;

    @PostConstruct
    public void init() {
        prefixConfigs = new LinkedHashMap<>();
        allowedFileExtensions = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "svg", "gif", "pdf", "tiff"));
    }

    public void setPrefixConfig(String prefix, PrefixConfig config) {
        prefixConfigs.put(prefix, config);
    }

    public void cacheImages(RemoteCheckServerService.ServerTaskData taskData) {
        String taskId = taskData.getTaskId();
        String prefix = getPrefix(taskId);

        PrefixConfig config = prefixConfigs.get(prefix);
        if (config == null) {
            return;
        }

        String html = markdownDescriptionRenderService.renderHtml(taskData.getTaskDescription());

        String remoteServerUrl = taskData.getServerConfig().getHost() + ":" + taskData.getServerConfig().getPort() + "/";

        List<String> urls = new ArrayList<>();

        Jsoup.parse(html).select("img").forEach(img -> {
            String src = img.attr("src");
            urls.add(src);
        });

        if (taskData.getMetadata() != null) {
            taskData.getMetadata().values().forEach(value -> {
                if (isRelativeImgPath(value.toString())) {
                    urls.add(value.toString());
                }
            });
        }

        urls.forEach(src -> {
            if (src.startsWith("./")) {
                src = src.substring(2);
            }

            if (!src.startsWith(taskId)) {
                src = taskId + "/" + src;
            }

            String localFilePath = config.getBasePath() + "/" + src;
            String remoteImageUrl = remoteServerUrl + src;

            bgTaskService.submitTask("Download image from remote server for task " + taskId, 2, createDownloadImageRunnable(remoteImageUrl, localFilePath));
        });
    }

    public void validateTask(String taskId) {
        bgTaskService.submitTask("Validate task " + taskId, 2, () -> {
            try {
                TaskCheckValidateService.TaskCheckValidateResult validateResult = taskCheckValidateService.validate(taskId);
                validateResult.getInvalidLinks().forEach(invalidLink -> {
                    String errorText = "Битая ссылка в задаче " + taskId + ": " + invalidLink;
                    errorService.add(errorText);
                });

                if (!validateResult.getResult().equals("ok")) {
                    errorService.add(validateResult.getResult());
                }
            } catch (Exception ex) {
                System.out.println("Error in " + taskId);
                ex.printStackTrace();
            }

        });
    }

    public boolean isRelativeImgPath(String value) {
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


    public Runnable createDownloadImageRunnable(String url, String filePath) {
        return () -> {
          try {
              Connection.Response resultImageResponse = Jsoup.connect(url).maxBodySize(0).ignoreContentType(true).execute();

              File imgFile = new File(filePath);

              File imgFileFolder = imgFile.getParentFile();
              if (!imgFileFolder.exists()) {
                  imgFileFolder.mkdirs();
              }

              FileOutputStream out = (new FileOutputStream(imgFile));
              out.write(resultImageResponse.bodyAsBytes());
              out.close();
          } catch (Exception ex) {
              //ex.printStackTrace();
          }
        };
    }

    @Data
    public static class PrefixConfig {
        private String basePath;
    }

    private String getPrefix(String taskId) {
        String prefix = taskId.split("-")[0];

        if (prefix.equals("css")) {
            prefix = "html";
        }

        return prefix;
    }
}
