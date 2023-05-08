package com.skypro.shelteranimaltgbot.service;

import com.skypro.shelteranimaltgbot.model.Enum.ReportStatus;
import com.skypro.shelteranimaltgbot.model.Report;
import com.skypro.shelteranimaltgbot.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(Report.class);
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report createReport(Report report) {
        logger.info("Вызван метод создания отчета о домашнем питомце {}", report);
        return reportRepository.save(report);
    }

    public Report saveReport(Long userTelegramId, byte[] photo, String diet,
                             String petInfo, String changeInPetBehavior) {
        logger.info("Вызван метод сохранения отчета в БД");
        Report report = new Report();
        report.setUserTelegramId(userTelegramId);
        report.setPhoto(photo);
        report.setDiet(diet);
        report.setPetInfo(petInfo);
        report.setChangeInPetBehavior(changeInPetBehavior);
        report.setReportStatus(ReportStatus.ACCEPTED);

        return reportRepository.save(report);
    }

    //TODO добавить исключение по отчету в класс exception
    public Report findReport(Long id) {
        logger.info("Вызван метод поиска отчета в БД {}", id);
        return reportRepository.findById(id).orElseThrow();
    }

    public Report findReportByChatId(Long chatId) {
        logger.info("Вызван метод поиска отчета в БД {} по чат айди пользователя");
        return reportRepository.findByUserTelegramId(chatId);
    }

    public Report updateReport(Report report, ReportStatus reportStatus) {
        logger.info("Вызван метод изменения данных в БД");
        if (report.getId() != null) {
            if (findReport(report.getId()) != null) {
                report.setReportStatus(reportStatus);
            }
        } else {
            logger.error("запрошенный отчет не найден");
            throw new RuntimeException();
        }
        return report;
    }

    public void deleteReport(Long id) {
        logger.info("Вызван метод удаления отчета");
        reportRepository.deleteById(id);
    }

    public Collection<Report> getAllReport() {
        logger.info("Вызван метод получения списка отчетов из БД");
        return reportRepository.findAll();
    }


}
