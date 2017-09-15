package com.desprice.unionchc.telegram;


import com.desprice.unionchc.Constants;
import com.desprice.unionchc.EthereumSer;
import com.desprice.unionchc.Utils;
import com.desprice.unionchc.entity.BotUser;
import com.desprice.unionchc.entity.UserStep;
import com.desprice.unionchc.sqlite.TStep;
import com.desprice.unionchc.sqlite.TUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.desprice.unionchc.Constants.STEP_EXISTS_ADDRESS;
import static com.desprice.unionchc.Constants.STEP_EXISTS_START;

public class BotTelegram extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotTelegram.class);

    public static final String DATA_START_NEW = "data_start_new";
    public static final String DATA_START_EXISTS = "data_start_exists";
    public static final String CALL_BALANCE = "Баланс";
    public static final String CALL_INFO = "Информация";
    public static final String CALL_CONTRACT = "Контракт";

    public static final String CALL_INC1 = "Доб 1";
    public static final String CALL_GET1 = "Пол 1";
    public static final String CALL_INC2 = "Доб 2";
    public static final String CALL_GET2 = "Пол 2";

    private BotUser botUser = null;
    private UserStep userStep = null;

    public static void init() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new BotTelegram());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return Constants.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return Constants.BOT_NAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.debug("onUpdateReceived");
        LOGGER.debug(Utils.jsonToString(update));


        if (update.hasMessage()) {

            Message message = update.getMessage();

            botUser = TUsers.getInstance().getUser(message.getChat().getId());
            userStep = TStep.getInstance().getStep(botUser.userId, Constants.BOT_TELEGRAM);
            switch (userStep.step) {
                case STEP_EXISTS_START:
                case STEP_EXISTS_ADDRESS:
                    startExists(update);
                    return;
                case Constants.STEP_PASSWORD:
                    //  userStep.step = Constants.STEP_PASSWORD;
                    startExists(update);
                    return;
            }

            if (message != null && message.hasText()) {
                if (message.getText().equals("/start"))
                    getStart(update);
                else if (message.getText().equals(CALL_BALANCE))
                    getBalance(update);
                else if (message.getText().equals(CALL_INFO))
                    getInfo(update);
                else if (message.getText().equals(CALL_CONTRACT))
                    callContract(update);
                else if (message.getText().equals("/help"))
                    sendMsg(message, "Привет", true);
                else if (message.getText().equals("*"))
                    hideButton(message);
                else
                    sendMsg(message, "Я не знаю что ответить на это\n" +
                            " Доступны команды:\n" +
                            "/help\n" +
                            "/start\n" +
                            "", true);
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(query.getId());

            Message message = query.getMessage();
            botUser = TUsers.getInstance().getUser(message.getChat().getId());
            userStep = TStep.getInstance().getStep(botUser.userId, Constants.BOT_TELEGRAM);

            if (query.getData().equals(DATA_START_NEW)) {
                startNew(update);
            } else if (query.getData().equals(DATA_START_EXISTS)) {
                userStep.step = Constants.STEP_EXISTS_START;
                startExists(update);
            }
        }
    }

    private SendMessage initMessage(Message message, boolean isReplay) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        if (isReplay)
            sendMessage.setReplyToMessageId(message.getMessageId());
        return sendMessage;
    }


    private void sendMsg(Message message, String text, boolean isReplay) {
        SendMessage sendMessage = initMessage(message, isReplay);
        sendMessage.setText(text);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void getStart(Update update) {
        Message messageIn = update.getMessage();
        SendMessage sendMessage = initMessage(messageIn, true);
        try {
            if (botUser.userId != 0) {
                sendMessage.setText("Вы зарегистрированы");
                sendMessage.setReplyMarkup(getMenuKeyboard());
                sendMessage(sendMessage);
                return;
            }

            String text = "Регистрация в системе \n Ваш выбор";
            sendMessage.setText(text);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Новый");
            button.setCallbackData(DATA_START_NEW);
            row.add(button);
            button = new InlineKeyboardButton();
            button.setText("Существующий");
            button.setCallbackData(DATA_START_EXISTS);
            row.add(button);

            keyboard.add(row);
            markup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(markup);

            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void startExists(Update update) {
        Message message;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else {
            CallbackQuery query = update.getCallbackQuery();
            message = query.getMessage();
        }
        try {
            TUsers tUsers = TUsers.getInstance();
            switch (userStep.step) {
                case STEP_EXISTS_START:
                    EditMessageText sendMessageEdit = new EditMessageText();
                    sendMessageEdit.setChatId(message.getChatId().toString());
                    sendMessageEdit.setMessageId(message.getMessageId());
                    sendMessageEdit.setText("Отправьте Ваш адрес");
                    editMessageText(sendMessageEdit);
                    botUser = new BotUser(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
                    tUsers.checkUser(botUser);
                    userStep.userId = botUser.userId;
                    userStep.step = STEP_EXISTS_ADDRESS;
                    TStep.getInstance().updateStep(userStep);
                    break;
                case STEP_EXISTS_ADDRESS:

                    botUser.address = update.getMessage().getText();
                    tUsers.updateAddress(botUser);
                    userStep.step = Constants.STEP_PASSWORD;
                    TStep.getInstance().updateStep(userStep);
                    sendMsg(update.getMessage(), "Отправьте Ваш пароль", true);
                    break;
                case Constants.STEP_PASSWORD:
                    botUser.password = update.getMessage().getText();
                    if (null != botUser.address && !botUser.address.isEmpty()) {
                        if (EthereumSer.getInstance().checkUnlock(botUser.address, botUser.password))
                            tUsers.updatePassword(botUser);
                        else {
                            sendMsg(update.getMessage(), "Неверный пароль\n, Отправьте другой пароль", true);
                            return;
                        }
                    }
                    if (null == botUser.address || botUser.address.isEmpty()) {
                        tUsers.updatePassword(botUser);
                        botUser.address = EthereumSer.getInstance().createAccount(botUser.password);
                        if (null != botUser.address && !botUser.address.isEmpty()) {
                            tUsers.updateAddress(botUser);
                            EthereumSer.getInstance().sendMoney(Constants.BOT_ACCOUNT[1], botUser.address, Constants.INIT_MONEY, Constants.BOT_ACCOUNT[0]);
                        }
                    }
                    userStep.step = Constants.STEP_NONE;
                    TStep.getInstance().updateStep(userStep);
                    SendMessage sendMessage = initMessage(update.getMessage(), true);
                    sendMessage.setText("Пароль обновлен");
                    sendMessage.setReplyMarkup(getMenuKeyboard());
                    break;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startNew(Update update) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        try {
            BotUser user = new BotUser(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
            userStep.userId = user.userId;
            TUsers tUsers = TUsers.getInstance();
            tUsers.checkUser(user);

            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(message.getChatId().toString());
            editMessage.setMessageId(message.getMessageId());
            // editMessage.setText("Вы добавлены в систему");
            editMessage.setText("Отправьте Ваш пароль");
            editMessageText(editMessage);
            userStep.step = Constants.STEP_PASSWORD;
            TStep.getInstance().updateStep(userStep);

            /*
            SendMessage sendMessage = initMessage(message, true);
            sendMessage.setText("Вам доступны новые команды");
            sendMessage.setReplyMarkup(getMenuKeyboard());
            sendMessage(sendMessage);
*/
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }


    private void getInfo(Update update) {
        try {
            Message message = update.getMessage();
            SendMessage sendMessage = initMessage(message, false);
            sendMessage.setText("Ваша информация\n" +
                    "Адрес: " + botUser.address);
            sendMessage.setReplyMarkup(getMenuKeyboard());
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getBalance(Update update) {
        try {
            Message message = update.getMessage();
            SendMessage sendMessage = initMessage(message, false);

            BigInteger balance = EthereumSer.getInstance().getBalance(botUser.address);
            if (null != balance)
                sendMessage.setText("Ваш баланс: " + Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER));
            else
                sendMessage.setText("Ваш баланс: 0");
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void callContract(Update update) {
        String result = EthereumSer.getInstance().sendContract(botUser.address, Constants.BOT_CONTRACT, botUser.password);
        sendMsg(update.getMessage(), result, true);
    }


    private void hideButton(Message message) {
        SendMessage sendMessage = initMessage(message, true);
        sendMessage.setText("Убрать Кнопки в низу");
        sendMessage.setReplyMarkup(hideKeyboard());
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private static ReplyKeyboardMarkup getMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(CALL_BALANCE);
        keyboardFirstRow.add(CALL_INFO);
        keyboardFirstRow.add(CALL_CONTRACT);
        keyboard.add(keyboardFirstRow);


        KeyboardRow keyboardtRow2 = new KeyboardRow();
        keyboardtRow2.add(CALL_INC1);
        keyboardtRow2.add(CALL_GET1);

        keyboardtRow2.add(CALL_INC2);
        keyboardtRow2.add(CALL_GET2);

        keyboard.add(keyboardtRow2);


        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardRemove hideKeyboard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        return replyKeyboardRemove;
    }


}
