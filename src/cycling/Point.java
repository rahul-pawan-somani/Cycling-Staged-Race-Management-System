package cycling;

import java.util.LinkedHashMap;
import java.io.Serializable;

public class Point implements Serializable {
    private static int pointIdCounter = 1;

    private int pointID;
    private PointType pointType;
    private LinkedHashMap<Integer, Integer> riderIDriderPointInStageMap;
    private LinkedHashMap<Integer, Integer> riderIDriderMountainPointInStageMap;
    private Stage stage;

    // Constructor
    public Point() {
        // do nothing
    }

    public Point(PointType pointType, LinkedHashMap<Integer, Integer> riderIDriderPointMap, Stage stage) {
        if (this.pointType == PointType.POINT_IN_STAGE) {
            this.pointID = pointIdCounter++;
            this.pointType = pointType;
            this.stage = stage;
            this.riderIDriderPointInStageMap = riderIDriderPointMap;
        } else if (this.pointType == PointType.MOUNTAIN_POINT_IN_STAGE) {
            this.pointID = pointIdCounter++;
            this.pointType = pointType;
            this.stage = stage;
            this.riderIDriderMountainPointInStageMap = riderIDriderPointMap;
        }
    }

    // Getters and Setters
    public int getPointIdCounter() {
        return pointIdCounter;
    }

    public void resetPointIdCounter() {
        Point.pointIdCounter = 1;
    }

    public int getPointID() {
        return pointID;
    }

    public void setPointID(int pointID) {
        this.pointID = pointID;
    }

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointType pointType) {
        this.pointType = pointType;
    }

    public LinkedHashMap<Integer, Integer> getRiderIDriderPointInStageMap() {
        return riderIDriderPointInStageMap;
    }

    public void setRiderIDriderPointInStageMap(LinkedHashMap<Integer, Integer> riderIDriderPointInStageMap) {
        this.riderIDriderPointInStageMap = riderIDriderPointInStageMap;
    }

    public LinkedHashMap<Integer, Integer> getRiderIDriderMountainPointInStageMap() {
        return riderIDriderMountainPointInStageMap;
    }

    public void setRiderIDriderMountainPointInStageMap(
            LinkedHashMap<Integer, Integer> riderIDriderMountainPointInStageMap) {
        this.riderIDriderMountainPointInStageMap = riderIDriderMountainPointInStageMap;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
