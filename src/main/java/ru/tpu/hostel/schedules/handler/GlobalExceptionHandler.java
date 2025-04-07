package ru.tpu.hostel.schedules.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tpu.hostel.schedules.exception.ResponsibleNotFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> map = new HashMap<>();

        map.put("code", "500");
        map.put("message", ex.getMessage());

        return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponsibleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResponsibleNotFoundException(ResponsibleNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        errorResponse.put("code", "404");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
