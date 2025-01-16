package ru.tpu.hostel.schedules.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(name = "user-userservice", url = "http://userservice:8080")
public interface UserServiceClient {


}
