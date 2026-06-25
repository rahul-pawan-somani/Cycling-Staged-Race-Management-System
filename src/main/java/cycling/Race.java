package cycling;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A staged cycling race.
 */
public final class Race implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int raceID;
    private final String raceName;
    private final String raceDescription;
    private final List<Stage> stages = new ArrayList<>();

    Race(int raceID, String raceName, String raceDescription) {
        this.raceID = raceID;
        this.raceName = raceName;
        this.raceDescription = raceDescription;
    }

    public int getRaceID() {
        return raceID;
    }

    public String getRaceName() {
        return raceName;
    }

    public String getRaceDescription() {
        return raceDescription;
    }

    public List<Stage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    void addStage(Stage stage) {
        stages.add(stage);
    }

    void removeStage(Stage stage) {
        stages.remove(stage);
    }

    double calculateTotalLength() {
        return stages.stream().mapToDouble(Stage::getStageLength).sum();
    }

    String getRaceDetails() {
        return "Race ID: " + raceID
                + "\nRace Name: " + raceName
                + "\nRace Description: " + raceDescription
                + "\nNumber of Stages: " + stages.size()
                + "\nTotal Length: " + calculateTotalLength() + " km";
    }
}
