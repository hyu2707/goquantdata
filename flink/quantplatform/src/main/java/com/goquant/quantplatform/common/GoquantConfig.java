package com.goquant.quantplatform.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;

public class GoquantConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GoquantConfig.class);

    private Properties props = new Properties();;

    public GoquantConfig(String env) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(InputStream resourceStream = loader.getResourceAsStream(env+".properties")) {
            props.load(resourceStream);
        }
        LOG.info("loaded config: "+props.toString());
    }

    public boolean getIsbacktest() {
        return loadBoolean("quantplatform.isbacktest");
    }

    public Instant getHistoricalDataStartDay() {
        return loadInstant("quantplatform.historical_data_start_day");
    }

    public Instant getHistoricalDataEndDay() {
        return loadInstant("quantplatform.historical_data_end_day");
    }

    public String getDataPath() {
        return loadAbsPath("quantplatform.data.path");
    }

    private String loadAbsPath(String propsKey) {
        return props.getProperty(propsKey).replaceFirst("^~", System.getProperty("user.home"));
    }

    private boolean loadBoolean(String propsKey) {
        String rawValue =  props.getProperty(propsKey, "false");
        return rawValue.equals("true");
    }

    private Instant loadInstant(String propsKey) {
        return Util.stringDayToInstant(props.getProperty(propsKey));
    }


}
