package com.intgroup.htmlcheck.feature.mq;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDrivenMessageRepository extends JpaRepository<EventDrivenMessage, Long>, JpaSpecificationExecutor<EventDrivenMessage> {
}
