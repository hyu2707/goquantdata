package com.goquant.quantplatform.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Util {

    public static Instant stringDayToInstant(String DayStr) {
        LocalDate date = LocalDate.parse(DayStr);
        return date.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    public static Instant stringDayToInstantHMS(String DayStr) {
        LocalDate date = LocalDate.parse(DayStr, DateTimeFormatter.ofPattern("uuuu-MM-dd hh:mm:ss"));
        return date.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

}
