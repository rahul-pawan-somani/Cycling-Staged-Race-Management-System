package cycling;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A cycling team and its riders.
 */
public final class Team implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int teamID;
    private final String teamName;
    private final String teamDescription;
    private final List<Rider> riders = new ArrayList<>();

    Team(int teamID, String teamName, String teamDescription) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
    }

    public int getTeamID() {
        return teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamDescription() {
        return teamDescription;
    }

    public List<Rider> getRiders() {
        return Collections.unmodifiableList(riders);
    }

    void addRider(Rider rider) {
        riders.add(rider);
    }

    void removeRider(Rider rider) {
        riders.remove(rider);
    }
}
