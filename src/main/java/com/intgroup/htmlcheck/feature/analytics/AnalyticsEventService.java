package com.intgroup.htmlcheck.feature.analytics;

import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsEventService {
    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    public void delete(Specification<AnalyticsEvent> specification) {
        analyticsEventRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<AnalyticsEvent> specification) {
        return (int) analyticsEventRepository.count(specification);
    }

    public List<AnalyticsEvent> queryAll(Specification<AnalyticsEvent> specification) {
        return analyticsEventRepository.findAll(specification);
    }

    public List<AnalyticsEvent> queryPage(int pageIndex, Specification<AnalyticsEvent> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<AnalyticsEvent> page = analyticsEventRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public void deleteById(long id) {
        if (analyticsEventRepository.existsById(id)) {
            analyticsEventRepository.deleteById(id);
        }
    }

    public AnalyticsEvent getById(long id) {
        if (analyticsEventRepository.existsById(id)) {
            return analyticsEventRepository.getOne(id);
        }

        return null;
    }

    public void save(AnalyticsEvent msg) {
        analyticsEventRepository.save(msg);
    }
}
