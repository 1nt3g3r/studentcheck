package com.intgroup.htmlcheck.feature.telegram.service.handle;

import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.mq.EventDrivenMessageService;
import com.intgroup.htmlcheck.feature.mq.EventDrivenMessageSpecifications;
import com.intgroup.htmlcheck.feature.mq.EventHandleService;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessageService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.util.NormalizeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Pattern;

@Service
public class TextMessageHandleService {
    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private EventDrivenMessageService eventDrivenMessageService;

    @Autowired
    private EventHandleService eventHandleService;

    @Autowired
    private InstantMessageService instantMessageService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private NormalizeDataService normalizeDataService;

    public void handle(Update update) {
        String message = update.hasMessage() ? update.getMessage().getText() : "";

        long telegramUserId = update.getMessage().getFrom().getId();

        telegramUserService.userNotBlockedBot(telegramUserId);

        TelegramUser telegramUser = telegramUserService.getById(telegramUserId);
        if (telegramUser == null) {
            return;
        }

        instantMessageService.addMessageFromUser(telegramUserId, message);

        eventDrivenMessageService.queryAll(EventDrivenMessageSpecifications.and(
                EventDrivenMessageSpecifications.eventIs(Event.userWrote),
                EventDrivenMessageSpecifications.tagIs(telegramUser.getTag())
        )).forEach(eventDrivenMessage -> {
            String compareMessage = message;
            if (compareMessage == null) {
                compareMessage = "";
            }
            compareMessage = compareMessage.toLowerCase();
            Pattern pattern = Pattern.compile(eventDrivenMessage.getUserWroteMessagePattern().toLowerCase());
            if (pattern.matcher(compareMessage).matches()) {
                eventHandleService.eventHappened(Event.userWrote, "telegramUser", telegramUser, "message", eventDrivenMessage, "text", message);
            }
        });

        handleUnhandled(telegramUser, update, message);
    }

    private void handleUnhandled(TelegramUser telegramUser, Update update, String message) {
        if (message != null) {
            if (message.equals("unsubscribeMePlease")) {
                telegramBotService.sendSimpleTextMessage(telegramUser, "You unsubscribed!");

                //Wait 1 second and delete
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    telegramUserService.deleteById(telegramUser.getUserId());
                }).start();
            }
        }

        if (update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();

            String phone = contact.getPhoneNumber();
            String firstName = contact.getFirstName();
            String lastName = contact.getLastName();

            telegramUser.setPhone(normalizeDataService.normalizePhone(phone));
            telegramUser.setFirstName(firstName);
            telegramUser.setLastName(lastName);

            telegramUserService.save(telegramUser);
        }
    }
}
