package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.feature.mq.enums.Event;
import com.intgroup.htmlcheck.feature.mq.enums.KeyboardType;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/event-driven-message")
public class EventDrivenMessageController {
    @Autowired
    private UserService userService;

    @Autowired
    private EventDrivenMessageService eventDrivenMessageService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private EventHandleService eventHandleService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage,
                             @RequestParam(name = "query", required = false) String query) {
        ModelAndView result = new ModelAndView("admin/event-driven-message/list");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Одиночные сообщения");

        //Construct specifications
        List<Specification<EventDrivenMessage>> specs = new ArrayList<>();

        if (query != null) {
            result.addObject("query", query);
            specs.add(EventDrivenMessageSpecifications.query(query));
        }

        if (specs.isEmpty()) {
            specs.add(EventDrivenMessageSpecifications.any());
        }

        Specification<EventDrivenMessage> userSpec = EventDrivenMessageSpecifications.and(specs);

        int recordCount = eventDrivenMessageService.count(userSpec);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        //Users page
        List<EventDrivenMessage> messages = new ArrayList<>(eventDrivenMessageService.queryPage(currentPage, userSpec));

        //Sort messages - by tag, by event, by day, by hour, by minute and by priority
        messages.sort((m1, m2) -> {
            int tagCompare = m1.getTag().compareTo(m2.getTag());
            if (tagCompare == 0) {
                int eventCompare = m1.getEvent().getSortOrder() - m2.getEvent().getSortOrder();
                if (eventCompare == 0) {
                    int dayCompare = m1.getDay() - m2.getDay();
                    if (dayCompare == 0) {
                        int concreteDateCompare = 0;
                        if (m1.getConcreteDate() != null && !m1.getConcreteDate().isBlank() && m2.getConcreteDate() != null && !m2.getConcreteDate().isBlank()) {
                            LocalDate m1Date = LocalDate.parse(m1.getConcreteDate());
                            LocalDate m2Date = LocalDate.parse(m2.getConcreteDate());
                            concreteDateCompare = m1Date.compareTo(m2Date);
                        }

                        if (concreteDateCompare == 0) {
                            int hourCompare = m1.getHour() - m2.getHour();
                            if (hourCompare == 0) {
                                int minuteCompare = m1.getMinute() - m2.getMinute();
                                if (minuteCompare == 0) {
                                    return m1.getPriority() - m2.getPriority();
                                } else {
                                    return minuteCompare;
                                }
                            } else {
                                return hourCompare;
                            }
                        } else {
                            return concreteDateCompare;
                        }

                    } else {
                        return dayCompare;
                    }
                } else {
                    return eventCompare;
                }
            } else {
                return tagCompare;
            }
        });

        result.addObject("messages", messages);

        return result;
    }

    @GetMapping("/copy")
    public String copy(@RequestParam long id) {
        EventDrivenMessage original = eventDrivenMessageService.getById(id);
        if (original != null) {
            EventDrivenMessage copy = new EventDrivenMessage();
            copy.setTitle("Копия " + original.getTitle());
            copy.setPriority(original.getPriority());
            copy.setEvent(original.getEvent());
            copy.setDay(original.getDay());
            copy.setHour(original.getHour());
            copy.setMinute(original.getMinute());
            copy.setTag(original.getTag());
            copy.setText(original.getText());
            copy.setUserWroteMessagePattern(original.getUserWroteMessagePattern());
            copy.setInlineKeyboard(original.getInlineKeyboard());
            copy.setUserButtonPayload(original.getUserButtonPayload());
            copy.setConcreteDate(original.getConcreteDate());
            copy.setKeyboardType(original.getKeyboardType());

            eventDrivenMessageService.save(copy);
        }

        return "redirect:/admin/event-driven-message/list";
    }

    @GetMapping("/create-update")
    public ModelAndView getCreateUpdate(@RequestParam(name = "id", required = false, defaultValue = "-1") long id) {
        ModelAndView result = new ModelAndView("admin/event-driven-message/create-update");
        result.addObject("user", userService.getUser());
        seoService.setTitle(result, "Отдельное сообщение");

        EventDrivenMessage msg;
        if (id < 0) {
            msg = new EventDrivenMessage();
            msg.setId(id);
        } else {
            msg = eventDrivenMessageService.getById(id);
        }

        result.addObject("msg", msg);

        //Related things
        result.addObject("events", Event.values());
        result.addObject("keyboardTypes", KeyboardType.values());

        return result;
    }

    @PostMapping("/create-update")
    public String postCreateUpdate(@RequestParam(required = false, defaultValue = "-1") long id,
                                   @RequestParam String concreteDate,
                                   @RequestParam int day,
                                   @RequestParam int hour,
                                   @RequestParam int minute,
                                   @RequestParam int priority,
                                   @RequestParam String title,
                                   @RequestParam String text,
                                   @RequestParam String tag,
                                   @RequestParam Event event,
                                   @RequestParam String userWroteMessagePattern,
                                   @RequestParam String inlineKeyboard,
                                   @RequestParam String userButtonPayload,
                                   @RequestParam KeyboardType keyboardType) {
        EventDrivenMessage msg;
        if (id < 0) {
            msg = new EventDrivenMessage();
        } else {
            msg = eventDrivenMessageService.getById(id);
        }

        msg.setConcreteDate(concreteDate);
        msg.setDay(day);
        msg.setHour(hour);
        msg.setMinute(minute);
        msg.setTitle(title);
        msg.setText(text);
        msg.setTag(tag);
        msg.setEvent(event);
        msg.setPriority(priority);
        msg.setUserWroteMessagePattern(userWroteMessagePattern);
        msg.setInlineKeyboard(inlineKeyboard);
        msg.setUserButtonPayload(userButtonPayload);
        msg.setKeyboardType(keyboardType);

        eventDrivenMessageService.save(msg);

        return "redirect:/admin/event-driven-message/list";
    }

    @GetMapping("/delete")
    public String delete(long id) {
        eventDrivenMessageService.deleteById(id);

        return "redirect:/admin/event-driven-message/list";
    }

    @PostMapping("/sendToUser")
    @ResponseBody
    public String sendToUser(@RequestParam long messageId,
                             @RequestParam String phone) {
        TelegramUser telegramUser = telegramUserService.getByPhoneNumber(phone);
        if (telegramUser == null) {
            return "Пользователь не найден";
        }

        EventDrivenMessage message = eventDrivenMessageService.getById(messageId);

        eventHandleService.eventHappened(Event.userWrote, "telegramUser", telegramUser, "message", message);
        return "Сообщение отправлено";
    }
}
