package cycling;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Stage implements Serializable {
    private static int stageIdCounter = 1;

    private int stageID;
    private String stageName;
    private String stageDescription;
    private double stageLength;
    private LocalDateTime stageDuration;
    private StageType stageType;
    private String stageStatus;
    private List<Checkpoint> listOfCheckpoints;
    private Race race;

    // Constructor
    public Stage() {
        // do nothing
    }

    public Stage(String stageName, String stageDescription, double stageLength,
            LocalDateTime stageDuration, StageType stageType, String stageStatus, Race race) {
        this.stageID = Stage.stageIdCounter++;
        this.stageName = stageName;
        this.stageDescription = stageDescription;
        this.stageLength = stageLength;
        this.stageDuration = stageDuration;
        this.stageType = stageType;
        this.stageStatus = stageStatus;
        this.listOfCheckpoints = new ArrayList<>();
        this.race = race;
    }

    // Getters and Setters
    public int getStageIdCounter() {
        return stageIdCounter;
    }

    public void resetStageIdCounter() {
        Stage.stageIdCounter = 1;
    }

    public int getStageID() {
        return stageID;
    }

    public void setStageNumber(int stageNumber) {
        this.stageID = stageNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public void setStageDescription(String stageDescription) {
        this.stageDescription = stageDescription;
    }

    public double getStageLength() {
        return stageLength;
    }

    public void setStageLength(double stageLength) {
        this.stageLength = stageLength;
    }

    public LocalDateTime getStageDuration() {
        return stageDuration;
    }

    public void setStageDuration(LocalDateTime stageDuration) {
        this.stageDuration = stageDuration;
    }

    public StageType getStageType() {
        return stageType;
    }

    public void setStageType(StageType stageType) {
        this.stageType = stageType;
    }

    public String getStageStatus() {
        return stageStatus;
    }

    public void setStageStatus(String stageStatus) {
        this.stageStatus = stageStatus;
    }

    public void addCheckpoint(Checkpoint checkpoint) {
        listOfCheckpoints.add(checkpoint);
    }

    public void removeCheckpoint(Checkpoint checkpoint) {
        listOfCheckpoints.remove(checkpoint);
    }

    public List<Checkpoint> getCheckpoints() {
        return listOfCheckpoints;
    }

    public Race getRace() {
        return race;
    }
}
