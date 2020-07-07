package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.block.CheckTaskBlockService;
import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.mq.enums.KeyboardType;
import com.intgroup.htmlcheck.feature.mq.settings.SettingService;
import com.intgroup.htmlcheck.feature.mq.settings.SettingSpecifications;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.feature.telegram.service.payload.TelegramMessagePayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventHandleService {
    @Autowired
    private EventDrivenMessageService eventDrivenMessageService;

    @Autowired
    private TemplateProcessService templateProcessService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private CheckTaskBlockService checkTaskBlockService;

    @Autowired
    private SettingService settingService;
    
    public void eventHappened(Event event, Object ... keyValues) {
        Map<String, Object> data = new HashMap<>();
        for(int i = 0; i < keyValues.length; i += 2) {
            String key = keyValues[i].toString();
            Object value = keyValues[i+1];

            data.put(key, value);
        }

        eventHappened(event, data);
    }

    public void eventHappened(Event event, Map<String, Object> data) {
        List<EventDrivenMessage> eventDrivenMessages = getEventDrivenMessages(event, data);
        if (eventDrivenMessages.isEmpty()) {
            return;
        }

        //Load global settings
        settingService.queryAll(SettingSpecifications.and(
                SettingSpecifications.nameNotEmpty(),
                SettingSpecifications.dateAndTagAreEmpty()
        )).forEach(setting -> {
            data.put(setting.getFullName(), setting.getValue());
        });
        
        switch (event) {
            case newUserSubscribed:
            case scheduledTime:
            case userWrote:
            case userPressInlineButton:
            case dailyNotifyHour:
            case concreteDate:
                TelegramUser telegramUser = (TelegramUser) data.getOrDefault("telegramUser", null);
                if (telegramUser == null) {
                    break;
                }

                //Add referral link
                String refLink = "https://t.me/" + telegramBotService.getBotName() + "?start=FROM-" + telegramUser.getUserId();
                data.put("refLink", refLink);

                        //Load user-specific settings
                settingService.queryAll(SettingSpecifications.and(
                        SettingSpecifications.nameNotEmpty(),
                        SettingSpecifications.or(
                                SettingSpecifications.tagIs(telegramUser.getTag()),
                                SettingSpecifications.dateIs(telegramUser.getEventDate())
                        )
                )).forEach(setting -> {
                    data.put(setting.getFullName(), setting.getValue());
                });

                User innerUser = telegramUserService.getRelatedInnerUser(telegramUser);
                if (innerUser != null) {
                    Map<Integer, Integer> userProgress = checkTaskBlockService.getUserBlockProgress(innerUser);
                    userProgress.forEach((block, percent) -> {
                        data.put("block" + block + "Solved", percent == 100);

                        String frontendUrl = data.getOrDefault("frontendUrlTag", "https://gomarathon.com.ua").toString();
                        String token = innerUser.getToken();
                        String blockHash = checkTaskBlockService.getHashByBlockIndex(block);
                        String blockLink =   frontendUrl + "?token=" + token + "&block=" + blockHash;
                        data.put("block" + block + "Link", blockLink);

                        data.put("block" + block + "Progress", percent);
                    });

                    //Calculate inactivity time
                    LocalDateTime userLastActiveTime = innerUser.getLastActionTime();
                    if (userLastActiveTime == null) { //User not interacted at all
                        data.put("userInactiveTimeInHours", Integer.MAX_VALUE);
                    } else {
                        Duration duration = Duration.between(userLastActiveTime, LocalDateTime.now());
                        data.put("userInactiveTimeInHours", duration.toHours());
                    }

                    //Calculate total register time in hours
                    LocalDateTime totalRegisterTime = telegramUser.getRegisterDate();
                    if (totalRegisterTime == null) {
                        data.put("userTotalRegisterTimeInHours", Integer.MAX_VALUE);
                    } else {
                        Duration duration = Duration.between(totalRegisterTime, LocalDateTime.now());
                        data.put("userTotalRegisterTimeInHours", duration.toHours());
                    }
                }

                //Debug all vars
                try {
                    String allVars = data.toString();
                    data.put("allVars", allVars);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                eventDrivenMessages.forEach(eventDrivenMessage -> {
                    String processedText = templateProcessService.process(eventDrivenMessage.getText(), data);
                    if (!processedText.isBlank()) {

                        List<TelegramMessagePayload> payloadList = new ArrayList<>();
                        if (eventDrivenMessage.getInlineKeyboard() != null && !eventDrivenMessage.getInlineKeyboard().isBlank()) {
                            //Preprocess inline keyboard - populate with variables
                            String inlineKeyboardText = eventDrivenMessage.getInlineKeyboard();
                            
                            for(String name: data.keySet()) {
                                String varName = "${" + name + "}";
                                inlineKeyboardText = inlineKeyboardText.replace(varName, data.getOrDefault(name, "").toString());
                            }

                            if (eventDrivenMessage.getKeyboardType() == KeyboardType.bottom) {
                                payloadList.add(TelegramMessagePayload.createBottomKeyboard(inlineKeyboardText));
                            } else if (eventDrivenMessage.getKeyboardType() == KeyboardType.inline || eventDrivenMessage.getKeyboardType() == null) {
                                payloadList.add(TelegramMessagePayload.createInlineKeyboard(inlineKeyboardText));
                            }
                        }
                        TelegramMessagePayload[] payloadArray = new TelegramMessagePayload[payloadList.size()];
                        for(int i = 0; i < payloadArray.length; i++) {
                            payloadArray[i] = payloadList.get(i);
                        }

                        telegramBotService.sendSimpleTextMessage(telegramUser, processedText, payloadArray);
                    }
                });
                break;
        }
    }

    private List<EventDrivenMessage> getEventDrivenMessages(Event event, Map<String, Object> data) {
        List<EventDrivenMessage> result = new ArrayList<>();

        switch (event) {
            case newUserSubscribed:
                TelegramUser telegramUser = (TelegramUser) data.getOrDefault("telegramUser", null);
                if (telegramUser != null) {
                    result.addAll(eventDrivenMessageService.queryAll(
                            EventDrivenMessageSpecifications.and(
                                    EventDrivenMessageSpecifications.eventIs(event), EventDrivenMessageSpecifications.tagIs(telegramUser.getTag())
                            )
                    ));
                }

                break;
            case scheduledTime:
            case userWrote:
            case userPressInlineButton:
            case dailyNotifyHour:
            case concreteDate:
                EventDrivenMessage message = (EventDrivenMessage) data.getOrDefault("message", null);
                if (message != null) {
                    result.add(message);
                }
                break;
        }

        result.sort(Comparator.comparingInt(EventDrivenMessage::getPriority));

        return result;
    }

    public static void main(String[] args) {
        Map<String, String> result = new HashMap<>();
        result.put("a", "b");
        result.put("b", "c");

        System.out.println(result.toString());

    }
}
