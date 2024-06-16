package cycling;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Race implements Serializable {
    private static int raceIdCounter = 1;

    private int raceID;
    private String raceName;
    private String raceDescription;
    private List<Stage> listOfStages;

    // Constructors

    public Race() {
        // do nothing
    }

    public Race(String raceName, String raceDescription) {
        this.raceID = Race.raceIdCounter++;
        this.raceName = raceName;
        this.raceDescription = raceDescription;
        this.listOfStages = new ArrayList<>();
    }

    // Getters and Setters
    public int getRaceIdCounter() {
        return raceIdCounter;
    }

    public void resetRaceIdCounter() {
        Race.raceIdCounter = 1;
    }

    public int getRaceID() {
        return raceID;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public String getRaceDescription() {
        return raceDescription;
    }

    public void setRaceDescription(String raceDescription) {
        this.raceDescription = raceDescription;
    }

    public List<Stage> getStages() {
        return listOfStages;
    }

    public void addStage(Stage stage) {
        listOfStages.add(stage);
    }

    public void removeStage(Stage stage) {
        listOfStages.remove(stage);
    }

    public List<Stage> getStages(List<Stage> listOfStages) {
        return listOfStages;
    }

    public double calculateTotalLength() {
        double totalLength = 0.0;
        for (Stage stage : listOfStages) {
            totalLength += stage.getStageLength();
        }
        return totalLength;
    }

    public String getRaceDetails() {
        Stage stage = new Stage();
        String raceDetails = "Race ID: " + raceID + "\nRace Name: " + raceName + "\nRace Description: "
                + raceDescription + "\nNumber of Stages: " + stage.getStageIdCounter() + "\nTotal Length: "
                + calculateTotalLength() + " km";
        return raceDetails;
    }
}
