package cycling;

import java.io.Serializable;

public class Checkpoint implements Serializable {
    private static int checkpointIdCounter = 1;

    private int checkpointID;
    private Double checkpointLocation;
    private CheckpointType checkpointType;
    private Double checkpointAverageGradient;
    private Stage stage;

    // Constructor
    public Checkpoint() {
        // do nothing
    }

    public Checkpoint(int stageId, double location, CheckpointType type, Stage stage) {
        this.checkpointID = Checkpoint.checkpointIdCounter++;
        this.checkpointLocation = location;
        this.checkpointType = type;
        this.stage = stage;
    }

    public Checkpoint(int stageId, Double location, CheckpointType type, Double averageGradient,
            Double length, Stage stage) {
        this.checkpointID = Checkpoint.checkpointIdCounter++;
        this.checkpointLocation = location;
        this.checkpointType = type;
        this.checkpointAverageGradient = averageGradient;
        this.stage = stage;
    }

    // Getters and Setters
    public int getCheckpointIdCounter() {
        return checkpointIdCounter;
    }

    public void resetCheckpointIdCounter() {
        Checkpoint.checkpointIdCounter = 1;
    }

    public int getCheckpointID() {
        return checkpointID;
    }

    public void setCheckpointID(int checkpointID) {
        this.checkpointID = checkpointID;
    }

    public Double getCheckpointLocation() {
        return checkpointLocation;
    }

    public void setCheckpointLocation(Double checkpointLocation) {
        this.checkpointLocation = checkpointLocation;
    }

    public CheckpointType getCheckpointType() {
        return checkpointType;
    }

    public void setCheckpointType(CheckpointType checkpointType) {
        this.checkpointType = checkpointType;
    }

    public Double getCheckpointAverageGradient() {
        return checkpointAverageGradient;
    }

    public void setCheckpointAverageGradient(Double checkpointAverageGradient) {
        this.checkpointAverageGradient = checkpointAverageGradient;
    }

    public Stage getStage() {
        return stage;
    }
}
