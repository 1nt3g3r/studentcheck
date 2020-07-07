package com.intgroup.htmlcheck.service.logic.task;

import com.intgroup.htmlcheck.domain.logic.Task;
import com.intgroup.htmlcheck.domain.logic.TaskType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class TaskSpecifications {
    public static Specification<Task> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<Task> idIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Task> notIdIs(long id) {
        return (root, cq, cb) -> cb.notEqual(root.get("id"), id);
    }

    public static Specification<Task> typeIs(TaskType type) {
        return (root, cq, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Task> taskContentLike(String content) {
        return (root, cq, cb) -> cb.like(cb.lower(root.get("content")), "%" + content.toLowerCase() + "%");
    }

    public static Specification<Task> and(Specification<Task> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<Task> and(Iterable<Specification<Task>> specifications) {
        int index = 0;
        Iterator<Specification<Task>> iterator = specifications.iterator();
        Specification<Task> result = null;
        while(iterator.hasNext()) {
            if (index == 0) {
                result = Specification.where(iterator.next());
            } else {
                result = result.and(iterator.next());
            }
            index++;
        }
        return result;
    }

    public static Specification<Task> or(Specification<Task> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<Task> or(Iterable<Specification<Task>> specifications) {
        int index = 0;
        Iterator<Specification<Task>> iterator = specifications.iterator();
        Specification<Task> result = null;
        while(iterator.hasNext()) {
            if (index == 0) {
                result = Specification.where(iterator.next());
            } else {
                result = result.or(iterator.next());
            }
            index++;
        }
        return result;
    }
}
