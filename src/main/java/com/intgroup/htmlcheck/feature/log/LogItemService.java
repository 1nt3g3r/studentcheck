package com.intgroup.htmlcheck.feature.log;

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
public class LogItemService {
    @Autowired
    private LogItemRepository logItemRepository;

    public void log(String tag, String value) {
        LogItem logItem = new LogItem();
        logItem.setTag(tag);
        logItem.setValue(value);
        logItemRepository.save(logItem);
    }

    public List<LogItem> queryAll(Specification<LogItem> specification) {
        return logItemRepository.findAll(specification);
    }

    public List<LogItem> queryPage(int pageIndex, Specification<LogItem> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "time"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<LogItem> page = logItemRepository.findAll(specification, pageable);
        return page.getContent();
    }


    public int count(Specification<LogItem> specification) {
        return (int) logItemRepository.count(specification);
    }
}
