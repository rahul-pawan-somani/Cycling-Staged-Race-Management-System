package cycling;

import java.io.Serial;
import java.io.Serializable;

/**
 * A rider registered to a team.
 */
public final class Rider implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int riderID;
    private final String riderName;
    private final int yearOfBirth;
    private final Team team;

    Rider(int riderID, String riderName, int yearOfBirth, Team team) {
        this.riderID = riderID;
        this.riderName = riderName;
        this.yearOfBirth = yearOfBirth;
        this.team = team;
    }

    public int getRiderID() {
        return riderID;
    }

    public String getRiderName() {
        return riderName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public Team getTeam() {
        return team;
    }
}
