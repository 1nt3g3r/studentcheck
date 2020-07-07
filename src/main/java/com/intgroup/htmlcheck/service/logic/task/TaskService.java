package com.intgroup.htmlcheck.service.logic.task;

import com.intgroup.htmlcheck.domain.logic.Task;
import com.intgroup.htmlcheck.domain.logic.TaskType;
import com.intgroup.htmlcheck.repository.logic.TaskRepository;
import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository parseRecordRepository;

    public List<Task> getAll() {
        return parseRecordRepository.findAll();
    }

    public Task getById(String id) {
        if (parseRecordRepository.existsById(id)) {
            return parseRecordRepository.getOne(id);
        }

        return null;
    }

    public void deleteById(String id) {
        if (parseRecordRepository.existsById(id)) {
            parseRecordRepository.deleteById(id);
        }
    }

    public void save(Task record) {
        parseRecordRepository.save(record);
    }

    public void delete(Specification<Task> specification) {
        parseRecordRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<Task> specification) {
        return (int) parseRecordRepository.count(specification);
    }

    public List<Task> queryAll(Specification<Task> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        return parseRecordRepository.findAll(specification, sort);
    }

    public List<Task> queryPage(int pageIndex, Specification<Task> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<Task> page = parseRecordRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public String generateId(TaskType taskType) {
        List<Task> tasks = queryAll(TaskSpecifications.typeIs(taskType));

        int index = 0;

        if (tasks.size() > 0) {
            index = tasks.size() + 1;
        }

        return taskType.name() + "-" + index;
    }
}
