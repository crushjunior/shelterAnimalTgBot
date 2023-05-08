package com.skypro.shelteranimaltgbot.service;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.skypro.shelteranimaltgbot.listener.TelegramBotUpdatesListener;
import com.skypro.shelteranimaltgbot.model.Adoption;
import com.skypro.shelteranimaltgbot.model.Enum.ProbationPeriod;
import com.skypro.shelteranimaltgbot.model.Enum.RoleEnum;
import com.skypro.shelteranimaltgbot.model.Enum.StatusEnum;
import com.skypro.shelteranimaltgbot.model.Report;
import com.skypro.shelteranimaltgbot.model.User;
import com.skypro.shelteranimaltgbot.repository.PetRepository;
import com.skypro.shelteranimaltgbot.repository.ReportRepository;
import com.skypro.shelteranimaltgbot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SendReportService {

    private static final Pattern REPORT_PATTERN = Pattern.compile(
            "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)\n" +
                    "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)\n" +
                    "([А-яA-z\\s\\d\\D]+):(\\s)([А-яA-z\\s\\d\\D]+)");

    private final String TEXT_TEMPLATE = "<b>ИНСТРУКЦЯИЯ ЗАПОЛНЕНИЯ ОТЧЕТА:</b> \n\n" +
            "1) <i>Скопируйте текст шаблона ниже</i> \n" +
            "2) <i>Сфотографируйте питомца</i> \n" +
            "3) <i>Вставьте скопированный шаблон в описание к фото</i> \n" +
            "4) <i>Замените ХХХ своими комментариями</i>";

    private final String TEMPLATE = "Id: ХХХ \n" +
            "Рацион: XXXX XXXX XXXX \n" +
            "Самочувствие: XXXX XXXX XXXX \n" +
            "Поведение: XXXX XXXX XXXX \n";

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final UserService userService;
    private final AdoptionService adoptionService;


    public SendReportService(TelegramBot telegramBot, ReportService reportService,
                             ReportRepository reportRepository, UserRepository userRepository,
                             PetRepository petRepository, UserService userService, AdoptionService adoptionService) {
        this.telegramBot = telegramBot;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.userService = userService;
        this.adoptionService = adoptionService;
    }

    public List<SendMessage> reportForm(Long id, List<SendMessage> messages) {
        logger.info("Вызван метод отправляющий образец отчета для пользователя");
        messages.add(new SendMessage(id, TEXT_TEMPLATE).parseMode(ParseMode.HTML));
        messages.add(new SendMessage(id, TEMPLATE));
        return messages;
    }


    //TODO поправить все недочеты изменить метод сохранения фотографии в локальную папку с сохранением ссылки на нее в БД
    //TODO добавить метод проверки принадлежности питомца опекуну
    public void saveReport(Update update) {
        logger.info("Вызван метод сохранения отчета из чата телеграм");
        String text = update.message().caption();
        Matcher matcher = REPORT_PATTERN.matcher(text);
        Long chatId = update.message().chat().id();
        Long petId = userService.findUser(chatId).getPet().getId();
        if (matcher.matches()) {
            String diet = matcher.group(3);
            String petInfo = matcher.group(6);
            String changeInPetBehavior = matcher.group(9);
            GetFile getFileRequest = new GetFile(update.message().photo()[1].fileId());
            GetFileResponse getFileResponse = telegramBot.execute(getFileRequest);
            try {
                File file = getFileResponse.file();
                file.fileSize();

                byte[] photo = telegramBot.getFileContent(file);

                if (reportService.findReportByChatId(chatId) == null &&
                        userService.checkUserStatus(chatId) == StatusEnum.ADOPTER) {
                    adoptionService.createRecord(chatId, petId, 30);
                }

                if (userRepository.findAByUserTelegramId(update.message().from().id()) != null &&
                        userService.checkUserStatus(chatId) == StatusEnum.ADOPTER) {

                    reportService.saveReport(chatId, photo, diet, petInfo, changeInPetBehavior);



                    telegramBot.execute(new SendMessage(chatId, "Отчет принят!"));
                } else {
                    telegramBot.execute(new SendMessage(chatId, "Вы еще не усыновили домашнего питомца!"));

                }
            } catch (IOException e) {
                logger.error("Ошибка при загрузке фотографии");
                telegramBot.execute(new SendMessage(chatId,
                        "Ошибка при загрузке фотографии"));
            }
        } else {
            telegramBot.execute(new SendMessage(chatId,
                    "Отчет не заполнен полностью, проверьте все поля заполнения отчета и повторите отправку!"));
        }
    }

    //TODO сделать  @Scheduled

    @Scheduled(cron = "0 0 20 * * *")
    public void run() {
        List<Adoption> adoptions = new ArrayList<>((Collection) adoptionService.getAll().stream()
                .filter(adoption -> adoption.getProbationPeriod() == ProbationPeriod.PASSING));
        for (Adoption adoption : adoptions) {
            Set<Report> reports = adoption.getReports();
            Report lastReport = reports.stream().max(Comparator.comparing(Report::getDate)).orElse(null);

            if (lastReport.getDate().isBefore(LocalDate.now().minusDays(1))) {
                List<User> volunteers = userService.checkUsersByRole(RoleEnum.VOLUNTEER);
                for (User volunteer : volunteers) {
                    String textToVolunteer = "Отчёт о животном: " + adoption.getPet().getId() + " от усыновителя: " + adoption.getUser().getId() +
                            " не поступал больше 2-х дней." + "/n" +
                            "Дата последнего отчета: " + lastReport.getDate();
                    telegramBot.execute(new SendMessage(volunteer.getId(), textToVolunteer));
                }
            }

            if (lastReport.getDate().equals(LocalDate.now().minusDays(1))) {
                String textToUser = "Уважаемый " + adoption.getUser().getFirstName() + " не забудьте сегодня отправить отчет";
                SendMessage messageToUser = new SendMessage(lastReport.getUserTelegramId(), textToUser);
                telegramBot.execute(messageToUser);
            }

            if (adoption.getDate().equals(LocalDate.now().minusDays(30))) {
                String textToUser = "Уважаемый " + adoption.getUser().getFirstName() + " поздравляем, вы прошли испытательный срок!";
                SendMessage messageToUser = new SendMessage(lastReport.getUserTelegramId(), textToUser);
                telegramBot.execute(messageToUser);
            }
        }
    }

}
