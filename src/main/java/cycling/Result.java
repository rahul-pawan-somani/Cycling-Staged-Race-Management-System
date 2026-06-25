package cycling;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * A rider's ordered checkpoint times for one stage.
 */
public final class Result implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Rider rider;
    private final Stage stage;
    private final List<LocalTime> checkpointTimes;

    Result(Rider rider, Stage stage, List<LocalTime> checkpointTimes) {
        this.rider = rider;
        this.stage = stage;
        this.checkpointTimes = List.copyOf(checkpointTimes);
    }

    public Rider getRider() {
        return rider;
    }

    public Stage getStage() {
        return stage;
    }

    public List<LocalTime> getCheckpointTimes() {
        return checkpointTimes;
    }
}
