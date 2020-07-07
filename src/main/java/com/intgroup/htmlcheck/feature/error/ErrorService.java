package com.intgroup.htmlcheck.feature.error;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ErrorService {
    private List<String> errors;

    @PostConstruct
    public void init() {
        errors = new CopyOnWriteArrayList<>();
    }

    public void clear() {
        errors.clear();
    }

    public void add(String error) {
        if (errors.contains(error)) {
            return;
        }

        errors.add(error);
    }

    public List<String> getAll() {
        return errors;
    }
}
