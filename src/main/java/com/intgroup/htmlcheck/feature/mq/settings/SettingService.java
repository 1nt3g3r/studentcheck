package com.intgroup.htmlcheck.feature.mq.settings;

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
public class SettingService {
    @Autowired
    private SettingRepository settingRepository;

    public void delete(Specification<Setting> specification) {
        settingRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<Setting> specification) {
        return (int) settingRepository.count(specification);
    }

    public List<Setting> queryAll(Specification<Setting> specification) {
        return settingRepository.findAll(specification);
    }

    public List<Setting> queryPage(int pageIndex, Specification<Setting> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<Setting> page = settingRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public void deleteById(long id) {
        if (settingRepository.existsById(id)) {
            settingRepository.deleteById(id);
        }
    }

    public Setting getById(long id) {
        if (settingRepository.existsById(id)) {
            return settingRepository.getOne(id);
        }

        return null;
    }

    public void save(Setting setting) {
        settingRepository.save(setting);
    }
}
