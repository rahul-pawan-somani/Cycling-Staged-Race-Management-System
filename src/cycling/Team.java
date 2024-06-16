package cycling;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Team implements Serializable {
    private static int teamIdCounter = 1;

    private int teamID;
    private String teamName;
    private String teamDescription;
    private List<Rider> listOfRiders;

    // Constructor

    public Team() {
        // do nothing
    }

    public Team(String teamName, String teamDescription) {
        this.teamID = Team.teamIdCounter++;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.listOfRiders = new ArrayList<>();
    }

    // Getters and Setters
    public int getTeamIdCounter() {
        return teamIdCounter;
    }

    public void resetTeamIdCounter() {
        Team.teamIdCounter = 1;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamDescription() {
        return teamDescription;
    }

    public void setTeamDescription(String teamDescription) {
        this.teamDescription = teamDescription;
    }

    public List<Rider> getRiders() {
        return listOfRiders;
    }

    public void addRider(Rider rider) {
        listOfRiders.add(rider);
    }

    public void removeRider(Rider rider) {
        listOfRiders.remove(rider);
    }
}
