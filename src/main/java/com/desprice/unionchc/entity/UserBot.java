package com.desprice.unionchc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.api.objects.User;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserBot {

    //    public static final String ID = "id";
    public static final String CHAT_ID = "chat_id";
    public static final String USER_ID = "user_id";
    public static final String MESSAGE_ID = "message_id";
    public static final String VERIFY = "verify";
    public static final String FIRSTNAME = "first_name";
    public static final String LASTNAME = "last_name";
    public static final String USERNAME = "username";
    public static final String WALLET = "wallet";
    public static final String ADDRESS = "address";
    public static final String PASSWORD = "password";


    //    @JsonProperty(ID)
//    public Long id;
    @JsonProperty(CHAT_ID)
    public Long chatId;
    @JsonProperty(USER_ID)
    public Long userId;
    @JsonProperty(MESSAGE_ID)
    public Long messageId;
    public int verify;
    @JsonProperty(FIRSTNAME)
    public String firstName;
    @JsonProperty(LASTNAME)
    public String lastName;
    @JsonProperty(USERNAME)
    public String userName;
    public String wallet;
    public String address;
    public String password;

    public UserBot() {
        this.userId = 0L;
        this.verify = 0;
    }

    public UserBot(Long id, String firstName, String lastName, String userName) {
        this.chatId = id;
        this.userId = id;
        this.verify = 0;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public UserBot(User user) {
        this.chatId = user.getId().longValue();
        this.userId = user.getId().longValue();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
    }

    public static String getFieldCreate() {
        return //ID + "  INTEGER AUTOINCREMENT PRIMARY KEY NOT NULL, " +
                CHAT_ID + " INTEGER, " +
                        USER_ID + " INTEGER, " +
                        MESSAGE_ID + " INTEGER, " +
                        VERIFY + " INT, " +
                        FIRSTNAME + " TEXT, " +
                        LASTNAME + "  TEXT, " +
                        USERNAME + "  TEXT, " +
                        WALLET + "  TEXT, " +
                        ADDRESS + "  TEXT, " +
                        PASSWORD + "  TEXT ";
    }

}
