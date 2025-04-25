package ru.tpu.hostel.schedules.aspect.amqp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для фильтрации способов логирования. Может ставиться на методы классов и интерфейсов, а также на сами
 * классы и интерфейсы
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogFilter {

    /**
     * Логирование метода. По умолчанию {@code true}
     */
    boolean enableMethodLogging() default true;

    /**
     * Логирование параметров (аргументов) метода. По умолчанию {@code true}
     */
    boolean enableParamsLogging() default true;

    /**
     * Логирование результата метода. По умолчанию {@code true}
     */
    boolean enableResultLogging() default true;

}
