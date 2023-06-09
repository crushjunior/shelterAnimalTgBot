package com.skypro.shelteranimaltgbot.service;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.skypro.shelteranimaltgbot.listener.TelegramBotUpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForwardListenerService {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final HandlerСalBakDataService handlerСalBakDataService;
    private final HandlerMessageDataService handlerMessageDataService;


    public ForwardListenerService(HandlerСalBakDataService handlerСalBakDataService, HandlerMessageDataService handlerMessageDataService) {
        this.handlerСalBakDataService = handlerСalBakDataService;
        this.handlerMessageDataService = handlerMessageDataService;
    }

    public List<SendMessage> messages(Update update) {
        List<SendMessage> messages = new ArrayList<>();
        try {
            var callBackData = update.callbackQuery();
            if (callBackData != null && update.message() == null) {
                handlerСalBakDataService.handlerСalBakData(callBackData, messages);
            } else if (callBackData == null && update.message() != null) {
                handlerMessageDataService.handlerMessageData(update, messages);
            }
        } catch (Exception e) {
            logger.info("не обрабатываемые данные.. ");
            e.getMessage();
        }
        return messages;
    }

}
