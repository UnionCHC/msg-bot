package com.desprice.unionchc;

import com.desprice.unionchc.service.Service;
import com.desprice.unionchc.sqlite.SQLite;
import com.desprice.unionchc.telegram.BotTelegram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        // runSQLite();
        runService();
    }


    private static void runService() {
        try {
            Service service = new Service();
            service.start();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private static void runSQLite() {
        try {
            SQLite sqlite = SQLite.getInstance();
            sqlite.checkTables();

            BotTelegram.init();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }


}

