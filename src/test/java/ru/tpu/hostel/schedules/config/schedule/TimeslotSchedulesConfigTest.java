package ru.tpu.hostel.schedules.config.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tpu.hostel.schedules.TestData;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TimeslotSchedulesConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void loadToFileAndLoadFromFileWithSuccess() throws Exception {
        String filePath = tempDir.resolve("schedules.json").toString();
        TimeslotSchedulesConfig config = TestData.gymConfig();

        TimeslotSchedulesConfig.loadToFile(filePath, config);
        TimeslotSchedulesConfig result = TimeslotSchedulesConfig.loadFromFile(filePath);

        assertThat(result.getSchedules()).containsKey("gym");
        assertThat(result.getSchedules().get("gym").getLimit()).isEqualTo(TestData.LIMIT_8);
    }
}
