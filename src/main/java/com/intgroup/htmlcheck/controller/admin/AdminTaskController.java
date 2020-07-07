package com.intgroup.htmlcheck.controller.admin;

import com.intgroup.htmlcheck.domain.logic.Task;
import com.intgroup.htmlcheck.domain.logic.TaskType;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.service.logic.task.TaskService;
import com.intgroup.htmlcheck.service.logic.task.TaskSpecifications;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/task")
public class AdminTaskController {
    @Autowired
    private SeoService seoService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private PaginationService paginationService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage,
                             @RequestParam(name = "query", required = false, defaultValue = "") String query,
                             @RequestParam(name = "type", required = false) TaskType type,
                             @RequestParam(name = "status", required = false, defaultValue = "none") OperationStatus status) {
        User user = userService.getUser();

        ModelAndView result = new ModelAndView("admin/task/list");

        if (status != OperationStatus.none) {
            result.addObject("message", status.toString());
        }

        result.addObject("user", user);
        seoService.setTitle(result, "Задачи");

        List<Specification<Task>> specs = new ArrayList<>();

        specs.add(TaskSpecifications.any());

        if (!query.isBlank()) {
            result.addObject("query", query);
            specs.add(TaskSpecifications.taskContentLike(query));
        }

        if (type != null) {
            result.addObject("type", type);
            specs.add(TaskSpecifications.typeIs(type));
        }

        Specification<Task> userSpec = TaskSpecifications.and(specs);

        int recordCount = taskService.count(userSpec);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        List<Task> tasks = taskService.queryPage(currentPage, userSpec);
        result.addObject("tasks", tasks);

        return result;
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        Task toDelete = taskService.getById(id);

        if (toDelete == null) {
            return getRedirectToList(OperationStatus.notExists);
        }

        taskService.deleteById(id);

        return getRedirectToList(OperationStatus.successDelete);
    }

    @GetMapping("/create-update")
    public ModelAndView createUpdate(@RequestParam(required = false, defaultValue = "null") String id) {
        User user = userService.getUser();

        Task task = taskService.getById(id);
        if (task == null) {
            task = new Task();
            task.setId(id);
            task.setType(TaskType.html);
        }

        ModelAndView result = new ModelAndView("admin/task/create-update");
        seoService.setTitle(result, id.equalsIgnoreCase("null") ? "Создание задачи" : "Редактирование задачи");
        result.addObject("task", task);
        result.addObject("user", user);

        return result;
    }

    @PostMapping("/create-update")
    public String handleCreateUpdate(@RequestParam(required = false, defaultValue = "null") String id,
                                     @RequestParam TaskType type,
                                     @RequestParam String content) {

        Task task = taskService.getById(id);
        if (task == null) {
            task = new Task();
            task.setId(id);
        }

        task.setType(type);
        task.setContent(content);


        taskService.save(task);

        return id.equals("null") ? getRedirectToList(OperationStatus.successCreate) : getRedirectToList(OperationStatus.successEdit);
    }

    private String getRedirectToList(OperationStatus status) {
        return "redirect:/admin/task/list?status=" + status.name();
    }

    private enum OperationStatus {
        none(""),
        successDelete("Задача успешно удалена"),
        notExists("Такой задачи не существует"),
        successEdit("Задача успешно отредактирована"),
        successCreate("Новая задача успешно создана");

        private String description;

        OperationStatus(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
