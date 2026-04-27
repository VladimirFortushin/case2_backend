package ru.mephi.case2.log;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendLogger {

    @Getter
    private static Logger logger = null;

    static {
        logger = LoggerFactory.getLogger(BackendLogger.class);
    }

    public static void log(String line) {
        if (logger != null) {
            logger.info(line);
        }
    }
}
