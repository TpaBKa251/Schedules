package ru.tpu.hostel.schedules.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * Общее исключение для всех остальных. При создании кастомных исключений, наследоваться от него, создавать тут же
 * в виде статических классов
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {

    private final HttpStatus status;

    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ServiceException(HttpStatus status) {
        super(status.getReasonPhrase());
        this.status = status;
    }

    public ServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static class BadRequest extends ServiceException {

        public BadRequest(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }

        public BadRequest(String message, Throwable cause) {
            super(message, HttpStatus.BAD_REQUEST, cause);
        }

        public BadRequest() {
            this("Bad Request");
        }
    }

    public static class Forbidden extends ServiceException {

        public Forbidden(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }

        public Forbidden(String message, Throwable cause) {
            super(message, HttpStatus.FORBIDDEN, cause);
        }

        public Forbidden() {
            this("Forbidden");
        }
    }

    public static class NotFound extends ServiceException {

        public NotFound(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }

        public NotFound(String message, Throwable cause) {
            super(message, HttpStatus.NOT_FOUND, cause);
        }

        public NotFound() {
            this("Not Found");
        }
    }

    public static class UnprocessableEntity extends ServiceException {

        public UnprocessableEntity(String message) {
            super(message, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        public UnprocessableEntity(String message, Throwable cause) {
            super(message, HttpStatus.UNPROCESSABLE_ENTITY, cause);
        }

        public UnprocessableEntity() {
            this("Unprocessable Entity");
        }
    }

    public static class ServiceUnavailable extends ServiceException {

        public ServiceUnavailable(String message) {
            super(message, HttpStatus.SERVICE_UNAVAILABLE);
        }

        public ServiceUnavailable(String message, Throwable cause) {
            super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
        }

        public ServiceUnavailable() {
            this("Service Unavailable");
        }
    }

}
