package com.desprice.unionchc.telegram;


import com.desprice.unionchc.Constants;
import com.desprice.unionchc.EthereumSer;
import com.desprice.unionchc.Options;
import com.desprice.unionchc.Utils;
import com.desprice.unionchc.entity.Config;
import com.desprice.unionchc.entity.UserBot;
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

import static com.desprice.unionchc.Constants.*;

public class BotTelegram extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotTelegram.class);

    private static final String DATA_START_NEW = "data_start_new";
    private static final String DATA_START_EXISTS = "data_start_exists";
    private static final String DATA_CONTRACT2_CANCEL = "data_contract2_cancel";

    private static final String CALL_ENTER = "Войти";
    private static final String CALL_EXIT = "Выйти";
    private static final String CALL_BALANCE = "Баланс";
    private static final String CALL_INFO = "Информация";
    private static final String CALL_CONTRACT = "Контракт";

    private static final String CALL_INC1 = "Добавить 1";
    private static final String CALL_GET1 = "Получить 1";
    private static final String CALL_INC2 = "Добавить 2";
    private static final String CALL_GET2 = "Получить 2";

    private UserBot userBot = null;
    private UserStep userStep = null;


    private static BotTelegram ourInstance;

    public static BotTelegram getInstance() {
        return ourInstance;
    }

    private BotTelegram() {

    }

    private String getPathWebUrl() {
        String result;
        Config config = Options.getInstance().getConfig();
        result = config.telegramRes;
        if (result.charAt(result.length() - 1) != File.separatorChar)
            result += File.separator;
        return result;
    }

    public void setUserBot(UserBot userBot) {
        this.userBot = userBot;
    }

    public void setUserBot(long userId) {
        userBot = TUsers.getInstance().getUser(userId);
    }

    public static void init() {
        if (null != ourInstance)
            return;
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            ourInstance = new BotTelegram();
            telegramBotsApi.registerBot(ourInstance);
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
            if (userBot.userId == 0) {
                userBot = new UserBot(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
                TUsers.getInstance().checkUser(userBot);
            } else
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
                    //callContract(update, "incValue2");
                    contact2start(update);
                else if (menuText.equals(CALL_GET1))
                    callContractGet(update, 1);
                else if (menuText.equals(CALL_GET2))
                    callContractGet(update, 2);

                else if (menuText.equals("*"))
                    hideButton(message);
                else
                    sendMsg(message, "Я не знаю что ответить на это\n" +
                            " Доступны команды:\n" +
                            "/start\n" +
                            "", false);
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
            } else if (query.getData().equals(DATA_CONTRACT2_CANCEL)) {
                contract2Cancel(update);
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
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgCreate() {
        try {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(userBot.userId.toString());
            editMessage.setMessageId(userBot.messageId.intValue());
            if (null != userBot.address && !userBot.address.isEmpty()) {
                editMessage.setText("Ваш адрес\n" + userBot.address);
                execute(editMessage);

                SendMessage sendMessage = new SendMessage();
                sendMessage.enableMarkdown(true);
                sendMessage.setChatId(userBot.userId.toString());
                sendMessage.setReplyMarkup(getMenuKeyboard());
                sendMessage.setText("Вам доступны команды");
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void getStart(Update update) {
        Message messageIn = update.getMessage();
        SendMessage sendMessage = initMessage(messageIn, false);
        try {
            // if (null != userBot.password && !userBot.password.isEmpty()) {
            if (userBot.verify == 1) {
                sendMessage.setText("Вы зарегистрированы");
                sendMessage.setReplyMarkup(getMenuKeyboard());
                execute(sendMessage);
                return;
            }
            String path = getPathWebUrl();
            path += "telegram/password/" + messageIn.getChat().getId() +
                    "/" + messageIn.getMessageId();
            System.out.println(path);

            String text = "Регистрация в системе \n Ваш выбор" +
                    "\n\n  <a href=\" " + path + "\"> Новый</a>" +
                    "" + path;
            sendMessage.setText(text);
            sendMessage.setParseMode("web/html");


            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Новый");
            button.setCallbackData(DATA_START_NEW);
            button.setUrl(path);

            row.add(button);
            button = new InlineKeyboardButton();
            button.setText("Существующий");
            button.setCallbackData(DATA_START_EXISTS);

            path = getPathWebUrl();
            path += "telegram/address/" + messageIn.getChat().getId() +
                    "/" + messageIn.getMessageId();
            System.out.println(path);
            button.setUrl(path);
            row.add(button);

            keyboard.add(row);
            markup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(markup);

            Message sendOk = execute(sendMessage);
            System.out.println(sendOk.getMessageId());
            userBot.messageId = sendOk.getMessageId().longValue();
            TUsers.getInstance().setMessage(userBot);
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
                    execute(sendMessageEdit);
                    userBot = new UserBot(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
                    tUsers.checkUser(userBot);
                    userStep.userId = userBot.userId;
                    userStep.step = STEP_EXISTS_ADDRESS;
                    TStep.getInstance().updateStep(userStep);
                    break;

                case STEP_EXISTS_ADDRESS:
                    /*
                    userBot.address = update.getMessage().getText();
                    tUsers.updateAddress(userBot);
                    userStep.step = Constants.STEP_PASSWORD;
                    TStep.getInstance().updateStep(userStep);
                    sendMsg(update.getMessage(), "Отправьте Ваш пароль", true);
                    break;
                    */
                case Constants.STEP_PASSWORD:
                    /*
                    userBot.password = update.getMessage().getText();
                    if (null != userBot.address && !userBot.address.isEmpty()) {
                        if (EthereumSer.getInstance().checkUnlock(userBot.address, userBot.password))
                            tUsers.updatePassword(userBot);
                        else {
                            sendMsg(update.getMessage(), "Неверный пароль\n, Отправьте другой пароль", true);
                            return;
                        }
                    }
                    createUser();
                    userStep.step = Constants.STEP_NONE;
                    TStep.getInstance().updateStep(userStep);
                    SendMessage sendMessage = initMessage(update.getMessage(), true);
                    sendMessage.setText("Пароль обновлен");
                    sendMessage.setReplyMarkup(getMenuKeyboard());
                    */
                    break;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void createUser() {
        TUsers tUsers = TUsers.getInstance();
        if (null == userBot.address || userBot.address.isEmpty()) {
            userBot.address = EthereumSer.getInstance().createAccount(userBot.password);
            userBot.verify = 1;
            tUsers.updateVerify(userBot);
            EthereumSer.getInstance().sendMoney(Constants.BOT_ACCOUNT[1], userBot.address, Constants.INIT_MONEY, Constants.BOT_ACCOUNT[0]);
            sendMsgCreate();
        }
    }

    public boolean checkAddress() {
        TUsers tUsers = TUsers.getInstance();
        boolean result = false;
        try {
            if (userBot.verify == 0) {
                if (EthereumSer.getInstance().checkUnlock(userBot.address, userBot.password)) {
                    userBot.verify = 1;
                    tUsers.updateVerify(userBot);
                    result = true;
                }
            } else
                result = true;
        } catch (Exception ex) {
            result = false;
            LOGGER.debug(ex.getMessage());
        }
        if (result)
            sendMsgCreate();
        return result;
    }


    private void startNew(Update update) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        try {
            UserBot user = new UserBot(message.getChat().getId(), message.getChat().getFirstName(), message.getChat().getLastName(), message.getChat().getUserName());
            userStep.userId = user.userId;
            TUsers tUsers = TUsers.getInstance();
            tUsers.checkUser(user);

            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(message.getChatId().toString());
            editMessage.setMessageId(message.getMessageId());
            // editMessage.setText("Вы добавлены в систему");
            editMessage.setText("Отправьте Ваш пароль");
            execute(editMessage);
            userStep.step = Constants.STEP_PASSWORD;
            TStep.getInstance().updateStep(userStep);

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
                execute(sendMessage);
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
                    "Адрес: \n");
            sendMessage.setReplyMarkup(getMenuKeyboard());
            execute(sendMessage);

            sendMessage.setText(userBot.address);
            execute(sendMessage);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getBalance(Update update) {
        try {
            Message message = update.getMessage();
            SendMessage sendMessage = initMessage(message, false);

            BigInteger balance = EthereumSer.getInstance().getBalance(userBot.address);
            //BigInteger gasPrice = EthereumSer.getInstance().getGasPrice();

            if (null != balance)
                sendMessage.setText("Ваш баланс: " + Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER)
                        //  + " "+ gasPrice
                );
            else
                sendMessage.setText("Ваш баланс: 0");
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void callContract(Update update, String functionName) {
        String result = EthereumSer.getInstance().sendContract(userBot.address, Constants.BOT_CONTRACT, functionName, userBot.password);
        // sendMsg(update.getMessage(), result, true);
        sendMsg(update.getMessage(), "Контракт отправлен", false);
    }

    private void callContractGet(Update update, int value) {
        BigInteger result = EthereumSer.getInstance().getValueEvent(userBot.address, Constants.BOT_CONTRACT, value);
        if (null != result)
            sendMsg(update.getMessage(), "Значение: " + result.toString(), false);
        else
            sendMsg(update.getMessage(), "Значение нет: ", false);
    }

    public void sendInfoToAddress(String address, String value) {
        UserBot userTo = TUsers.getInstance().getUserFromAddress(address);
        if (userTo.userId > 0) {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.enableMarkdown(true);
                sendMessage.setChatId(userBot.userId.toString());
                sendMessage.setText("Значение: " + value);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private void contact2start(Update update) {
        Message messageIn = update.getMessage();
        SendMessage sendMessage = initMessage(messageIn, false);
        try {
            if (userBot.verify == 0) {
                sendMessage.setText("Вам нужно зарегистрироваться");
                sendMessage.setReplyMarkup(hideKeyboard());
                execute(sendMessage);
                return;
            }
            String path = getPathWebUrl();
            path += "telegram/contract2/" + messageIn.getChat().getId() +
                    "/" + messageIn.getMessageId();
            System.out.println(path);

            String text = "Подтвердите контракт" +
                    "\n\n  <a href=\" " + path + "\"> Контракт</a>" +
                    "" + path;
            sendMessage.setText(text);
            sendMessage.setParseMode("web/html");
            TStep.getInstance().updateStep(userStep, Constants.STEP_CONTRACT2);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Контракт");
            button.setUrl(path);
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText("Отмена");
            button.setCallbackData(DATA_CONTRACT2_CANCEL);
            row.add(button);


            keyboard.add(row);
            markup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(markup);

            Message sendOk = execute(sendMessage);
            System.out.println(sendOk.getMessageId());
            userBot.messageId = sendOk.getMessageId().longValue();
            TUsers.getInstance().setMessage(userBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean contract2update() {
        userStep = TStep.getInstance().getStep(userBot.userId, Constants.BOT_TELEGRAM);
        if (userStep.step != STEP_CONTRACT2)
            return false;

        EditMessageText sendMessageEdit = new EditMessageText();
        sendMessageEdit.setChatId(userBot.userId);
        sendMessageEdit.setMessageId(userBot.messageId.intValue());
        sendMessageEdit.setText("Контракт отправлен");
        try {
            execute(sendMessageEdit);
            String result = EthereumSer.getInstance().sendContract(userBot.address, Constants.BOT_CONTRACT, "incValue2", userBot.password);
            TStep.getInstance().updateStep(userStep, Constants.STEP_NONE);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void contract2Cancel(Update update) {
        if (userStep.step != STEP_CONTRACT2)
            return;
        try {
            EditMessageText sendMessageEdit = new EditMessageText();
            sendMessageEdit.setChatId(userBot.userId);
            sendMessageEdit.setMessageId(userBot.messageId.intValue());
            sendMessageEdit.setText("Отмена");
            execute(sendMessageEdit);
            TStep.getInstance().updateStep(userStep, Constants.STEP_NONE);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void hideButton(Message message) {
        SendMessage sendMessage = initMessage(message, false);
        sendMessage.setText("Убрать Кнопки в низу");
        sendMessage.setReplyMarkup(hideKeyboard());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getMenuStart() {
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


    private ReplyKeyboardMarkup getMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(CALL_BALANCE);
        keyboardFirstRow.add(CALL_INFO);
        //  keyboardFirstRow.add(CALL_CONTRACT);
        keyboardFirstRow.add(CALL_EXIT);
        keyboard.add(keyboardFirstRow);


        KeyboardRow keyboardtRow2 = new KeyboardRow();
        keyboardtRow2.add(CALL_GET1);
        keyboardtRow2.add(CALL_INC1);

        KeyboardRow keyboardtRow3 = new KeyboardRow();
        keyboardtRow3.add(CALL_GET2);
        keyboardtRow3.add(CALL_INC2);

        keyboard.add(keyboardtRow2);
        keyboard.add(keyboardtRow3);


        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardRemove hideKeyboard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        return replyKeyboardRemove;
    }


}
