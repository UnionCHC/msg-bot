package com.desprice.unionchc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserStep {


    public static final String USER_ID = "user_id";
    public static final String BOT_ID = "bot_id";
    public static final String STEP = "step";

    @JsonProperty(USER_ID)
    public Long userId;
    @JsonProperty(BOT_ID)
    public Integer botId;

    public Integer step;


    public UserStep() {
        this.step = 0;
    }

    public UserStep(Long userId, Integer botId, Integer step) {
        this.userId = userId;
        this.botId = botId;
        this.step = step;
    }

    public UserStep(Long userId, Integer botId) {
        this.userId = userId;
        this.botId = botId;
        this.step = 0;
    }


    public static String getFieldCreate() {
        return USER_ID + " INTEGER, " +
                BOT_ID + " INT, " +
                STEP + " INT ";
    }


}
