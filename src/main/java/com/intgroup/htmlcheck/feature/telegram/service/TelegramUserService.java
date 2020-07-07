package com.intgroup.htmlcheck.feature.telegram.service;

import com.intgroup.htmlcheck.feature.error.ErrorService;
import com.intgroup.htmlcheck.feature.mq.*;
import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserRepository;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessageService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TelegramUserService {
    @Autowired
    private TelegramUserRepository telegramUserRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EventDrivenMessageService eventDrivenMessageService;

    @Autowired
    private EventHandleService eventHandleService;

    @Autowired
    private ErrorService errorService;

    @Autowired
    private InstantMessageService instantMessageService;

    private Set<Long> usersThatBlockedBot;

    private boolean updateEnabled = true;

    private int lastMessageUpdateExecuteMinute = -1;

    @PostConstruct
    public void update() {
        usersThatBlockedBot = new HashSet<>();
    }

    public void userBlockedBot(String chatId) {
        TelegramUser telegramUser = queryFirstOrNull(TelegramUserSpecifications.chatIdIs(chatId));

        if (telegramUser == null) {
            return;
        }

        usersThatBlockedBot.add(telegramUser.getUserId());
    }

    public void userNotBlockedBot(long userId) {
        usersThatBlockedBot.remove(userId);
    }

    public boolean isUserBlockedBot(long telegramUserId) {
        return usersThatBlockedBot.contains(telegramUserId);
    }

    public List<Long> getUsersThatBlockedBot() {
        return new ArrayList<>(usersThatBlockedBot);
    }

    public List<TelegramUser> getWithUnseenMessages(int maxCount) {
        List<Long> userIds = instantMessageService.getTelegramUserIdsWithUnseenMessages();

        if (userIds.size() <= 0) {
            return new ArrayList<>();
        }

        if (userIds.size() > maxCount) {
            userIds = userIds.subList(0, maxCount);
        }

        return telegramUserRepository.getAllWithIds(userIds);
    }

    public List<String> getUniqueTags() {
        return telegramUserRepository.getUniqueTags().stream().filter(tag -> tag != null && !tag.isBlank()).collect(Collectors.toList());
    }

    public List<String> getUniqueEventDates() {
        return telegramUserRepository.getUniqueEventDates().stream().filter(date -> date != null && !date.isBlank()).collect(Collectors.toList());
    }

    public TelegramUser getByPhoneNumber(String phoneNumber) {
        List<TelegramUser> users = queryPage(0, TelegramUserSpecifications.phoneIs(phoneNumber));

        if (users.size() >= 1) {
            return users.get(0);
        }

        return null;
    }

    public TelegramUser getById(long id) {
        if (telegramUserRepository.existsById(id)) {
            return telegramUserRepository.getOne(id);
        }

        return null;
    }

    public void deleteById(long id) {
        if (telegramUserRepository.existsById(id)) {
            //Delete related innner user, if it present
            try {
                TelegramUser user = getById(id);

                com.intgroup.htmlcheck.domain.security.User innerUser = getRelatedInnerUser(user);
                if (innerUser != null) {
                    userService.removeTokenFromCache(innerUser.getToken());
                    userService.deleteUserById(innerUser.getId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //Delete Telegram user
            telegramUserRepository.deleteById(id);
        }
    }

    public void save(TelegramUser user) {
        telegramUserRepository.save(user);

        String innerUserEmail = getUserInnerEmail(user);
        if (userService.findUserByEmail(innerUserEmail) == null) {
            try {
                String password = "TelegramUser" + Float.toString(new Random().nextFloat()).hashCode();

                com.intgroup.htmlcheck.domain.security.User innerUser = new com.intgroup.htmlcheck.domain.security.User();
                innerUser.setEmail(innerUserEmail);

                String innerUserName = user.getFirstName();
                if (innerUserName == null) {
                    innerUserName = "";
                }
                innerUser.setName(innerUserName);

                String innerUserLastName = user.getLastName();
                if (innerUserLastName == null) {
                    innerUserLastName = "";
                }
                innerUser.setLastName(innerUserLastName);

                innerUser.setToken(userService.generateToken());
                innerUser.setPassword(password);

                userService.saveUser(innerUser);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public com.intgroup.htmlcheck.domain.security.User getRelatedInnerUser(TelegramUser user) {
        return userService.findUserByEmail(getUserInnerEmail(user));
    }

    public String getUserInnerEmail(TelegramUser user) {
        String emailSuffix = "@telegram-user";

        //Step 1 - search if user with phone exists - backward compatibility
        String phone = user.getPhone();
        if (phone != null && !phone.isBlank()) {
            String phoneBasedEmail = phone + emailSuffix;
            if (userService.userWithEmailExists(phoneBasedEmail)) {
                return phoneBasedEmail;
            }
        }

        //Step 2 - return id based on telegram user id
        return user.getUserId() + emailSuffix;
    }

    public void delete(Specification<TelegramUser> specification) {
        telegramUserRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<TelegramUser> specification) {
        return (int) telegramUserRepository.count(specification);
    }

    public TelegramUser getFirstOrNull(Specification<TelegramUser> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "userId"));
        Pageable pageable = PageRequest.of(0, 1, sort);
        Page<TelegramUser> page = telegramUserRepository.findAll(specification, pageable);
        List<TelegramUser> users = page.getContent();

        return users.size() == 0 ? null : users.get(0);
    }

    public TelegramUser queryFirstOrNull(Specification<TelegramUser> specification) {
        List<TelegramUser> users = queryAll(specification);

        if (users.size() == 0) {
            return null;
        }

        return users.get(0);
    }

    public List<TelegramUser> queryAll(Specification<TelegramUser> specification) {
        return telegramUserRepository.findAll(specification);
    }

    public List<TelegramUser> queryPage(int pageIndex, Specification<TelegramUser> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "userId"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<TelegramUser> page = telegramUserRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public TelegramUser getOrCreateByUpdate(Update update) {
        long userId = getUserId(update);

        if (userId < 0) {
            return null;
        }

        boolean userChanged = false;

        TelegramUser user = getById(userId);
        if (user == null) {
            user = new TelegramUser();
            user.setUserId(userId);

            userChanged = true;
        }

        //Save additional info
        User tgUser = update.hasMessage() ? update.getMessage().getFrom() : update.getCallbackQuery().getFrom();

        //Right now we don't work with bots
        if (tgUser.getBot()) {
            return null;
        }

        boolean firstNameIsEmpty = tgUser.getFirstName() == null || tgUser.getFirstName().isEmpty();
        if (firstNameIsEmpty && shouldUpdate(tgUser.getFirstName(), user.getFirstName())) {
            userChanged = true;
            user.setFirstName(tgUser.getFirstName());
        }

        boolean lastNameIsEmpty = tgUser.getLastName() == null || tgUser.getLastName().isEmpty();
        if (lastNameIsEmpty && shouldUpdate(tgUser.getLastName(), user.getLastName())) {
            userChanged = true;
            user.setLastName(tgUser.getLastName());
        }

        if (shouldUpdate(tgUser.getLanguageCode(), user.getLanguageCode())) {
            userChanged = true;
            user.setLanguageCode(tgUser.getLanguageCode());
        }

        if (shouldUpdate(tgUser.getUserName(), user.getUserName())) {
            userChanged = true;
            user.setUserName(tgUser.getUserName());
        }

        if (update.hasMessage()) {
            String chatId = update.getMessage().getChatId().toString();
            if (shouldUpdate(chatId, user.getChatId())) {
                user.setChatId(chatId);
                userChanged = true;
            }
        }

        if (userChanged) {
            save(user);
        }

        return user;
    }

    private boolean shouldUpdate(String value, String oldValue) {
        return value != null && !value.isBlank() && !value.equals(oldValue);
    }

    public long getUserId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        } else {
            return -1;
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void each5MinutesUpdate() {
        if (!updateEnabled) {
            return;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"));

        callEventDrivenMessageMinuteUpdate(calendar);
    }

    public void callEventDrivenMessageMinuteUpdate(Calendar calendar) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        boolean shouldUpdate = currentMinute % 5 == 0;

        if (!shouldUpdate) {
            //No chance for more checks
            if (lastMessageUpdateExecuteMinute < 0) {
                return;
            }

            int minuteDiff = currentMinute % 5;
            int expectedLastMinute = currentMinute - minuteDiff;
            if (lastMessageUpdateExecuteMinute != expectedLastMinute) {
                shouldUpdate = true;
                lastMessageUpdateExecuteMinute = expectedLastMinute;

                errorService.add("Ошибка 5-минутного обновления сообщений в " + currentHour + ":" + lastMessageUpdateExecuteMinute + ", запущено в " + currentHour + ":" + currentMinute);
            }
        }

        //Accept only 5, 10, 15, etc minutes
        if (!shouldUpdate) {
            return;
        }

        if (currentMinute%5 == 0) {
            lastMessageUpdateExecuteMinute = currentMinute;
        } else {
            System.out.println("Fix current minute");
            currentMinute = lastMessageUpdateExecuteMinute;
        }

        //Select all related events
        List<EventDrivenMessage> timeMessages = eventDrivenMessageService.queryAll(EventDrivenMessageSpecifications.and(
                EventDrivenMessageSpecifications.eventIs(Event.scheduledTime),
                EventDrivenMessageSpecifications.hourIs(currentHour),
                EventDrivenMessageSpecifications.minuteIs(currentMinute)
        ));

        //Order messages by priority. Messages with lower priority number will send first
        timeMessages.sort(Comparator.comparingInt(EventDrivenMessage::getPriority));

        //For each message get related users
        timeMessages.forEach(message -> {
            String tag = message.getTag();
            String expectedUserDate = LocalDate.now().minusDays(message.getDay()).format(DateTimeFormatter.ISO_LOCAL_DATE);

            //Select all users with this tag
            List<TelegramUser> usersWithTag = queryAll(TelegramUserSpecifications.and(
                    TelegramUserSpecifications.tagIs(tag),
                    TelegramUserSpecifications.eventDateIs(expectedUserDate)
            ));

            //Notify each user
            usersWithTag.forEach(telegramUser -> {
                eventHandleService.eventHappened(Event.scheduledTime, "telegramUser", telegramUser, "message", message);
            });
        });


        //Search for concrete date
        String todayDate = getLocalDateNow();
        List<EventDrivenMessage> concreteDateTimeMessages = eventDrivenMessageService.queryAll(EventDrivenMessageSpecifications.and(
                EventDrivenMessageSpecifications.eventIs(Event.concreteDate),
                EventDrivenMessageSpecifications.concreteDateIs(todayDate),
                EventDrivenMessageSpecifications.hourIs(currentHour),
                EventDrivenMessageSpecifications.minuteIs(currentMinute)
        ));
        //Order messages by priority. Messages with lower priority number will send first
        concreteDateTimeMessages.sort(Comparator.comparingInt(EventDrivenMessage::getPriority));
        //For each message get related users
        concreteDateTimeMessages.forEach(message -> {
            String tag = message.getTag();

            //Select all users with this tag
            List<TelegramUser> usersWithTag = queryAll(TelegramUserSpecifications.tagIs(tag));

            //Notify each user
            usersWithTag.forEach(telegramUser -> {
                eventHandleService.eventHappened(Event.concreteDate, "telegramUser", telegramUser, "message", message);
            });
        });

        //Update daily updates for each user
        if (currentMinute == 0) {
            //Select all messages
            List<EventDrivenMessage> dailyMessages = eventDrivenMessageService.queryAll(EventDrivenMessageSpecifications.eventIs(Event.dailyNotifyHour));

            List<String> tags = new ArrayList<>();
            dailyMessages.forEach(message -> {
                String tag = message.getTag();
                if (tag != null && !tag.isBlank() && !tags.contains(tag)) {
                    tags.add(tag);
                }
            });
            List<Specification<TelegramUser>> tagSpecifications = new ArrayList<>();
            tags.forEach(tag -> tagSpecifications.add(TelegramUserSpecifications.tagIs(tag)));

            //Select all related users
            List<TelegramUser> telegramUsers = queryAll(TelegramUserSpecifications.and(
                    TelegramUserSpecifications.dailyNotifyHourIs(currentHour),
                    TelegramUserSpecifications.or(tagSpecifications)
            ));

            //For each user find that messages
            telegramUsers.forEach(telegramUser -> {
                List<EventDrivenMessage> messagesForThisUser = new ArrayList<>();

                dailyMessages.forEach(message -> {
                    if (message.getTag().equals(telegramUser.getTag())) {
                        messagesForThisUser.add(message);
                    }
                });

                //Order messages by priority. Messages with lower priority number will send first
                messagesForThisUser.sort(Comparator.comparingInt(EventDrivenMessage::getPriority));

                messagesForThisUser.forEach(message -> {
                    eventHandleService.eventHappened(Event.dailyNotifyHour, "telegramUser", telegramUser, "message", message);
                });
            });
        }
    }

    private String getLocalDateNow() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        StringJoiner result = new StringJoiner("-");

        result.add(addLeadingZero(year, 4));
        result.add(addLeadingZero(month, 2));
        result.add(addLeadingZero(dayOfMonth, 2));

        return result.toString();
    }

    private String addLeadingZero(int value, int totalLength) {
        String result = Integer.toString(value);

        while(result.length() < totalLength) {
            result = "0" + result;
        }

        return result;
    }
}
