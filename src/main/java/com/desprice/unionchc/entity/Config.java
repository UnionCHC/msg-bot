package com.desprice.unionchc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {
    public String host;
    public int port;
    public String server;
    @JsonProperty("socket_file")
    public String socketFile;
    @JsonProperty("telegram_res")
    public String telegramRes;
    public Map<String, String> sqlite;
}
