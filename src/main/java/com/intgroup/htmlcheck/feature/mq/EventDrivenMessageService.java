package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventDrivenMessageService {
    @Autowired
    private EventDrivenMessageRepository eventDrivenMessageRepository;

    public void delete(Specification<EventDrivenMessage> specification) {
        eventDrivenMessageRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<EventDrivenMessage> specification) {
        return (int) eventDrivenMessageRepository.count(specification);
    }

    public List<EventDrivenMessage> queryAll(Specification<EventDrivenMessage> specification) {
        return eventDrivenMessageRepository.findAll(specification);
    }

    public List<EventDrivenMessage> queryPage(int pageIndex, Specification<EventDrivenMessage> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<EventDrivenMessage> page = eventDrivenMessageRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public void deleteById(long id) {
        if (eventDrivenMessageRepository.existsById(id)) {
            eventDrivenMessageRepository.deleteById(id);
        }
    }

    public EventDrivenMessage getById(long id) {
        if (eventDrivenMessageRepository.existsById(id)) {
            return eventDrivenMessageRepository.getOne(id);
        }

        return null;
    }

    public void save(EventDrivenMessage msg) {
        eventDrivenMessageRepository.save(msg);
    }
}
