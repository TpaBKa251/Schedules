package ru.tpu.hostel.schedules.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RoomsConfig {

    @JsonProperty("2")
    private List<String> secondFloor;

    @JsonProperty("3")
    private List<String> thirdFloor;

    @JsonProperty("4")
    private List<String> fourthFloor;

    @JsonProperty("5")
    private List<String> fifthFloor;

    public Map<String, List<String>> toMap() {
        return Map.of(
                "2", secondFloor,
                "3", thirdFloor,
                "4", fourthFloor,
                "5", fifthFloor
        );
    }

    // TODO сделать нормальную загрузку списка этажей из конфига
    public List<String> getAllFloors() {
        return List.of("2", "3", "4", "5");
    }
}
