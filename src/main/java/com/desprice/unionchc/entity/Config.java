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
    @JsonProperty("server_socket")
    public String serverSocket;

    public Map<String, String> telegram;
    public Map<String, String> sqlite;
    public Map<String, String> ethereum;
}
