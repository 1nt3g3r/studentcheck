package com.intgroup.htmlcheck.controller.api;

import com.intgroup.htmlcheck.domain.logic.Task;
import com.intgroup.htmlcheck.domain.logic.TaskType;
import com.intgroup.htmlcheck.service.logic.task.TaskService;
import com.intgroup.htmlcheck.service.logic.task.TaskSpecifications;
import com.intgroup.htmlcheck.service.security.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {
    @Autowired
    private ApiService apiService;

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/create", method = {RequestMethod.GET, RequestMethod.POST})
    public Object create(@RequestParam(required = false) String apiKey,
                         @RequestParam(required = false) String jsonText,
                         @RequestParam(required = false) TaskType type,
                         @RequestParam(name = "id", required = false) String id) {
        if (!apiService.isApiKeyOk(apiKey)) {
            return RequestResult.failBadApiKey();
        }

        if (jsonText == null) {
            return RequestResult.fail("JSON text not passed");
        }

        if (type == null) {
            return RequestResult.fail("Task type not passed");
        }

        if (id == null) {
            id = taskService.generateId(type);
        }

        Task existing = taskService.getById(id);
        if (existing != null) {
            return RequestResult.fail("Task with id " + id + " already exists");
        }

        try {
            Task task = new Task();
            task.setId(id);
            task.setContent(jsonText);
            task.setType(type);
            taskService.save(task);

            return RequestResult.success(id);
        } catch (Exception ex) {
            return RequestResult.fail(ex.getMessage());
        }
    }

    @RequestMapping(value = "/get", method = {RequestMethod.GET, RequestMethod.POST})
    public Object get(@RequestParam(required = false)String id,
                      @RequestParam(required = false)TaskType type) {
        if (type == null) {
            return RequestResult.fail("Task type not passed");
        }

        if (id == null) {
            return RequestResult.fail("ID not passed");
        }

        Task task = taskService.getById(id);
        if (task == null) {
            return RequestResult.fail("Task with id " + id + " not found");
        }

        return wrap(task);
    }

    @RequestMapping(value = "/getAll", method = {RequestMethod.GET, RequestMethod.POST})
    public Object getAll(@RequestParam(required = false) TaskType type) {
        if (type == null) {
            return RequestResult.fail("Task type not passed");
        }

        List<Task> tasks = taskService.queryAll(TaskSpecifications.typeIs(type));

        return wrap(tasks);
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.GET, RequestMethod.POST})
    public Object delete(@RequestParam(required = false)String apiKey,
                         @RequestParam(required = false)String id,
                         @RequestParam(required = false)TaskType type) {
        if (!apiService.isApiKeyOk(apiKey)) {
            return RequestResult.failBadApiKey();
        }

        if (type == null) {
            return RequestResult.fail("Task type not passed");
        }

        if (id == null) {
            return RequestResult.fail("ID not passed");
        }

        Task task = taskService.getById(id);
        if (task == null) {
            return RequestResult.fail("Task with id " + id + " not found");
        }

        taskService.deleteById(task.getId());

        return RequestResult.success(id);
    }

    @RequestMapping(value = "/update", method = {RequestMethod.GET, RequestMethod.POST})
    public Object update(@RequestParam(required = false) String apiKey,
                         @RequestParam(required = false) String jsonText,
                         @RequestParam(required = false) TaskType type,
                         @RequestParam(name = "id", required = false) String id) {

        if (!apiService.isApiKeyOk(apiKey)) {
            return RequestResult.failBadApiKey();
        }

        if (jsonText == null) {
            return RequestResult.fail("JSON text not passed");
        }

        if (type == null) {
            return RequestResult.fail("Task type not passed");
        }

        Task task = taskService.getById(id);
        if (task == null) {
            return RequestResult.fail("Task with id " + id + " not found");
        }

        task.setType(type);
        task.setContent(jsonText);
        taskService.save(task);

        return RequestResult.success(id);
    }

    private List<Map<String, Object>> wrap(List<Task> tasks) {
        List<Map<String, Object>> result = new ArrayList<>();

        tasks.forEach(task -> {
            result.add(wrap(task));
        });

        return result;
    }

    private Map<String, Object> wrap(Task task) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", task.getId());
        result.put("jsonText", task.getContent());
        result.put("type", task.getType().name());

        return result;
    }

}
