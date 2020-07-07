package com.intgroup.htmlcheck.feature.telegram.im;

import com.intgroup.htmlcheck.service.util.PaginationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InstantMessageService {
    @Autowired
    private InstantMessageRepository instantMessageRepository;

    public int getUnseenMessageCount(long telegramUserId) {
        return instantMessageRepository.getUnseenMessageCount(telegramUserId);
    }

    public Map<Long, Long> getUnseenMessageCountForUsers(List<Long> telegramUserIds) {
        Map<Long, Long> result = new HashMap<>();

        if (telegramUserIds.isEmpty()) {
            return result;
        }

        long[][] queryResult = instantMessageRepository.getUnseenMessageCountForUsers(telegramUserIds);
        for(long[] row: queryResult) {
            result.put(row[0], row[1]);
        }

        return result;
    }

    public List<Long> getTelegramUserIdsWithUnseenMessages() {
        return instantMessageRepository.getTelegramUserIdsWithUnseenMessages();
    }

    public void delete(Specification<InstantMessage> specification) {
        instantMessageRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<InstantMessage> specification) {
        return (int) instantMessageRepository.count(specification);
    }

    public List<InstantMessage> queryAll(Specification<InstantMessage> specification) {
        return instantMessageRepository.findAll(specification);
    }

    public List<InstantMessage> queryPage(int pageIndex, Specification<InstantMessage> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<InstantMessage> page = instantMessageRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public void deleteById(long id) {
        if (instantMessageRepository.existsById(id)) {
            instantMessageRepository.deleteById(id);
        }
    }

    public InstantMessage getById(long id) {
        if (instantMessageRepository.existsById(id)) {
            return instantMessageRepository.getOne(id);
        }

        return null;
    }

    public void save(InstantMessage instantMessage) {
        instantMessageRepository.save(instantMessage);
    }

    public void addMessageToUser(long telegramUserId, String message) {
        InstantMessage instantMessage = new InstantMessage();

        instantMessage.setSeen(true);
        instantMessage.setMessageType(InstantMessageType.toUser);
        instantMessage.setMessage(message);
        instantMessage.setTelegramUserId(telegramUserId);

        save(instantMessage);
    }

    public void addMessageFromUser(long telegramUserId, String message) {
        InstantMessage instantMessage = new InstantMessage();

        instantMessage.setSeen(false);
        instantMessage.setMessageType(InstantMessageType.fromUser);
        instantMessage.setMessage(message);
        instantMessage.setTelegramUserId(telegramUserId);

        save(instantMessage);
    }
}
