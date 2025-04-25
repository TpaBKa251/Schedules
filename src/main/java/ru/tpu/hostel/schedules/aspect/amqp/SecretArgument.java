package ru.tpu.hostel.schedules.aspect.amqp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для скрытия чувствительных данных в параметрах (аргументах) метода из логов
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecretArgument {
}
