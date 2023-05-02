package com.skypro.shelteranimaltgbot.service;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.*;

import com.skypro.shelteranimaltgbot.model.Pet;
import com.skypro.shelteranimaltgbot.model.TypePet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.skypro.shelteranimaltgbot.model.Enum.CommandButton.*;

@Service
public class ButtonService {

    @Autowired
    PetService petService;

    @Autowired
    TypePetService typePetService;


    /**
     * основное меню + позвать волонтера, поделится контактом
     */
    public Keyboard keyboardMenu() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(CALL_VOLUNTEER.getCommandText()),
                new KeyboardButton("Оставить контакт").requestContact(true))
                .addRow(new KeyboardButton(START.getCommandText())).resizeKeyboard(true);
    }

    /**
     * выпадающее меню в чат
     */
    public Keyboard keyboardChatMenu() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(ABOUT.getCommandText()).callbackData(ABOUT.getCommandText()))
                .addRow(new InlineKeyboardButton(TAKE_PET.getCommandText()).callbackData(TAKE_PET.getCommandText()))
                .addRow(new InlineKeyboardButton(REPORT.getCommandText()).callbackData(REPORT.getCommandText()))
                .addRow(new InlineKeyboardButton(SHARE.getCommandText()).switchInlineQuery(SHARE.getCommandText()));
    }

    /**
     * кнопки принять или отклонить запрос для волонтера
     * */
    public Keyboard keyboardForChatSession() {
        return new ReplyKeyboardMarkup(
                new KeyboardButton(OPEN.getCommandText()),
                new KeyboardButton(CLOSE.getCommandText())).resizeKeyboard(true);
    }

    /**
     * метод выводит все виды животных(кошки собаки и тд)
     */

    public Keyboard viewAllTypePet() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        Set<TypePet> typePetsList = new HashSet<>(typePetService.getAllTypePet());
        for (TypePet typePet : typePetsList) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(typePet.getType()).callbackData(typePet.getType()));
        }
        return inlineKeyboardMarkup;
    }

    /**
     * выпадающее меню в чат
     * подробнее о приюте
     */
    public Keyboard keyboardChatInfoShelterMenu() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(ABOUT_SHELTER.getCommandText()).callbackData(ABOUT_SHELTER.getCommandText()))
                .addRow(new InlineKeyboardButton(OPERATING_MODE.getCommandText()).callbackData(OPERATING_MODE.getCommandText()))
                .addRow(new InlineKeyboardButton(SAFETY.getCommandText()).callbackData(SAFETY.getCommandText()));
    }

    /**
     * метод возвращает всех животных по типу (кошки / собаки)
     * */
    public Keyboard viewPets(String data) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<Pet> pets = new ArrayList<>(petService.getAllPetByTypePet(data));
        pets.stream()
                .filter(p -> p.getTypePet().getType().equals(data))
                .sorted(Comparator.comparing(Pet::getName))
                .forEach(pet -> {
                    inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Имя " + pet.getName() + " Возраст " + pet.getAge() + " года")
                            .callbackData(pet.getId() + " " + pet.getName() + " " + pet.getAge()));
                });
        return inlineKeyboardMarkup;
    }

    /**
     * кнопки рекомендаций как взять кошку
     * */

    public Keyboard keyboardGetCatRecommendation() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(RULES_OF_ACQUAINTANCE.getCommandText()).callbackData(RULES_OF_ACQUAINTANCE.getCommandText()))
                .addRow(new InlineKeyboardButton(TRANSPORT_RECOMMENDATIONS.getCommandText()).callbackData(TRANSPORT_RECOMMENDATIONS.getCommandText()))
                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()))
                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()))
                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()))
                .addRow(new InlineKeyboardButton(REASONS_FOR_REJECTION.getCommandText()).callbackData(REASONS_FOR_REJECTION.getCommandText()))
                .addRow(new InlineKeyboardButton(DOCUMENTS.getCommandText()).switchInlineQuery(DOCUMENTS.getCommandText()));
    }
//    public Keyboard keyboardGetCatRecommendation() {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(RULES_OF_ACQUAINTANCE.getCommandText()).callbackData(RULES_OF_ACQUAINTANCE.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(TRANSPORT_RECOMMENDATIONS.getCommandText()).callbackData(TRANSPORT_RECOMMENDATIONS.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(REASONS_FOR_REJECTION.getCommandText()).callbackData(REASONS_FOR_REJECTION.getCommandText()));
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(DOCUMENTS.getCommandText()).callbackData(DOCUMENTS.getCommandText()));
//       return inlineKeyboardMarkup;
////                new InlineKeyboardButton(RULES_OF_ACQUAINTANCE.getCommandText()).callbackData(RULES_OF_ACQUAINTANCE.getCommandText()))
////                .addRow(new InlineKeyboardButton(TRANSPORT_RECOMMENDATIONS.getCommandText()).callbackData(TRANSPORT_RECOMMENDATIONS.getCommandText()))
////                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_KID_HOUSE.getCommandText()))
////                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_ADULT_HOUSE.getCommandText()))
////                .addRow(new InlineKeyboardButton(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()).callbackData(RECOMMENDATIONS_FOR_INVALID_HOUSE.getCommandText()))
////                .addRow(new InlineKeyboardButton(REASONS_FOR_REJECTION.getCommandText()).callbackData(REASONS_FOR_REJECTION.getCommandText()))
////                .addRow(new InlineKeyboardButton(CYNOLOGIST_ADVICE.getCommandText()).callbackData(CYNOLOGIST_ADVICE.getCommandText()))
////                .addRow(new InlineKeyboardButton(BEST_CYNOLOGISTS.getCommandText()).callbackData(BEST_CYNOLOGISTS.getCommandText()))
////                .addRow(new InlineKeyboardButton(DOCUMENTS.getCommandText()).callbackData(DOCUMENTS.getCommandText()));
//    }


    /**
     * кнопки получить рекомендации как взять кошку/собаку
     * */
    public Keyboard takePetMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(TAKE_CAT.getCommandText()).callbackData(TAKE_CAT.getCommandText()));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(TAKE_DOG.getCommandText()).callbackData(TAKE_DOG.getCommandText()));
        return inlineKeyboardMarkup;
    }

    /**
     * кнопки Оформить или Назад
     * */
    public Keyboard designOrBack(CallbackQuery callbackQuery) {
        String[] data = callbackQuery.data().split(" ");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Оформить").callbackData("Design " + data[0] + " " + data[1] + " " + data[2]));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Назад").callbackData("Back"));
        return inlineKeyboardMarkup;
    }
}
