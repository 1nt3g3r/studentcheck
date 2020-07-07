package com.intgroup.htmlcheck.feature.taskstat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTaskStatRepository extends JpaRepository<UserTaskStat, Long>, JpaSpecificationExecutor<UserTaskStat> {
}
