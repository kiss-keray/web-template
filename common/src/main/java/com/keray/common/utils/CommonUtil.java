package com.keray.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

/**
 * @author by keray
 * date:2019/12/4 2:14 PM
 */
@Slf4j
public final class CommonUtil {
    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/12/4 2:15 PM</h3>
     * 寻找某个class是否有某个注解 包括向上的接口，父类
     * </p>
     *
     * @param clazz
     * @param annotation
     * @return <p> {@link boolean} </p>
     * @throws
     */
    public static boolean classAllSuperHaveAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        if (clazz.getAnnotation(annotation) != null) {
            return true;
        }
        if (clazz.getSuperclass() == null) {
            return false;
        }
        return classAllSuperHaveAnnotation(clazz.getSuperclass(), annotation);
    }

    public static <A extends Annotation> A getClassAllAnnotation(Class<?> clazz, Class<A> annotation) {
        if (clazz.getAnnotation(annotation) != null) {
            return clazz.getAnnotation(annotation);
        }
        if (clazz.getSuperclass() == null) {
            return null;
        }
        return getClassAllAnnotation(clazz.getSuperclass(), annotation);
    }

    public static BigDecimal moneyTrans(double num) {
        return BigDecimal.valueOf((long) (num * 100),2);
    }
}
