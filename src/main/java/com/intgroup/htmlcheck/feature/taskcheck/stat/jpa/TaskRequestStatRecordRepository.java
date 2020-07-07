package com.intgroup.htmlcheck.feature.taskcheck.stat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRequestStatRecordRepository extends JpaRepository<TaskRequestStatRecord, Long>, JpaSpecificationExecutor<TaskRequestStatRecord> {
}
