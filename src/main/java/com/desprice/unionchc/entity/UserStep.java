package com.desprice.unionchc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserStep {


    public static final String USER_ID = "user_id";
    public static final String BOT_ID = "bot_id";
    public static final String STEP = "step";
    public static final String VALUE = "value";

    @JsonProperty(USER_ID)
    public Long userId;
    @JsonProperty(BOT_ID)
    public Integer botId;

    public Integer step;
    public Long value;

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
        this.value = 0L;
    }


    public static String getFieldCreate() {
        return USER_ID + " INTEGER, " +
                BOT_ID + " INT, " +
                STEP + " INT, " +
                VALUE + " BIGINT ";
    }

}
