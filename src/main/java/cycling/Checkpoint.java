package cycling;

import java.io.Serial;
import java.io.Serializable;

/**
 * A sprint or categorised-climb checkpoint within a stage.
 */
public final class Checkpoint implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int checkpointID;
    private final double checkpointLocation;
    private final CheckpointType checkpointType;
    private final Double checkpointAverageGradient;
    private final Double checkpointLength;
    private final Stage stage;

    Checkpoint(int checkpointID, double location, CheckpointType type, Double averageGradient,
            Double length, Stage stage) {
        this.checkpointID = checkpointID;
        this.checkpointLocation = location;
        this.checkpointType = type;
        this.checkpointAverageGradient = averageGradient;
        this.checkpointLength = length;
        this.stage = stage;
    }

    public int getCheckpointID() {
        return checkpointID;
    }

    public double getCheckpointLocation() {
        return checkpointLocation;
    }

    public CheckpointType getCheckpointType() {
        return checkpointType;
    }

    public Double getCheckpointAverageGradient() {
        return checkpointAverageGradient;
    }

    public Double getCheckpointLength() {
        return checkpointLength;
    }

    public Stage getStage() {
        return stage;
    }
}
