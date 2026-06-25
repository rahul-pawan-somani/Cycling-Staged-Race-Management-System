package cycling;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

/**
 * A stage belonging to a race.
 */
public final class Stage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int stageID;
    private final String stageName;
    private final String stageDescription;
    private final double stageLength;
    private final LocalDateTime startTime;
    private final StageType stageType;
    private final Race race;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private boolean waitingForResults;

    Stage(int stageID, String stageName, String stageDescription, double stageLength,
            LocalDateTime startTime, StageType stageType, Race race) {
        this.stageID = stageID;
        this.stageName = stageName;
        this.stageDescription = stageDescription;
        this.stageLength = stageLength;
        this.startTime = startTime;
        this.stageType = stageType;
        this.race = race;
    }

    public int getStageID() {
        return stageID;
    }

    public String getStageName() {
        return stageName;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public double getStageLength() {
        return stageLength;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public StageType getStageType() {
        return stageType;
    }

    public Race getRace() {
        return race;
    }

    public List<Checkpoint> getCheckpoints() {
        return Collections.unmodifiableList(checkpoints);
    }

    boolean isWaitingForResults() {
        return waitingForResults;
    }

    void concludePreparation() {
        waitingForResults = true;
    }

    void addCheckpoint(Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
        checkpoints.sort(Comparator.comparingDouble(Checkpoint::getCheckpointLocation));
    }

    void removeCheckpoint(Checkpoint checkpoint) {
        checkpoints.remove(checkpoint);
    }
}
