package com.intgroup.htmlcheck.feature.taskcheck.stat.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskRequestStatRecordService {
    @Autowired
    private TaskRequestStatRecordRepository repository;

    public void delete(Specification<TaskRequestStatRecord> specification) {
        repository.deleteAll(queryAll(specification));
    }

    public int count(Specification<TaskRequestStatRecord> specification) {
        return (int) repository.count(specification);
    }

    public List<TaskRequestStatRecord> queryAll(Specification<TaskRequestStatRecord> specification) {
        return repository.findAll(specification);
    }

    public void deleteById(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }

    public TaskRequestStatRecord getById(long id) {
        if (repository.existsById(id)) {
            return repository.getOne(id);
        }

        return null;
    }

    public void save(TaskRequestStatRecord msg) {
        repository.save(msg);
    }
}
