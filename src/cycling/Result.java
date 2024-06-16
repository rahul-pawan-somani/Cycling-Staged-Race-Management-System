package cycling;

import java.util.List;
import java.time.LocalTime;
import java.io.Serializable;

public class Result implements Serializable {
    private static int resultIdCounter = 1;

    private int resultID;
    private List<LocalTime> checkpointTimes;
    private Rider rider;
    private Stage stage;

    // Constructors
    public Result() {
        // do nothing
    }

    public Result(Rider rider, Stage stage, List<LocalTime> checkpointTimes) {
        this.resultID = Result.resultIdCounter++;
        this.rider = rider;
        this.stage = stage;
        this.checkpointTimes = checkpointTimes;
    }

    // Getters and Setters
    public int getResultIdCounter() {
        return resultIdCounter;
    }

    public void resetResultIdCounter() {
        Result.resultIdCounter = 1;
    }

    public int getResultID() {
        return resultID;
    }

    public void setResultID(int resultID) {
        this.resultID = resultID;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public List<LocalTime> getCheckpointTimes() {
        return this.checkpointTimes;
    }
}
