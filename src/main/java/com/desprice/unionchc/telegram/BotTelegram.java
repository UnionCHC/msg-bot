package com.desprice.unionchc.telegram;


import com.desprice.unionchc.Constants;
import com.desprice.unionchc.EthereumSer;
import com.desprice.unionchc.Options;
import com.desprice.unionchc.Utils;
import com.desprice.unionchc.entity.BotUser;
import com.desprice.unionchc.entity.Config;
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

import java.io.File;
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
    public static final String CALL_ENTER = "Войти";
    public static final String CALL_EXIT = "Выйти";
    public static final String CALL_BALANCE = "Баланс";
    public static final String CALL_INFO = "Информация";
    public static final String CALL_CONTRACT = "Контракт";

    public static final String CALL_INC1 = "Доб 1";
    public static final String CALL_GET1 = "Пол 1";
    public static final String CALL_INC2 = "Доб 2";
    public static final String CALL_GET2 = "Пол 2";

    private BotUser userBot = null;
    private UserStep userStep = null;


    private String getPathWebUrl() {
        String result;
        Config config = Options.getInstance().getConfig();
        result = config.telegramRes;
        if (result.charAt(result.length() - 1) != File.separatorChar)
            result += File.separator;
        return result;
    }

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

            userBot = TUsers.getInstance().getUser(message.getChat().getId());
            userStep = TStep.getInstance().getStep(userBot.userId, Constants.BOT_TELEGRAM);
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

            String menuText = message.getText();

            if (message != null && message.hasText()) {
                if (menuText.equals("/start") || menuText.equals(CALL_ENTER))
                    getStart(update);
                else if (menuText.equals(CALL_EXIT))
                    callExit(update);
                else if (menuText.equals(CALL_BALANCE))
                    getBalance(update);
                else if (menuText.equals(CALL_INFO))
                    getInfo(update);
                else if (menuText.equals(CALL_CONTRACT))
                    callContract(update, "sendEvent");
                else if (menuText.equals(CALL_INC1))
                    callContract(update, "incValue1");
                else if (menuText.equals(CALL_INC2))
                    callContract(update, "incValue2");
                else if (menuText.equals("/help"))
                    sendMsg(message, "Привет", true);
                else if (menuText.equals("*"))
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
            userBot = TUsers.getInstance().getUser(message.getChat().getId());
            userStep = TStep.getInstance().getStep(userBot.userId, Constants.BOT_TELEGRAM);

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
            if (userBot.userId != 0) {
                sendMessage.setText("Вы зарегистрированы");
                sendMessage.setReplyMarkup(getMenuKeyboard());
                sendMessage(sendMessage);
                return;
            }

            String path = getPathWebUrl();
            path += messageIn.getChat().getId();
            System.out.println(path);

            String text = "Регистрация в системе \n Ваш выбор" +
                    "\n\n  <a href=\" " + path + "\"> Новый</a>" +
                    "" + path;
            sendMessage.setText(text);
            sendMessage.setParseMode("html");


            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Новый");
            button.setCallbackData(DATA_START_NEW);

            // button.setUrl("https://unionchc.com");
            button.setUrl(path);


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
                    userBot = new BotUser(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
                    tUsers.checkUser(userBot);
                    userStep.userId = userBot.userId;
                    userStep.step = STEP_EXISTS_ADDRESS;
                    TStep.getInstance().updateStep(userStep);
                    break;
                case STEP_EXISTS_ADDRESS:

                    userBot.address = update.getMessage().getText();
                    tUsers.updateAddress(userBot);
                    userStep.step = Constants.STEP_PASSWORD;
                    TStep.getInstance().updateStep(userStep);
                    sendMsg(update.getMessage(), "Отправьте Ваш пароль", true);
                    break;
                case Constants.STEP_PASSWORD:
                    userBot.password = update.getMessage().getText();
                    if (null != userBot.address && !userBot.address.isEmpty()) {
                        if (EthereumSer.getInstance().checkUnlock(userBot.address, userBot.password))
                            tUsers.updatePassword(userBot);
                        else {
                            sendMsg(update.getMessage(), "Неверный пароль\n, Отправьте другой пароль", true);
                            return;
                        }
                    }
                    if (null == userBot.address || userBot.address.isEmpty()) {
                        tUsers.updatePassword(userBot);
                        userBot.address = EthereumSer.getInstance().createAccount(userBot.password);
                        if (null != userBot.address && !userBot.address.isEmpty()) {
                            tUsers.updateAddress(userBot);
                            EthereumSer.getInstance().sendMoney(Constants.BOT_ACCOUNT[1], userBot.address, Constants.INIT_MONEY, Constants.BOT_ACCOUNT[0]);
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

    private void callExit(Update update) {
        if (userBot.userId > 0) {
            try {
                TUsers tUsers = TUsers.getInstance();
                tUsers.removeUser(userBot.userId);
                Message message = update.getMessage();
                SendMessage sendMessage = initMessage(message, false);
                sendMessage.setText("Вы вышли из системы");
                sendMessage.setReplyMarkup(getMenuStart());
                sendMessage(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private void getInfo(Update update) {
        try {
            Message message = update.getMessage();
            SendMessage sendMessage = initMessage(message, false);
            sendMessage.setText("Ваша информация\n" +
                    "Адрес: " + userBot.address);
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

            BigInteger balance = EthereumSer.getInstance().getBalance(userBot.address);
            if (null != balance)
                sendMessage.setText("Ваш баланс: " + Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER));
            else
                sendMessage.setText("Ваш баланс: 0");
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void callContract(Update update, String functionName) {
        String result = EthereumSer.getInstance().sendContract(userBot.address, Constants.BOT_CONTRACT, functionName, userBot.password);
        // sendMsg(update.getMessage(), result, true);
        sendMsg(update.getMessage(), "Контракт отправлен", true);
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


    private static ReplyKeyboardMarkup getMenuStart() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(CALL_ENTER);
        keyboard.add(keyboardFirstRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
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
