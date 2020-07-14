package com.intgroup.htmlcheck.feature.analytics;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class AnalyticsEventSpecifications {
    public static Specification<AnalyticsEvent> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<AnalyticsEvent> idIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<AnalyticsEvent> and(Specification<AnalyticsEvent> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<AnalyticsEvent> and(Iterable<Specification<AnalyticsEvent>> specifications) {
        int index = 0;
        Iterator<Specification<AnalyticsEvent>> iterator = specifications.iterator();
        Specification<AnalyticsEvent> result = null;
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

    public static Specification<AnalyticsEvent> or(Specification<AnalyticsEvent> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<AnalyticsEvent> or(Iterable<Specification<AnalyticsEvent>> specifications) {
        int index = 0;
        Iterator<Specification<AnalyticsEvent>> iterator = specifications.iterator();
        Specification<AnalyticsEvent> result = null;
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
