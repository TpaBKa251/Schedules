package ru.tpu.hostel.schedules.aspect.type.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckTypeAccess {

    /**
     * Иия поля из DTO
     */
    String typeField() default "";

    /**
     * Явное указание типа (если не из DTO)
     */
    Class<?> typeClass() default void.class;

    /**
     * Роли, которым разрешен доступ для каждого типа
     */
    TypeRole[] typeRoles();
    @interface TypeRole {
        String type(); // название типа или класс
        String[] roles(); // разрешенные роли
    }
}
