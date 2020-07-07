package com.intgroup.htmlcheck.feature.telegram.service.handle;

import com.intgroup.htmlcheck.feature.mq.*;
import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
public class CallbackQueryHandleService {
    public static final boolean USE_RU_ONLY = true;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private EventDrivenMessageService eventDrivenMessageService;

    @Autowired
    private EventHandleService eventHandleService;

    @Autowired
    private TelegramBotService telegramBotService;

    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        String callbackQuery = update.getCallbackQuery().getData();
        long telegramUserId = update.getCallbackQuery().getFrom().getId();

        TelegramUser telegramUser = telegramUserService.getById(telegramUserId);
        if (telegramUser == null) {
            return;
        }

        List<EventDrivenMessage> eventDrivenMessages = eventDrivenMessageService.queryAll(EventDrivenMessageSpecifications.and(
                EventDrivenMessageSpecifications.eventIs(Event.userPressInlineButton),
                EventDrivenMessageSpecifications.userButtonPayloadIs(callbackQuery),
                EventDrivenMessageSpecifications.tagIs(telegramUser.getTag())
        ));

        if (eventDrivenMessages.size() > 0) {
            eventDrivenMessages.forEach(message -> {
                eventHandleService.eventHappened(Event.userPressInlineButton, "telegramUser", telegramUser, "message", message, "buttonPayload", callbackQuery);
            });
        } else {
            handleUnmatchedQuery(callbackQuery, telegramUser);
        }
    }

    private void handleUnmatchedQuery(String payload, TelegramUser telegramUser) {
        if (payload.startsWith("set_daily_notify_hour")) {
            handleSetDailyNotifyHour(payload, telegramUser);
        } else if (payload.startsWith("set_language")) {
            handleSetLanguage(payload, telegramUser);
        }
    }

    private void handleSetDailyNotifyHour(String payload, TelegramUser telegramUser) {
        String[] parts = payload.split("_");
        int hour = Integer.parseInt(parts[parts.length - 1]);

        telegramUser.setDailyNotifyHour(hour);
        telegramUserService.save(telegramUser);

        String confirmMessage = "";
        String languageCode = telegramUser.getLanguageCode();
        if (USE_RU_ONLY) {
            languageCode = "ru";
        }
        switch (languageCode) {
            case "ru":
                confirmMessage = "Спасибо, сохранили время напоминания в " + hour;
                break;
            case "ua":
            default:
                confirmMessage = "Дякуємо, зберегли час нагадування в " + hour;
                break;
        }

        telegramBotService.sendSimpleTextMessage(telegramUser, confirmMessage);
    }

    private void handleSetLanguage(String payload, TelegramUser telegramUser) {
        String[] parts = payload.split("_");
        String languageCode = parts[parts.length - 1];

        telegramUser.setLanguageCode(languageCode);
        telegramUserService.save(telegramUser);

        String confirmMessage = "";
        switch (languageCode) {
            case "ru":
                confirmMessage = "Спасибо, ваш язык в системе - русский";
                break;
            case "ua":
            default:
                confirmMessage = "Дякуємо, тепер ваша мова в системі - українська";
                break;
        }

        telegramBotService.sendSimpleTextMessage(telegramUser, confirmMessage);
    }
}
