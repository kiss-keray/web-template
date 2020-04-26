package com.keray.common.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

/**
 * @author by keray
 * date:2019/9/5 15:08
 * kz表达式工具
 */
public class KZEngine {
    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 15:08</h3>
     * 校验kz表达式
     * -+y|M|d|H|m|s|S100
     * </p>
     *
     * @param expression
     * @return <p> {@link boolean} </p>
     * @throws
     */
    public static boolean checkKZ(String expression) {
        if (expression == null || "".equals(expression)) {
            return false;
        }
        String[] es = es(expression);
        for (String e : es) {
            if (!e.matches("[+|\\-|]?[y|M|d|H|m|s|S]{1}[\\d]+")) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/5 15:26</h3>
     * 根据kz表达式和开始时间计算结束时间
     * </p>
     *
     * @param expression
     * @param date
     * @return <p> {@link LocalDateTime} </p>
     * @throws
     */
    public static LocalDateTime computeTime(String expression, LocalDateTime date) {
        if (!checkKZ(expression)) {
            throw new IllegalStateException("kz表达式不合法");
        }
        String[] es = es(expression);
        for (String e : es) {
            date = computeA(e, date);
        }
        return date;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/10 17:10</h3>
     * 将时间差转换为kz表达式
     * </p>
     *
     * @param start
     * @param end
     * @return <p> {@link String} </p>
     * @throws
     */
    public static String generateKz(LocalDateTime start, LocalDateTime end) {
        StringBuilder builder = new StringBuilder();
        if (start.getYear() > end.getYear()) {
            builder.append(String.format("-y%d", start.getYear() - end.getYear()));
        } else {
            builder.append(String.format("+y%d", end.getYear() - start.getYear()));
        }
        int k = start.getMonthValue() - end.getMonthValue();
        if (k > 0) {
            builder.append(String.format(" -M%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +M%d", -k));
        }
        k = start.getDayOfMonth() - end.getDayOfMonth();
        if (k > 0) {
            builder.append(String.format(" -d%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +d%d", -k));
        }
        k = start.getHour() - end.getHour();
        if (k > 0) {
            builder.append(String.format(" -H%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +H%d", -k));
        }
        k = start.getMinute() - end.getMinute();
        if (k > 0) {
            builder.append(String.format(" -m%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +m%d", -k));
        }
        k = start.getSecond() - end.getSecond();
        if (k > 0) {
            builder.append(String.format(" -s%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +s%d", -k));
        }
        k = start.get(ChronoField.MILLI_OF_SECOND) - end.get(ChronoField.MILLI_OF_SECOND);
        if (k > 0) {
            builder.append(String.format(" -S%d", k));
        } else if (k < 0) {
            builder.append(String.format(" +S%d", -k));
        }

        return builder.toString();
    }

    private static String[] es(String expression) {
        String[] esA = expression.split(":");
        String[] esB = expression.split(" ");
        if (esA.length > esB.length || esA.length == esB.length) {
            return esA;
        }
        return esB;
    }

    /**
     * @param aEs
     * @param date
     */
    private static LocalDateTime computeA(String aEs, LocalDateTime date) {
        // 计算模式 false 减法 true 加法
        boolean state = !aEs.startsWith("-");
        aEs = aEs.substring(aEs.startsWith("-") || aEs.startsWith("+") ? 1 : 0);
        Integer value = Integer.valueOf(aEs.substring(1));
        switch (aEs.charAt(0)) {
            case 'y': {
                return date.minusYears(state ? -value : value);
            }
            case 'M': {
                return date.minusMonths(state ? -value : value);
            }
            case 'd': {
                return date.minusDays(state ? -value : value);
            }
            case 'H': {
                return date.minusHours(state ? -value : value);
            }
            case 'm': {
                return date.minusMinutes(state ? -value : value);
            }
            case 's': {
                return date.minusSeconds(state ? -value : value);
            }
            case 'S': {
                return date.minus(state ? -value : value, ChronoUnit.MILLIS);
            }
            default:
                return date;
        }
    }

    public static void main(String[] args) {
        System.out.println(computeTime("+y0 +m2 -s25 -S262", LocalDateTime.now()));
//        int a = 1;
//        Object b = a;
//        System.out.println(((Object) a).getClass());
        LocalDateTime a = LocalDateTime.parse("2019-08-08 00:00:00", TimeUtil.DATE_TIME_FORMATTER_SC);
        LocalDateTime b = LocalDateTime.parse("2019-07-10 00:00:00", TimeUtil.DATE_TIME_FORMATTER_SC);
        System.out.println(generateKz(a, b));
    }
}
