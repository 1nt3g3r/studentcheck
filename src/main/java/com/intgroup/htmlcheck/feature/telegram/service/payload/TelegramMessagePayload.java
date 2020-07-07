package com.intgroup.htmlcheck.feature.telegram.service.payload;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Data
public class TelegramMessagePayload {
    private TelegramMessagePayloadType payloadType;
    private Map<String, Object> data = new HashMap<>();

    public static TelegramMessagePayload createBottomKeyboard(String keyboard) {
        TelegramMessagePayload result = new TelegramMessagePayload();
        result.setPayloadType(TelegramMessagePayloadType.bottomKeyboard);
        result.data.put("keyboard", keyboard);
        return result;
    }

    public static TelegramMessagePayload createInlineKeyboard(String keyboard) {
        TelegramMessagePayload result = new TelegramMessagePayload();
        result.setPayloadType(TelegramMessagePayloadType.inlineKeyboard);
        result.data.put("keyboard", keyboard);
        return result;
    }

    public void apply(SendMessage message) {
        if (payloadType == null) {
            return;
        }

        switch (payloadType) {
            case inlineKeyboard:
                addInlineKeyboard(message, data.getOrDefault("keyboard", "").toString());
                break;
            case bottomKeyboard:
                addBottomKeyboard(message, data.getOrDefault("keyboard", "").toString());
                break;
        }
    }

    private void addBottomKeyboard(SendMessage message, String keyboard) {
        String[] rows = keyboard.replace("\r", "").split("\n");

        List<KeyboardRow> keyboardLayout = new ArrayList<>();

        for(String row: rows) {
            KeyboardRow keyboardLayoutRow = new KeyboardRow();
            keyboardLayout.add(keyboardLayoutRow);

            String[] buttons = row.split("\\|");
            for(String buttonData: buttons) {
                String[] buttonParts = buttonData.split("=>");
                String buttonText = buttonParts[0];
                String buttonPayload = buttonParts[1];

                KeyboardButton button = new KeyboardButton().setText(buttonText);

                if (buttonPayload.equals("${requestContact}")) {
                    button.setRequestContact(true);
                }

                keyboardLayoutRow.add(button);
            }
        }

        //Attach keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(keyboardLayout);

        message.setReplyMarkup(replyKeyboardMarkup);
    }

    private void addInlineKeyboard(SendMessage message, String keyboard) {
        String[] rows = keyboard.replace("\r", "").split("\n");

        List<List<InlineKeyboardButton>> keyboardLayout = new ArrayList<>();
        for(String row: rows) {
            List<InlineKeyboardButton> keyboardLayoutRow = new ArrayList<>();
            keyboardLayout.add(keyboardLayoutRow);

            String[] buttons = row.split("\\|");
            for(String buttonData: buttons) {
                String[] buttonParts = buttonData.split("=>");
                String buttonText = buttonParts[0];
                String buttonPayload = buttonParts[1];

                InlineKeyboardButton button = new InlineKeyboardButton().setText(buttonText);

                if (buttonPayload.startsWith("http://") || buttonPayload.startsWith("https://")) {
                    button.setUrl(buttonPayload);
                } else {
                    button.setCallbackData(buttonPayload);
                }

                keyboardLayoutRow.add(button);
            }
        }

        //Attach keyboard
        message.setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(keyboardLayout));
    }

    public static void main(String[] args) {
        String line = "Привет=>hello|Пока=>bye";
        TelegramMessagePayload.createBottomKeyboard(line);
    }
}
