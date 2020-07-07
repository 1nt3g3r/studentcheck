package com.intgroup.htmlcheck.service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoginSuccessRedirectService {
    public String getRedirect() {
        if (hasAuthority("ADMIN")) {
            return "redirect:/admin/";
        }

        return "redirect:/";
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for(GrantedAuthority a: auth.getAuthorities()) {
            if (a.getAuthority().equals(authority)) {
                return true;
            }
        }

        return false;
    }
}
