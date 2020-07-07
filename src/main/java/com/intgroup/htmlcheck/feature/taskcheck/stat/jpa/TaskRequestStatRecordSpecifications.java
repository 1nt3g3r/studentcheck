package com.intgroup.htmlcheck.feature.taskcheck.stat.jpa;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;

public class TaskRequestStatRecordSpecifications {
    public static Specification<TaskRequestStatRecord> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<TaskRequestStatRecord> dateLowerOrEqual(LocalDateTime dateTime) {
        return (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("dateTime"), dateTime);
    }

    public static Specification<TaskRequestStatRecord> dateGreaterOrEqual(LocalDateTime dateTime) {
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("dateTime"), dateTime);
    }

    public static Specification<TaskRequestStatRecord> and(Specification<TaskRequestStatRecord> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<TaskRequestStatRecord> and(Iterable<Specification<TaskRequestStatRecord>> specifications) {
        int index = 0;
        Iterator<Specification<TaskRequestStatRecord>> iterator = specifications.iterator();
        Specification<TaskRequestStatRecord> result = null;
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

    public static Specification<TaskRequestStatRecord> or(Specification<TaskRequestStatRecord> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<TaskRequestStatRecord> or(Iterable<Specification<TaskRequestStatRecord>> specifications) {
        int index = 0;
        Iterator<Specification<TaskRequestStatRecord>> iterator = specifications.iterator();
        Specification<TaskRequestStatRecord> result = null;
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
