package com.intgroup.htmlcheck.feature.telegram.service;

import com.intgroup.htmlcheck.feature.bgtask.BgTaskService;
import com.intgroup.htmlcheck.feature.telegram.TelegramBot;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessageService;
import com.intgroup.htmlcheck.feature.telegram.service.payload.TelegramMessagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;

@Service
public class TelegramBotService {
    @Autowired
    private ApplicationContext context;

    private TelegramBot bot;

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private BgTaskService bgTaskService;

    @Autowired
    private InstantMessageService instantMessageService;

    public String getBotName() {
        return botName;
    }

    @PostConstruct
    public void init() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            bot = new TelegramBot(botName, botToken, context);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public TelegramBot getBot() {
        return bot;
    }

    public void sendSimpleTextMessage(TelegramUser telegramUser, String text, TelegramMessagePayload ... payloads) {
        send(telegramUser, createText(text), payloads);
    }

    public SendMessage createText(String text) {
        return new SendMessage().enableMarkdown(true).setText(text);
    }

    public void send(TelegramUser telegramUser, SendMessage message, TelegramMessagePayload ... payloads) {
        message.setChatId(telegramUser.getChatId());

        for(TelegramMessagePayload payload: payloads) {
            payload.apply(message);
        }

        instantMessageService.addMessageToUser(telegramUser.getUserId(), message.getText());

        bgTaskService.submitTask("Send message for Telegram User", 2, () -> bot.send(message), true);
    }

    public void send(TelegramUser telegramUser, SendDocument document) {
        document.setChatId(telegramUser.getChatId());

        instantMessageService.addMessageFromUser(telegramUser.getUserId(), "Send document");

        bgTaskService.submitTask("Send document for Telegram User", 2, () -> bot.send(document), true);
    }
}
