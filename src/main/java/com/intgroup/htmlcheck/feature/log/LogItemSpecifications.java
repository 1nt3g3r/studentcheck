package com.intgroup.htmlcheck.feature.log;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class LogItemSpecifications {
    public static Specification<LogItem> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<LogItem> tagOrValueLike(String searchString) {
        return (root, cq, cb) ->
          cb.or(
                  cb.like(root.get("tag"), searchString),
                  cb.like(root.get("value"), searchString)
          );
    }

    public static Specification<LogItem> and(Specification<LogItem> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<LogItem> and(Iterable<Specification<LogItem>> specifications) {
        int index = 0;
        Iterator<Specification<LogItem>> iterator = specifications.iterator();
        Specification<LogItem> result = null;
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

    public static Specification<LogItem> or(Specification<LogItem> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<LogItem> or(Iterable<Specification<LogItem>> specifications) {
        int index = 0;
        Iterator<Specification<LogItem>> iterator = specifications.iterator();
        Specification<LogItem> result = null;
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
