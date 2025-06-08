package ru.tpu.hostel.schedules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableAsync
public class SchedulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulesApplication.class, args);
    }

}
