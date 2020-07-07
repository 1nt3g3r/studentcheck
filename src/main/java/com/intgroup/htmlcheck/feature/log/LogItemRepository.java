package com.intgroup.htmlcheck.feature.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LogItemRepository extends JpaRepository<LogItem, Long>, JpaSpecificationExecutor<LogItem> {
}
