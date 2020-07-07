package com.intgroup.htmlcheck.feature.telegram;

import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.feature.telegram.service.handle.CallbackQueryHandleService;
import com.intgroup.htmlcheck.feature.telegram.service.handle.TextMessageHandleService;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TelegramBot extends TelegramLongPollingCommandBot {
    private String username, token;

    private CallbackQueryHandleService callbackQueryHandleService;

    private TextMessageHandleService textMessageHandleService;

    private TelegramUserService telegramUserService;

    public TelegramBot(String username, String token, ApplicationContext context) {
        super((DefaultBotOptions) ApiContext.getInstance(DefaultBotOptions.class), false);

        this.username = username;
        this.token = token;

        callbackQueryHandleService = context.getBean(CallbackQueryHandleService.class);
        textMessageHandleService = context.getBean(TextMessageHandleService.class);
        telegramUserService = context.getBean(TelegramUserService.class);

        register(new StartCommand(context, this));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            textMessageHandleService.handle(update);
        } else if (update.hasCallbackQuery()) {
            callbackQueryHandleService.handle(update);
        }
    }

    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            handleSendException(message.getChatId(), e);
        }
    }

    public void send(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            handleSendException(photo.getChatId(), e);
        }
    }

    public void send(SendDocument document) {
        try {
            execute(document);
        } catch (TelegramApiException e) {
            handleSendException(document.getChatId(), e);
        }
    }

    private void handleSendException(String chatId, Exception exception) {
        if (isUserBlockedBot(exception)) {
            telegramUserService.userBlockedBot(chatId);
        } else {
            System.out.println("Unknown send error to chat id " + chatId);
            exception.printStackTrace();
        }
    }

    private boolean isUserBlockedBot(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        return exceptionAsString.contains("Forbidden: bot was blocked by the user");
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
