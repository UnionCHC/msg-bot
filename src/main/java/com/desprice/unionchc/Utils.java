package com.desprice.unionchc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Utils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final String MSG_LOG_LINE = "-------------------------------------------";
    private static final String MSG_LOG_EXCEPTION = "Exception: ";

    public static String jsonToString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            logException(ex);
        }

        return "";
    }

    public static String stackTrace(Exception exception) {
        if (exception == null)
            return "";
        StringWriter sw = new StringWriter(1024);
        final PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }


    public static void logException(Exception ex) {
        String message = "";
        if (null != ex && null != ex.getMessage()) {
            message = ex.getMessage();
        }
        String stack = stackTrace(ex);
        LOGGER.error(MSG_LOG_LINE);
        LOGGER.error(MSG_LOG_EXCEPTION + message);
        LOGGER.error(stack);
        LOGGER.error(MSG_LOG_LINE);
    }


}
