package com.desprice.unionchc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.api.objects.User;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BotUser {

    //    public static final String ID = "id";
    public static final String CHAT_ID = "chat_id";
    public static final String USER_ID = "user_id";
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
    @JsonProperty(FIRSTNAME)
    public String firstName;
    @JsonProperty(LASTNAME)
    public String lastName;
    @JsonProperty(USERNAME)
    public String userName;
    public String wallet;
    public String address;
    public String password;

    public BotUser() {
        this.userId = 0L;
    }

    public BotUser(Long id, String firstName, String lastName, String userName) {
        this.chatId = id;
        this.userId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public BotUser(User user) {
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
                        FIRSTNAME + " TEXT, " +
                        LASTNAME + "  TEXT, " +
                        USERNAME + "  TEXT, " +
                        WALLET + "  TEXT, " +
                        ADDRESS + "  TEXT, " +
                        PASSWORD + "  TEXT ";

    }

}
