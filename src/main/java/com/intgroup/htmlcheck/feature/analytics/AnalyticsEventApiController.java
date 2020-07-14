package com.intgroup.htmlcheck.feature.analytics;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/api/v2/analytics")
public class AnalyticsEventApiController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private AnalyticsEventService analyticsEventService;
    
    @PostMapping("/addUserEvent")
    public Object addUserEvent(@RequestBody Map<String, Object> payload) {
        String token = payload.getOrDefault("token", "").toString();
        User user = userService.findUserByToken(token);
        if (user == null) {
            return RequestResult.fail("User with token not found");
        }
        
        AnalyticsEvent event = new AnalyticsEvent();
        event.setUserEmail(user.getEmail());
        
        event.setEventName(payload.getOrDefault("eventName", "").toString());
        event.setEventValue(payload.getOrDefault("eventValue", "").toString());
        event.setCf1(payload.getOrDefault("cf1", "").toString());
        event.setCf2(payload.getOrDefault("cf2", "").toString());
        event.setCf3(payload.getOrDefault("cf3", "").toString());
        event.setCf4(payload.getOrDefault("cf4", "").toString());
        
        analyticsEventService.save(event);
        
        return RequestResult.success("Event saved");
    }
}
