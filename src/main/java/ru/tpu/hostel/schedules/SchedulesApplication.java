package ru.tpu.hostel.schedules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableFeignClients
@SpringBootApplication
@EnableRetry
public class SchedulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulesApplication.class, args);
    }

}
