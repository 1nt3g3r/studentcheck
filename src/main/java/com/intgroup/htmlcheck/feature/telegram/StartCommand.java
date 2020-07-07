package com.intgroup.htmlcheck.feature.telegram;

import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.mq.EventHandleService;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotPayloadParseService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.feature.telegram.userinfo.TelegramUserInfoService;
import com.intgroup.htmlcheck.service.util.NormalizeDataService;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Map;
import java.util.StringJoiner;

public class StartCommand extends BotCommand {
    private TelegramBot bot;

    private TelegramUserService telegramUserService;
    private TelegramBotPayloadParseService payloadParseService;
    private TelegramUserInfoService telegramUserInfoService;
    private EventHandleService eventHandleService;
    private TelegramBotService telegramBotService;
    private NormalizeDataService normalizeDataService;

    public StartCommand(ApplicationContext context, TelegramBot bot) {
        super("start", "Начать общение с ботом");

        telegramUserService = context.getBean(TelegramUserService.class);
        payloadParseService = context.getBean(TelegramBotPayloadParseService.class);
        telegramUserInfoService = context.getBean(TelegramUserInfoService.class);
        eventHandleService = context.getBean(EventHandleService.class);
        telegramBotService = context.getBean(TelegramBotService.class);
        normalizeDataService = context.getBean(NormalizeDataService.class);

        this.bot = bot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        //Save user if not saved
        TelegramUser telegramUser = telegramUserService.getById(user.getId());
        boolean newUser = telegramUser == null;

        if (telegramUser == null) {
            telegramUser = new TelegramUser();
            telegramUser.setUserId(user.getId());
        }

        telegramUser.setUserName(user.getUserName());
        telegramUser.setFirstName(user.getFirstName());
        telegramUser.setLastName(user.getLastName());
        telegramUser.setLanguageCode(user.getLanguageCode());
        telegramUser.setChatId(chat.getId().toString());

        boolean shouldNotifyAboutNewUser = false;

        //Apply payload only if it's brand new user
        if (newUser) {
            StringJoiner payload = new StringJoiner("");
            for(String payloadItem: strings) {
                payload.add(payloadItem);
            }

            //Check if user with this phone exists
            Map<String, String> payloadMap = payloadParseService.parsePayload(payload.toString());
            String phone = normalizeDataService.normalizePhone(payloadMap.getOrDefault("PH", ""));
            boolean phoneIsEmpty = phone == null || phone.isBlank();

            if (phoneIsEmpty || telegramUserService.getByPhoneNumber(phone) == null) {
                payloadParseService.applyPayload(payload.toString(), telegramUser);

                //Check if user has tag
                if (telegramUser.getTag() == null || telegramUser.getTag().isBlank()) {
                    telegramUser.setTag("UNKNOWN");
                }

                shouldNotifyAboutNewUser = true;
            } else {
                String message = "Пользователь с таким номером телефона уже зарегистрирован";
                telegramBotService.sendSimpleTextMessage(telegramUser, message);
            }
        }

        //Save user
        telegramUserService.save(telegramUser);

        //Update user
        telegramUserInfoService.updateUser(telegramUser.getPhone());

        //Last action - notify that new user here
        if (shouldNotifyAboutNewUser) {
            eventHandleService.eventHappened(Event.newUserSubscribed, "telegramUser", telegramUser);
        }
    }
}
