package com.intgroup.htmlcheck.service.security;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailValidateService {
	// Email Regex java
	private static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

	private Pattern pattern;

	public EmailValidateService() {
		pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
	}

	public boolean isValid(String email) {
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}