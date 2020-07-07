package com.intgroup.htmlcheck.feature.taskstat;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class UserTaskStatSpecifications {
    public static Specification<UserTaskStat> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }


    public static Specification<UserTaskStat> userIdInList(List<String> userIds) {
        return (root, cq, cb) -> root.get("userId").in(userIds);
    }

    public static Specification<UserTaskStat> userIdIs(String userId) {
        return (root, cq, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<UserTaskStat> taskIdIs(String taskId) {
        return (root, cq, cb) -> cb.equal(root.get("taskId"), taskId);
    }

    public static Specification<UserTaskStat> and(Specification<UserTaskStat> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<UserTaskStat> and(Iterable<Specification<UserTaskStat>> specifications) {
        int index = 0;
        Iterator<Specification<UserTaskStat>> iterator = specifications.iterator();
        Specification<UserTaskStat> result = null;
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

    public static Specification<UserTaskStat> or(Specification<UserTaskStat> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<UserTaskStat> or(Iterable<Specification<UserTaskStat>> specifications) {
        int index = 0;
        Iterator<Specification<UserTaskStat>> iterator = specifications.iterator();
        Specification<UserTaskStat> result = null;
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
