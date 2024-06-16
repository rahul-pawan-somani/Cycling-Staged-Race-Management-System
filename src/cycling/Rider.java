package cycling;

import java.io.Serializable;

public class Rider implements Serializable {
    private static int riderIdCounter = 1;

    private int teamID;
    private int riderID;
    private String riderName;
    private int yearOfBirth;
    private Team team; // Reference to the Team object associated with this Rider

    // Constructor
    public Rider() {
        // do nothing
    }

    public Rider(int teamID, String name, int yearOfBirth, Team team) {
        this.teamID = teamID;
        this.riderID = Rider.riderIdCounter++;
        this.riderName = name;
        this.yearOfBirth = yearOfBirth;
        this.team = team;
    }

    // Getters and Setters
    public int getRiderIdCounter() {
        return riderIdCounter;
    }

    public void resetRiderIdCounter() {
        Rider.riderIdCounter = 1;
    }

    public int getRiderID() {
        return riderID;
    }

    public void setRiderID(int riderID) {
        this.riderID = riderID;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public int getTeamIdOfRider() {
        return teamID;
    }

    public void setTeamIdOfRider(int teamID) {
        this.teamID = teamID;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

}
