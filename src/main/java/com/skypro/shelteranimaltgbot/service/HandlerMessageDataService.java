package com.skypro.shelteranimaltgbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HandlerMessageDataService {


    private final CommandButtonService commandButtonService;
    private final TelegramBot telegramBot;
    private final ChatSessionWithVolunteerService chatSessionService;
    private final SendReportService sendReportService;
    private final UserService userService;

    /**
     * команды
     */
    private final String START = "/start";
    private final String CALL_VOLUNTEER = "Позвать волонтера";
    private final String OPEN = "Принять";
    private final String CLOSE = "Закрыть/Отклонить";


    public HandlerMessageDataService(CommandButtonService commandButtonService,
                                     TelegramBot telegramBot, ChatSessionWithVolunteerService chatSessionService,
                                     SendReportService sendReportService, UserService userService) {
        this.commandButtonService = commandButtonService;
        this.telegramBot = telegramBot;
        this.chatSessionService = chatSessionService;
        this.sendReportService = sendReportService;
        this.userService = userService;
    }

    /**
     * метод обрабатывает входящие сообщения от юзера, отвечает на вызовы кнопок основного меню клавиатуры, а также проверяет отправлен отчет о питомце или пользователь поделился контактом
     */
    public List<SendMessage> handlerMessageData(Update update, List<SendMessage> messages) {
        Message message = update.message();
        Long userId = message.from().id();
        String text = update.message().text();
        var contact = update.message().contact();

        if (update.message().photo() != null
                && update.message().caption() != null) {
            sendReportService.saveReport(update);
        } else if (contact != null) {
            commandButtonService.setContact(update);
            messages.add(new SendMessage(userId, message.from().firstName() + " спасибо, мы свяжемся с вами в ближайшее время"));
            commandButtonService.sendNotification(update, messages);
        } else {
            switch (text) {
                case START -> commandButtonService.mainMenu(update, messages);
                case CALL_VOLUNTEER -> commandButtonService.callVolunteer(update, messages);
                case OPEN -> commandButtonService.openСonnection(messages);
                case CLOSE -> commandButtonService.closeСonnection(update, messages);
                default -> chatWithVolunteer(message, userId);
            }
        }
        return messages;

    }

    /**
     * метод проверяет если соединение с волонтером установлено, то сообщения пересылаются от юзера волонтеру и от волонтера юзеру
     */
    private void chatWithVolunteer(Message message, Long userId) {
        Long idSessionForConnect = chatSessionService.getLastId(userId);
        Long userTelegramId = chatSessionService.getChatUser(idSessionForConnect).getTelegramIdUser();
        Long volunteerChatId = chatSessionService.getChatUser(idSessionForConnect).getTelegramIdVolunteer();
        if (chatSessionService.checkSession(idSessionForConnect) && !userId.equals(userTelegramId)) {
            ForwardMessage forwardMessage = new ForwardMessage(userTelegramId, userId, message.messageId());
            SendResponse response = telegramBot.execute(forwardMessage);
        } else if (chatSessionService.checkSession(idSessionForConnect) && !userId.equals(volunteerChatId)) {
            ForwardMessage forwardMessage = new ForwardMessage(volunteerChatId, userId, message.messageId());
            SendResponse response = telegramBot.execute(forwardMessage);
        }
    }

}
