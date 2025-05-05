package io.github.lujian213.eggfund.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Constants {
    private Constants() {
    }

    public static final String FUNDS_FILE_NAME = "funds.json";
    public static final String INVESTORS_FILE_NAME = "investors.json";
    public static final String INVEST_FILE_NAME_PATTERN = "invest_%s.json";
    public static final String INVEST_AUDIT_FILE_NAME_PATTERN = "audit_%s.json";
    public static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    public static final ZoneId ZONE_ID = ZoneId.of(System.getProperty("zone.id","UTC+08:00"));
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_FORMAT2 = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE_ID);
    public static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM").withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZONE_ID);
    public static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONE_ID);
    public static final DateTimeFormatter SECOND_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZONE_ID);
    public static final DateTimeFormatter SECOND_FORMAT2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZONE_ID);
    public static final List<String> DEFAULT_ROLE_USER = List.of("user");
    public static final List<String> DEFAULT_ROLE_ADMIN = List.of("admin");
    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
    public static final String JWT_HEADER = "Authorization";
}