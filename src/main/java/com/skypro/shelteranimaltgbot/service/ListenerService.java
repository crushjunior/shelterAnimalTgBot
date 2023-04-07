package com.skypro.shelteranimaltgbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.skypro.shelteranimaltgbot.listener.TelegramBotUpdatesListener;
import com.skypro.shelteranimaltgbot.model.ChatSessionWithVolunteer;
import com.skypro.shelteranimaltgbot.model.Enum.RoleEnum;
import com.skypro.shelteranimaltgbot.model.Enum.SessionEnum;
import com.skypro.shelteranimaltgbot.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListenerService {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    @Autowired
    private UserService userService;

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private ChatSessionWithVolunteerService chatSessionService;


    /**
     * обработчики запросов
     */
    private final String START = "/start";
    private final String CALL_VOLUNTEER = "Позвать волонтера";
    private final String OPEN = "Принять";
    private final String CLOSE = "Закрыть/Откланить";
    private final String ABOUT = "О приюте";
    private final String TAKE_PET = "Как взять питомца из приюта";
    private final String REPORT = "Прислать отчет о питомце";
    private final String SHARE = "Рекомендация..";


    private Long idSessionForConnect;
    private Message message;
    private Long chatId;


    /**
     * метод распределяющий входящие данные от клиента
     */

    public List<SendMessage> messages(Update update) {
        List<SendMessage> messages = new ArrayList<>();
        try {
            var calBackData = update.callbackQuery();
            if (calBackData != null && update.message() == null) {
                handlerСalBakData(calBackData, messages);
            } else if (calBackData == null && update.message() != null) {
                handlerMessageData(update, messages);
            }
        } catch (NullPointerException e) {
            logger.info("не обрабатываемые данные.. ");
            e.getMessage();
        }
        return messages;
    }

    /**
     * метод обработки update.message
     */
    private List<SendMessage> handlerMessageData(Update update, List<SendMessage> messages) {
        message = update.message();
        chatId = message.chat().id();
        var contact = update.message().contact();

        if (contact != null) {
            setContact(update);
            messages.add(new SendMessage(chatId, message.from().firstName() + " спасибо, мы свяжемся с вами в ближайшее время"));
            //TODO перенаправить запрос волонтеру
        } else {
            switch (message.text()) {
                case START:
                    mainMenu(update, messages);
                    break;
                case CALL_VOLUNTEER:
                    callVolunteer(messages);
                    break;
                case OPEN:
                    openСonnection(messages);
                    break;
                case CLOSE:
                    closeСonnection(messages);
                    break;
                default:
                    chatWithVolunteer();
                    break;
            }
        }
        return messages;

    }

    /**
     * закрытие соединения с клиентом
     */

    private List<SendMessage> closeСonnection(List<SendMessage> messages) {
        chatSessionService.getChatSessionForClose(idSessionForConnect, SessionEnum.CLOSE);
        ChatSessionWithVolunteer chatUser = chatSessionService.getChatUser(idSessionForConnect);
        messages.add(new SendMessage(chatUser.getChatIdUser(), "Волонтер перевел Вас на бота, для повторной связи с волонтером нажмите кнопку Позвать волонтера"));
        return messages;
    }


    /**
     * чат с волонтером
     */
    private void chatWithVolunteer() {
        Long userChatId = chatSessionService.getChatUser(idSessionForConnect).getChatIdUser();
        Long volunteerChatId = chatSessionService.getChatUser(idSessionForConnect).getTelegramIdVolunteer();
        if (chatSessionService.checkSession(idSessionForConnect) && !chatId.equals(userChatId)) {
            ForwardMessage forwardMessage = new ForwardMessage(userChatId, chatId, message.messageId());
            SendResponse response = telegramBot.execute(forwardMessage);
        } else if (chatSessionService.checkSession(idSessionForConnect) && !chatId.equals(volunteerChatId)) {
            ForwardMessage forwardMessage = new ForwardMessage(volunteerChatId, chatId, message.messageId());
            SendResponse response = telegramBot.execute(forwardMessage);
        }
    }


    /**
     * подтверждение соединения волонтером с клиентом
     */

    private List<SendMessage> openСonnection(List<SendMessage> messages) {
        if (!chatSessionService.checkSession(idSessionForConnect)) {
            chatSessionService.getChatSessionForClose(idSessionForConnect, SessionEnum.OPEN);
        } else {
            messages.add(new SendMessage(chatId, "Запрос от пользователя обрабатывается, либо уже закрыт"));
        }
        return messages;
    }


    /**
     * метод направляем волонтерам запрос от клиента
     */
    private List<SendMessage> callVolunteer(List<SendMessage> messages) {
        getVolunteer().stream()
                .forEach(user -> {
                    //отправили сообщение всем волонтерам и кнопки принять / откланить, открыли сессию в статусе ожидания
                    messages.add(new SendMessage(user.getUserChatId(), "нужна помощь " + " для " + message.from().firstName()).replyMarkup(keyboardForChatSession()));
                    ChatSessionWithVolunteer newSession = new ChatSessionWithVolunteer(user.getUserTelegramId(), message.from().id(), chatId, SessionEnum.STANDBY);
                    chatSessionService.createSession(newSession);
                    idSessionForConnect = newSession.getId();
                });
        messages.add(new SendMessage(chatId, "Соединение устанавливается.."));
        return messages;
    }


    /**
     * вывод основного меню
     */
    private List<SendMessage> mainMenu(Update update, List<SendMessage> messages) {
        userService.addUser(update);
        messages.add(new SendMessage(chatId, "Привет " + message.from().firstName()).replyMarkup(keyboardMenu()));
        messages.add(new SendMessage(chatId, "Выберете пункт меню:").replyMarkup(keyboardChatMenu()));
        return messages;
    }


    /**
     * метод обработки update.callBack
     */
    private List<SendMessage> handlerСalBakData(CallbackQuery calBackData, List<SendMessage> messages) {
        switch (calBackData.data()) {
            case ABOUT:
                //TODO сделать метод обработки запроса о приюте
                messages.add(new SendMessage(chatId, "в разработке"));
                break;
            case TAKE_PET:
                //TODO сделать метод обработки запроса как взять питомца
                messages.add(new SendMessage(chatId, "в разработке"));
                break;
            case REPORT:
                //TODO сделать метод по обработке запроса подать отчет
                messages.add(new SendMessage(chatId, "в разработке"));
                break;
        }
        return messages;
    }


    /**
     * добавляем номер телефона в БД
     */
    private void setContact(Update update) {
        userService.setContact(update);
    }


    /**
     * получаем список волонтеров
     */
    public List<User> getVolunteer() {
        return new ArrayList<>(userService.cheUsersByRole(RoleEnum.VOLUNTEER));
    }


    /**
     * выпадающее меню в чат
     */
    private Keyboard keyboardChatMenu() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(ABOUT).callbackData(ABOUT))
                .addRow(new InlineKeyboardButton(TAKE_PET).callbackData(TAKE_PET))
                .addRow(new InlineKeyboardButton(REPORT).callbackData(REPORT))
                .addRow(new InlineKeyboardButton(SHARE).switchInlineQuery(SHARE));
    }

    /**
     * нижнее меню
     */
    private Keyboard keyboardMenu() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(CALL_VOLUNTEER),
                new KeyboardButton("Оставить контакт").requestContact(true))
                .addRow(new KeyboardButton(START)).resizeKeyboard(true);
    }


    /**
     * кнопки принять или отклонить запрос для волонтера
     * */
    private Keyboard keyboardForChatSession() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(OPEN),
                new KeyboardButton(CLOSE)).resizeKeyboard(true);
    }


}