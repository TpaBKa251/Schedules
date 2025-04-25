package ru.tpu.hostel.schedules.aspect.amqp;

import lombok.experimental.UtilityClass;

/**
 * Сообщения для логов
 */
@UtilityClass
class Message {

    static final String START_REPOSITORY_METHOD_EXECUTION
            = "[REPOSITORY] Выполняется репозиторный метод {}.{}()";

    static final String FINISH_REPOSITORY_METHOD_EXECUTION
            = "[REPOSITORY] Завершилось выполнение репозиторного метода {}.{}(). Время выполнения: {} мс";

    static final String REPOSITORY_METHOD_EXECUTION_EXCEPTION
            = "[REPOSITORY] Ошибка во время выполнения репозиторного метода {}.{}(). "
            + "Время старта: {}, длительность: {} мс, ошибка: {}";

    static final String START_SERVICE_METHOD_EXECUTION = "[SERVICE] Выполняется сервисный метод {}.{}()";

    static final String START_SERVICE_METHOD_EXECUTION_WITH_PARAMETERS
            = "[SERVICE] Выполняется сервисный метод {}.{}({})";

    static final String FINISH_SERVICE_METHOD_EXECUTION
            = "[SERVICE] Завершилось выполнение сервисного метода {}.{}(). Время выполнения: {} мс";

    static final String FINISH_SERVICE_METHOD_EXECUTION_WITH_RESULT
            = "[SERVICE] Завершилось выполнение сервисного метода {}.{}() с результатом {}. Время выполнения: {} мс";

    static final String SERVICE_METHOD_EXECUTION_EXCEPTION
            = "[SERVICE] Ошибка во время выполнения сервисного метода {}.{}(). "
            + "Время старта: {}, длительность: {} мс, ошибка: {}";

    static final String START_CONTROLLER_METHOD_EXECUTION = "[REQUEST] {} {}";

    static final String FINISH_CONTROLLER_METHOD_EXECUTION = "[RESPONSE] Статус: {}. Время выполнения: {} мс";

    static final String START_RABBIT_SENDING_MESSAGE = "[RABBIT] Выполняется отправка сообщения ({}, {})";

    static final String START_RABBIT_SENDING_MESSAGE_VIA_ROUTING_KEY
            = "[RABBIT] Выполняется отправка сообщения ({}, {}) по ключу {}";

    static final String FINISH_RABBIT_SENDING_MESSAGE = "[RABBIT] Сообщение ({}, {}) отправлено за {} мс";

    static final String FINISH_RABBIT_SENDING_MESSAGE_VIA_ROUTING_KEY
            = "[RABBIT] Сообщение ({}, {}) отправлено по ключу {} за {} мс";

    static final String RABBIT_RECEIVING_MESSAGE = "[RABBIT] Получено сообщение {}";

    static final String RABBIT_SENDING_MESSAGE_EXCEPTION
            = "[RABBIT] Не удалось отправить сообщение ({}, {}). Ошибка: {}. Время старта: {}, длительность: {}";

    static final String RABBIT_SENDING_MESSAGE_VIA_ROUTING_KEY_EXCEPTION
            = "[RABBIT] Не удалось отправить сообщение ({}, {}) по ключу {}. Ошибка: {}. "
            + "Время старта: {}, длительность: {}";

    static final String RABBIT_RECEIVING_MESSAGE_EXCEPTION
            = "[RABBIT] Не удалось получить сообщение. Ошибка: {}. Время старта: {}, длительность: {}";

}
