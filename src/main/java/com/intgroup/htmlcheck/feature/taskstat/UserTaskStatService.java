package com.intgroup.htmlcheck.feature.taskstat;

import com.intgroup.htmlcheck.domain.security.User;
import groovy.lang.GroovyShell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserTaskStatService {
    @Autowired
    private UserTaskStatRepository statRepository;

    public List<String> getPassedTasks(User user) {
        return getPassedTasks(user.getEmail());
    }

    public List<String> getPassedTasks(String innerUserEmail) {
        List<UserTaskStat> userTaskStats = queryAll(UserTaskStatSpecifications.userIdIs(innerUserEmail));

        List<String> result = new ArrayList<>();
        userTaskStats.forEach(userTaskStat -> {
            if (userTaskStat.isSolved()) {
                result.add(userTaskStat.getTaskId());
            }
        });

        return result;
    }

    public List<UserTaskStat> queryAll(Specification<UserTaskStat> specification) {
        return statRepository.findAll(specification);
    }

    public UserTaskStat getOrCreate(String userId, String taskId) {
        List<UserTaskStat> stats = statRepository.findAll(UserTaskStatSpecifications.and(
                UserTaskStatSpecifications.userIdIs(userId),
                UserTaskStatSpecifications.taskIdIs(taskId)
        ));

        if (stats.size() == 1) {
            return stats.get(0);
        }

        if (stats.size() >= 2) {
            statRepository.deleteAll(stats);
        }

        UserTaskStat result = new UserTaskStat();
        result.setUserId(userId);
        result.setTaskId(taskId);

        statRepository.save(result);

        return result;
    }

    public void save(UserTaskStat stat) {
        List<UserTaskStat> stats = statRepository.findAll(UserTaskStatSpecifications.and(
                UserTaskStatSpecifications.userIdIs(stat.getUserId()),
                UserTaskStatSpecifications.taskIdIs(stat.getTaskId())
        ));

        //Rare case when two concurrent requests save few copies of task stat
        if (stats.size() >= 2) {
            statRepository.deleteAll(stats);

            //Reinit task stat
            UserTaskStat copy = new UserTaskStat();
            copy.setUserId(stat.getUserId());
            copy.setTaskId(stat.getTaskId());
            copy.setSolveTimeSeconds(stat.getSolveTimeSeconds());
            copy.setSolveTryCount(stat.getSolveTryCount());
            copy.setSolved(stat.isSolved());
            copy.setSolveDateTime(stat.getSolveDateTime());

            stat = copy;
        }

        statRepository.save(stat);
    }
}
