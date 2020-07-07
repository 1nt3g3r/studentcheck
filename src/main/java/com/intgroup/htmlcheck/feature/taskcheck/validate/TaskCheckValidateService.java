package com.intgroup.htmlcheck.feature.taskcheck.validate;

import com.intgroup.htmlcheck.feature.taskcheck.TaskCheckService;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskCheckValidateService {
    public static final String[] METADATA_CHECK_FIELDS = {"cvLink", "video"};

    @Autowired
    private TaskCheckService taskCheckService;

    public TaskCheckValidateResult validate(String taskId) {
        TaskCheckValidateResult result = new TaskCheckValidateResult();

        TaskCheckService.TaskData taskData = taskCheckService.getTaskData(taskId);
        if (taskData == null) {
            result.setResult("Task with id " + taskId + " not found");
        } else {
            List<String> links = getLinks(taskData);

            links.forEach(link -> {
                if (isLinkValid(link)) {
                    result.getValidLinks().add(link);
                } else {
                    result.getInvalidLinks().add(link);
                }
            });
        }

        return result;
    }

    private List<String> getLinks(TaskCheckService.TaskData taskData) {
        List<String> result = new ArrayList<>();

        //Extract links and images from html description
        Document descriptionDoc = Jsoup.parse(taskData.getHtmlDescription());
        descriptionDoc.select("a").forEach(a -> result.add(a.attr("href")));
        descriptionDoc.select("img").forEach(img -> result.add(img.attr("src")));

        //Extract links from initial html code
        Document initialHtmlDoc = Jsoup.parse(taskData.getMetadata().getOrDefault("initialHtml", "").toString());
        initialHtmlDoc.select("a").forEach(a -> result.add(a.attr("href")));
        initialHtmlDoc.select("img").forEach(img -> result.add(img.attr("src")));

        //Extract links from metadata
        for(String fieldName: METADATA_CHECK_FIELDS) {
            String link = taskData.getMetadata().getOrDefault(fieldName, "").toString();
            boolean linkOk = true;

            if (link.isEmpty()) {
                linkOk = false;
            }

            if (link.equals("#")) {
                linkOk = false;
            }

            if (linkOk) {
                result.add(link);
            }
        }

        //Remove empty, mail and phone links
        result.removeIf(link -> link.isEmpty() || link.contains("tel:") || link.contains("mailto:") || link.equals("#"));

        return result;
    }

    private boolean isLinkValid(String link) {
        if (link.replace("://", "").contains("//")) {
            return false;
        }

        try {
            Connection.Response response = Jsoup.connect(link).timeout(2000).ignoreContentType(true).method(Connection.Method.GET).execute();
            return response.statusCode() == 200;
        } catch (Exception ex) {
            return false;
        }
    }

    @Data
    public static class TaskCheckValidateResult {
        public String result = "ok";

        public List<String> validLinks = new ArrayList<>();
        public List<String> invalidLinks = new ArrayList<>();
    }

    public static void main(String[] args) {
        String link = "http://localhost:8888/taskcache/css-19//img/code/css-19/cv.svg";
        System.out.println(new TaskCheckValidateService().isLinkValid(link));
    }
}
