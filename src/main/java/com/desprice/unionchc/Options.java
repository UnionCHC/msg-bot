package com.desprice.unionchc;

import com.desprice.unionchc.entity.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class Options {

    private final static Logger LOGGER = LoggerFactory.getLogger(Options.class);
    private static Options INSTANCE = null;

    private Config mConfig;

    private Options() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("config.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mConfig = mapper.readValue(is, Config.class);
        } catch (IOException e) {
            LOGGER.error("Error config file:" + e.getMessage());
        }
    }

    public static Options getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Options();
        }

        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Config getConfig() {
        return mConfig;
    }
}
