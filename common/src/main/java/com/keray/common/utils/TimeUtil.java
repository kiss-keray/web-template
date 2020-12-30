package com.keray.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author by keray
 * date:2019/9/5 14:05
 */
public class TimeUtil {
    public static final DateTimeFormatter DATE_TIME_FORMATTER_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_SC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_SN = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_DAY_YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 14:08</h3>
     * 当前时间，毫秒
     * </p>
     *
     * @param
     * @return <p> {@link String} </p>
     * @throws
     */
    public static String nowMS() {
        return DATE_TIME_FORMATTER_MS.format(LocalDateTime.now());
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 14:08</h3>
     * 当前时间，秒
     * </p>
     *
     * @param
     * @return <p> {@link String} </p>
     * @throws
     */
    public static String now() {
        return DATE_TIME_FORMATTER_SC.format(LocalDateTime.now());
    }
}
