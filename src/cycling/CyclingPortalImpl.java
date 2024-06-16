package cycling;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * BadCyclingPortal is a minimally compiling, but non-functioning implementor
 * of the CyclingPortal interface.
 * 
 * @author Diogo Pacheco
 * @version 2.0
 *
 */
public class CyclingPortalImpl implements CyclingPortal {

    private List<Race> races = new ArrayList<>();
    private List<Stage> stages = new ArrayList<>();
    private List<Checkpoint> checkpoints = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<Rider> riders = new ArrayList<>();
    private List<Result> results = new ArrayList<>();
    private List<Point> points = new ArrayList<>();

    /**
     * Get the races currently created in the platform.
     *
     * @return An array of race IDs in the system or an empty array if none exists.
     */
    @Override
    public int[] getRaceIds() {
        int[] raceIDs = new int[races.size()];
        for (int i = 0; i < races.size(); i++) {
            raceIDs[i] = races.get(i).getRaceID();
        }
        return raceIDs;
    }

    /**
     * The method creates a staged race in the platform with the given name and
     * description.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param name        Race’s name.
     * @param description Race’s description (can be null).
     * @throws IllegalNameException If the name already exists in the platform.
     * @throws InvalidNameException If the name is null, empty, has more than 30
     *                              characters, or has white spaces.
     * @return the unique ID of the created race.
     *
     */
    @Override
    public int createRace(String name, String description) throws IllegalNameException, InvalidNameException {
        if (name == null || name.strip().length() == 0 || name.strip().length() > 30) {
            throw new InvalidNameException(
                    "Race name cannot be null or empty. It cannot have more than 30 characters and it cannot have spaces in it");
        }
        Iterator<Race> iterator = races.iterator();
        while (iterator.hasNext()) {
            Race r = iterator.next();
            if (r.getRaceName() == name) {
                throw new IllegalNameException(name + " already exists");
            }
        }
        Race race = new Race(name, description);
        races.add(race);
        return race.getRaceID();
    }

    /**
     * Get the details from a race.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return Any formatted string containing the race ID, name, description, the
     *         number of stages, and the total length (i.e., the sum of all stages’
     *         length).
     * @throws IDNotRecognisedException If the ID does not match to any race in the
     *                                  system.
     */
    @Override
    public String viewRaceDetails(int raceId) throws IDNotRecognisedException {
        Iterator<Race> iterator = races.iterator();
        while (iterator.hasNext()) {
            Race race = iterator.next();
            if (race.getRaceID() == raceId) {
                return race.getRaceDetails();
            }
        }
        throw new IDNotRecognisedException("Invalid Race ID: " + raceId);
    }

    /**
     * The method removes the race and all its related information, i.e., stages,
     * checkpoints, and results.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race to be removed.
     * @throws IDNotRecognisedException If the ID does not match to any race in the
     *                                  system.
     */
    @Override
    public void removeRaceById(int raceId) throws IDNotRecognisedException {
        Iterator<Race> iterator1 = races.iterator();
        while (iterator1.hasNext()) {
            Race race = iterator1.next();
            if (race.getRaceID() == raceId) {
                Iterator<Stage> iterator2 = race.getStages().iterator();
                while (iterator2.hasNext()) {
                    Stage stage = iterator2.next();
                    Iterator<Checkpoint> iterator3 = stage.getCheckpoints().iterator();
                    while (iterator3.hasNext()) {
                        iterator3.remove();
                    }
                    Iterator<Result> iterator4 = results.iterator();
                    while (iterator4.hasNext()) {
                        Result result = iterator4.next();
                        if (result.getStage() == stage) {
                            iterator4.remove();
                        }
                    }
                    Iterator<Point> iterator5 = points.iterator();
                    while (iterator5.hasNext()) {
                        Point point = iterator5.next();
                        if (point.getStage() == stage) {
                            iterator5.remove();
                        }
                    }
                    iterator2.remove();
                }
                iterator1.remove();
            }
        }
        throw new IDNotRecognisedException("Invalid Race ID: " + raceId);
    }

    /**
     * The method queries the number of stages created for a race.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return The number of stages created for the race.
     * @throws IDNotRecognisedException If the ID does not match to any race in the
     *                                  system.
     */
    @Override
    public int getNumberOfStages(int raceId) throws IDNotRecognisedException {
        Iterator<Race> iterator = races.iterator();
        while (iterator.hasNext()) {
            Race race = iterator.next();
            if (race.getRaceID() == raceId) {
                return race.getStages().size();
            }
        }
        throw new IDNotRecognisedException("Invalid Race ID: " + raceId);
    }

    /**
     * Creates a new stage and adds it to the race.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId      The race which the stage will be added to.
     * @param stageName   An identifier name for the stage.
     * @param description A descriptive text for the stage.
     * @param length      Stage length in kilometres.
     * @param startTime   The date and time in which the stage will be raced. It
     *                    cannot be null.
     * @param type        The type of the stage. This is used to determine the
     *                    amount of points given to the winner.
     * @return the unique ID of the stage.
     * @throws IDNotRecognisedException If the ID does not match to any race in the
     *                                  system.
     * @throws IllegalNameException     If the name already exists in the platform.
     * @throws InvalidNameException     If the name is null, empty, has more than 30
     *                                  characters, or has white spaces.
     * @throws InvalidLengthException   If the length is less than 5km.
     */
    @Override
    public int addStageToRace(int raceId, String stageName, String description, double length, LocalDateTime startTime,
            StageType type)
            throws IDNotRecognisedException, IllegalNameException, InvalidNameException, InvalidLengthException {
        if (stageName == null || stageName.strip().length() == 0 || stageName.strip().length() > 30) {
            throw new InvalidNameException(
                    "Stage name cannot be null or empty. It cannot have more than 30 characters and it cannot have spaces in it");
        }
        if (length <= 5) {
            throw new InvalidLengthException("A stage has to be at least 5 kilometers long");
        }
        Iterator<Race> iterator = races.iterator();
        while (iterator.hasNext()) {
            Race race = iterator.next();
            if (race.getRaceID() == raceId) {
                Iterator<Stage> iterator2 = race.getStages().iterator();
                while (iterator2.hasNext()) {
                    Stage stage = iterator2.next();
                    if (stage.getStageName() == stageName) {
                        throw new IllegalNameException(stageName + " already exists");
                    }
                }
                Stage stage = new Stage(stageName, description, length, startTime, type, "", race);
                stages.add(stage);
                race.addStage(stage);
                return stage.getStageID();
            }
        }
        throw new IDNotRecognisedException("Invalid Race ID: " + raceId);
    }

    /**
     * Retrieves the list of stage IDs of a race.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return An array of stage IDs ordered (from first to last) by their sequence
     *         in the
     *         race or an empty array if none exists.
     * @throws IDNotRecognisedException If the ID does not match to any race in the
     *                                  system.
     */
    @Override
    public int[] getRaceStages(int raceId) throws IDNotRecognisedException {
        Iterator<Race> iterator = races.iterator();
        while (iterator.hasNext()) {
            Race race = iterator.next();
            if (race.getRaceID() == raceId) {
                List<Stage> s = race.getStages();
                int[] stageIDs = new int[s.size()];
                for (int i = 0; i < s.size(); i++) {
                    stageIDs[i] = s.get(i).getStageID();
                }
                return stageIDs;
            }
        }
        throw new IDNotRecognisedException("Invalid Race ID: " + raceId);
    }

    /**
     * Gets the length of a stage in a race, in kilometres.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return The stage’s length.
     * @throws IDNotRecognisedException If the ID does not match to any stage in the
     *                                  system.
     */
    @Override
    public double getStageLength(int stageId) throws IDNotRecognisedException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                return stage.getStageLength();
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID: " + stageId);
    }

    /**
     * Removes a stage and all its related data, i.e., checkpoints and results.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being removed.
     * @throws IDNotRecognisedException If the ID does not match to any stage in the
     *                                  system.
     */
    @Override
    public void removeStageById(int stageId) throws IDNotRecognisedException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                iterator.remove();
                Race race = stage.getRace();
                race.removeStage(stage);
                return; // Exit the method after removal
            }
            throw new IDNotRecognisedException("Invalid Stage ID: " + stageId);
        }
    }

    /**
     * Adds a climb checkpoint to a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId         The ID of the stage to which the climb checkpoint is
     *                        being added.
     * @param location        The kilometre location where the climb finishes within
     *                        the stage.
     * @param type            The category of the climb - {@link CheckpointType#C4},
     *                        {@link CheckpointType#C3}, {@link CheckpointType#C2},
     *                        {@link CheckpointType#C1}, or
     *                        {@link CheckpointType#HC}.
     * @param averageGradient The average gradient for the climb.
     * @param length          The length of the climb in kilometre.
     * @return The ID of the checkpoint created.
     * @throws IDNotRecognisedException   If the ID does not match to any stage in
     *                                    the system.
     * @throws InvalidLocationException   If the location is out of bounds of the
     *                                    stage length.
     * @throws InvalidStageStateException If the stage is "waiting for results".
     * @throws InvalidStageTypeException  Time-trial stages cannot contain any
     *                                    checkpoint.
     */
    @Override
    public int addCategorizedClimbToStage(int stageId, Double location, CheckpointType type, Double averageGradient,
            Double length) throws IDNotRecognisedException, InvalidLocationException, InvalidStageStateException,
            InvalidStageTypeException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                if (stage.getStageType() == StageType.TT) {
                    throw new InvalidStageTypeException("Time-trial stages cannot contain any checkpoint");
                }
                if (location < 0 || stage.getStageLength() < location) {
                    throw new InvalidLocationException("The location is out of bounds of the stage length");
                }
                if (stage.getStageStatus() == "waiting for results") {
                    throw new InvalidStageStateException("The stage is waiting for results");
                }
                Checkpoint checkpoint = new Checkpoint(stageId, location, type, averageGradient, length, stage);
                checkpoints.add(checkpoint);
                stage.addCheckpoint(checkpoint);
                return checkpoint.getCheckpointID();
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID " + stageId);
    }

    /**
     * Adds an intermediate sprint to a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId  The ID of the stage to which the intermediate sprint
     *                 checkpoint
     *                 is being added.
     * @param location The kilometre location where the intermediate sprint finishes
     *                 within the stage.
     * @return The ID of the checkpoint created.
     * @throws IDNotRecognisedException   If the ID does not match to any stage in
     *                                    the system.
     * @throws InvalidLocationException   If the location is out of bounds of the
     *                                    stage length.
     * @throws InvalidStageStateException If the stage is "waiting for results".
     * @throws InvalidStageTypeException  Time-trial stages cannot contain any
     *                                    checkpoint.
     */
    @Override
    public int addIntermediateSprintToStage(int stageId, double location) throws IDNotRecognisedException,
            InvalidLocationException, InvalidStageStateException, InvalidStageTypeException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                if (stage.getStageType() == StageType.TT) {
                    throw new InvalidStageTypeException("Time-trial stages cannot contain any checkpoint");
                }
                if (location < 0 || stage.getStageLength() < location) {
                    throw new InvalidLocationException("The location is out of bounds of the stage length");
                }
                if (stage.getStageStatus() == "waiting for results") {
                    throw new InvalidStageStateException("The stage is waiting for results");
                }
                Checkpoint checkpoint = new Checkpoint(stageId, location, CheckpointType.SPRINT, stage);
                checkpoints.add(checkpoint);
                stage.addCheckpoint(checkpoint);
                return checkpoint.getCheckpointID();
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID " + stageId);
    }

    /**
     * Removes a checkpoint from a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param checkpointId The ID of the checkpoint to be removed.
     * @throws IDNotRecognisedException   If the ID does not match to any checkpoint
     *                                    in
     *                                    the system.
     * @throws InvalidStageStateException If the stage is "waiting for results".
     */
    @Override
    public void removeCheckpoint(int checkpointId) throws IDNotRecognisedException, InvalidStageStateException {
        Iterator<Checkpoint> iterator = checkpoints.iterator();
        while (iterator.hasNext()) {
            Checkpoint checkpoint = iterator.next();
            if (checkpoint.getCheckpointID() == checkpointId) {
                iterator.remove();
                Stage stage = checkpoint.getStage();
                stage.removeCheckpoint(checkpoint);
                return; // Exit the method after removal
            }
            throw new IDNotRecognisedException("Invalid Checkpoint ID: " + checkpointId);
        }
    }

    /**
     * Concludes the preparation of a stage. After conclusion, the stage’s state
     * should be "waiting for results".
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     * 
     * @param stageId The ID of the stage to be concluded.
     * @throws IDNotRecognisedException   If the ID does not match to any stage in
     *                                    the system.
     * @throws InvalidStageStateException If the stage is "waiting for results".
     */
    @Override
    public void concludeStagePreparation(int stageId) throws IDNotRecognisedException, InvalidStageStateException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                if (stage.getStageStatus().equals("waiting for results")) {
                    throw new InvalidStageStateException("The stage is waiting for results");
                } else {
                    stage.setStageStatus("waiting for results");
                    return;
                }
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID " + stageId);
    }

    /**
     * Retrieves the list of checkpoint (mountains and sprints) IDs of a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return The list of checkpoint IDs ordered (from first to last) by their
     *         location in the
     *         stage.
     * @throws IDNotRecognisedException If the ID does not match to any stage in the
     *                                  system.
     */
    @Override
    public int[] getStageCheckpoints(int stageId) throws IDNotRecognisedException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                List<Checkpoint> c = stage.getCheckpoints();
                int[] checkpointIDs = new int[c.size()];
                for (int i = 0; i < c.size(); i++) {
                    checkpointIDs[i] = c.get(i).getCheckpointID();
                }
                return checkpointIDs;
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID: " + stageId);
    }

    /**
     * Creates a team with name and description.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param name        The identifier name of the team.
     * @param description A description of the team.
     * @return The ID of the created team.
     * @throws IllegalNameException If the name already exists in the platform.
     * @throws InvalidNameException If the name is null, empty, has more than 30
     *                              characters, or has white spaces.
     */
    @Override
    public int createTeam(String name, String description) throws IllegalNameException, InvalidNameException {
        if (name == null || name.strip().length() == 0 || name.strip().length() > 30) {
            throw new InvalidNameException(
                    "Stage name cannot be null or empty. It cannot have more than 30 characters");
        }
        Iterator<Team> iterator = teams.iterator();
        while (iterator.hasNext()) {
            Team t = iterator.next();
            if (t.getTeamName() == name) {
                throw new IllegalNameException(name + " already exists");
            }
        }
        Team team = new Team(name, description);
        teams.add(team);
        return team.getTeamID();
    }

    /**
     * Removes a team from the system.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param teamId The ID of the team to be removed.
     * @throws IDNotRecognisedException If the ID does not match to any team in the
     *                                  system.
     */
    @Override
    public void removeTeam(int teamId) throws IDNotRecognisedException {
        Iterator<Team> iterator = teams.iterator();
        while (iterator.hasNext()) {
            Team team = iterator.next();
            if (team.getTeamID() == teamId) {
                iterator.remove();
                return; // Exit the method after removal
            }
            throw new IDNotRecognisedException("Invalid Team ID: " + teamId);
        }
    }

    /**
     * Get the list of teams’ IDs in the system.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @return The list of IDs from the teams in the system. An empty list if there
     *         are no teams in the system.
     *
     */
    @Override
    public int[] getTeams() {
        int[] teamIDs = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamIDs[i] = teams.get(i).getTeamID();
        }
        return teamIDs;
    }

    /**
     * Get the riders of a team.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param teamId The ID of the team being queried.
     * @return A list with riders’ ID.
     * @throws IDNotRecognisedException If the ID does not match to any team in the
     *                                  system.
     */
    @Override
    public int[] getTeamRiders(int teamId) throws IDNotRecognisedException {
        Iterator<Team> iterator = teams.iterator();
        while (iterator.hasNext()) {
            Team team = iterator.next();
            if (team.getTeamID() == teamId) {
                List<Rider> r = team.getRiders();
                int[] riderIDs = new int[r.size()];
                for (int i = 0; i < r.size(); i++) {
                    riderIDs[i] = r.get(i).getRiderID();
                }
                return riderIDs;
            }
        }
        throw new IDNotRecognisedException("Invalid Team ID: " + teamId);
    }

    /**
     * Creates a rider.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param teamID      The ID rider’s team.
     * @param name        The name of the rider.
     * @param yearOfBirth The year of birth of the rider.
     * @return The ID of the rider in the system.
     * @throws IDNotRecognisedException If the ID does not match to any team in the
     *                                  system.
     * @throws IllegalArgumentException If the name of the rider is null or empty,
     *                                  or the year of birth is less than 1900.
     */
    @Override
    public int createRider(int teamID, String name, int yearOfBirth)
            throws IDNotRecognisedException, IllegalArgumentException {
        Iterator<Team> iterator = teams.iterator();
        while (iterator.hasNext()) {
            Team team = iterator.next();
            if (team.getTeamID() == teamID) {
                Rider rider = new Rider(teamID, name, yearOfBirth, team);
                riders.add(rider);
                team.addRider(rider);
                return rider.getRiderID();
            }
        }
        throw new IDNotRecognisedException("Invalid Team ID: " + teamID);
    }

    /**
     * Removes a rider from the system. When a rider is removed from the platform,
     * all of its results should be also removed. Race results must be updated.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param riderId The ID of the rider to be removed.
     * @throws IDNotRecognisedException If the ID does not match to any rider in the
     *                                  system.
     */
    @Override
    public void removeRider(int riderId) throws IDNotRecognisedException {
        Iterator<Rider> iterator = riders.iterator();
        while (iterator.hasNext()) {
            Rider rider = iterator.next();
            if (rider.getRiderID() == riderId) {
                iterator.remove();
                Team team = rider.getTeam();
                team.removeRider(rider);
                return; // Exit the method after removal
            }
        }
        throw new IDNotRecognisedException("Invalid Rider ID: " + riderId);
    }

    /**
     * Record the times of a rider in a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId         The ID of the stage the result refers to.
     * @param riderId         The ID of the rider.
     * @param checkpointTimes An array of times at which the rider reached each of
     *                        the
     *                        checkpoints of the stage, including the start time and
     *                        the
     *                        finish line.
     * @throws IDNotRecognisedException        If the ID does not match to any rider
     *                                         or stage in the system.
     * @throws DuplicatedResultException       Thrown if the rider has already a
     *                                         result for the stage. Each rider can
     *                                         have only one result per stage.
     * @throws InvalidCheckpointTimesException Thrown if the length of
     *                                         checkpointTimes is not equal to n+2,
     *                                         where n is the number of checkpoints
     *                                         in the stage; +2 represents the start
     *                                         time and the finish time of the
     *                                         stage.
     * @throws InvalidStageStateException      Thrown if the stage is not "waiting
     *                                         for results". Results can only be
     *                                         added to a stage while it is "waiting
     *                                         for results".
     */
    @Override
    public void registerRiderResultsInStage(int stageId, int riderId, LocalTime... checkpointTimes)
            throws IDNotRecognisedException, DuplicatedResultException, InvalidCheckpointTimesException,
            InvalidStageStateException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageId) {
                if (!stage.getStageStatus().equals("waiting for results")) {
                    throw new InvalidStageStateException("Stage in not ready for results");
                }
                if (checkpointTimes.length != stage.getCheckpoints().size() + 2) {
                    throw new InvalidCheckpointTimesException(
                            "Incorrect number of checkpoints have been entered. Make sure you add the start-time and finish-time");
                }
                Iterator<Rider> iterator2 = riders.iterator();
                while (iterator2.hasNext()) {
                    Rider rider = iterator2.next();
                    if (rider.getRiderID() == riderId) {
                        Iterator<Result> iterator3 = results.iterator();
                        while (iterator3.hasNext()) {
                            Result r = iterator3.next();
                            if (r.getRider() == rider && r.getStage() == stage) {
                                throw new DuplicatedResultException(
                                        "Each rider can have only one result per stage.");
                            }
                        }
                        List<LocalTime> cpTimes = new ArrayList<>();
                        for (LocalTime checkpointTime : checkpointTimes) {
                            cpTimes.add(checkpointTime);
                        }
                        Result result = new Result(rider, stage, cpTimes);
                        results.add(result);
                        return;
                    }
                }
                throw new IDNotRecognisedException("Invalid Rider ID: " + riderId);
            }
        }
        throw new IDNotRecognisedException("Invalid Stage ID: " + stageId);
    }

    /**
     * Get the times of a rider in a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any exceptions are
     * thrown.
     *
     * @param stageId The ID of the stage the result refers to.
     * @param riderId The ID of the rider.
     * @return The array of times at which the rider reached each of the checkpoints
     *         of the stage and the total elapsed time. The elapsed time is the
     *         difference between the finish time and the start time. Return an
     *         empty array if there is no result registered for the rider in the
     *         stage. Assume the total elapsed time of a stage never exceeds 24h
     *         and, therefore, can be represented by a LocalTime variable. There is
     *         no need to check for this condition or raise any exception.
     * @throws IDNotRecognisedException If the ID does not match to any rider or
     *                                  stage in the system.
     */
    @Override
    public LocalTime[] getRiderResultsInStage(int stageId, int riderId) throws IDNotRecognisedException {
        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            if (result.getStage().getStageID() == stageId && result.getRider().getRiderID() == riderId) {
                List<LocalTime> riderResultsInStage = result.getCheckpointTimes();
                LocalTime[] riderResultsInStageWithElapsedTime = Arrays.copyOf(
                        riderResultsInStage.toArray(new LocalTime[0]),
                        riderResultsInStage.size() + 1);
                LocalTime startTime = riderResultsInStageWithElapsedTime[0];
                LocalTime endTime = riderResultsInStageWithElapsedTime[riderResultsInStageWithElapsedTime.length - 2];
                riderResultsInStageWithElapsedTime[riderResultsInStageWithElapsedTime.length
                        - 1] = LocalTime.MIDNIGHT.plus(Duration.between(startTime, endTime));
                return riderResultsInStageWithElapsedTime;
            }
        }
        return new LocalTime[0];
    }

    /**
     * For the general classification, the aggregated time is based on the adjusted
     * elapsed time, not the real elapsed time. Adjustments are made to take into
     * account groups of riders finishing very close together, e.g., the peloton. If
     * a rider has a finishing time less than one second slower than the
     * previous rider, then their adjusted elapsed time is the smallest of both. For
     * instance, a stage with 200 riders finishing "together" (i.e., less than 1
     * second between consecutive riders), the adjusted elapsed time of all riders
     * should be the same as the first of all these riders, even if the real gap
     * between the 200th and the 1st rider is much bigger than 1 second. There is no
     * adjustments on elapsed time on time-trials.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage the result refers to.
     * @param riderId The ID of the rider.
     * @return The adjusted elapsed time for the rider in the stage. Return null if
     *         there is no result registered for the rider in the stage.
     * @throws IDNotRecognisedException If the ID does not match to any rider or
     *                                  stage in the system.
     */
    @Override
    public LocalTime getRiderAdjustedElapsedTimeInStage(int stageId, int riderId) throws IDNotRecognisedException {
        HelperMethods helperMethods = new HelperMethods();
        Stage stage = helperMethods.getStageById(stageId, stages);
        Rider rider = helperMethods.getRiderById(riderId, riders);

        LocalTime riderStartTime = null;
        LocalTime riderEndTime = null;

        for (Result result : results) {
            if (result.getStage() == stage && result.getRider() == rider) {
                riderStartTime = getRiderResultsInStage(stageId, riderId)[0];
                riderEndTime = getRiderResultsInStage(stageId, riderId)[getRiderResultsInStage(stageId, riderId).length
                        - 2];
                break;
            }
        }

        if (riderEndTime == null && riderStartTime == null) {
            return null;
        }

        LocalTime otherRiderEndTime = null;
        for (Result result : results) {
            if (result.getStage() == stage && !result.getRider().equals(rider)) {
                LocalTime[] otherRiderTimes = getRiderResultsInStage(stageId, result.getRider().getRiderID());
                otherRiderEndTime = otherRiderTimes[otherRiderTimes.length - 1];
                if (Math.abs(ChronoUnit.SECONDS.between(otherRiderEndTime, riderEndTime)) < 1) {
                    riderEndTime = otherRiderEndTime;
                }
            }
        }

        return LocalTime.ofSecondOfDay(Math.abs(ChronoUnit.SECONDS.between(riderEndTime, riderStartTime)));
    }

    /**
     * Removes the stage results from the rider.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage the result refers to.
     * @param riderId The ID of the rider.
     * @throws IDNotRecognisedException If the ID does not match to any rider or
     *                                  stage in the system.
     */
    @Override
    public void deleteRiderResultsInStage(int stageId, int riderId) throws IDNotRecognisedException {
        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            if (result.getStage().getStageID() == stageId && result.getRider().getRiderID() == riderId) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Get the riders finished position in a a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return A list of riders ID sorted by their elapsed time. An empty list if
     *         there is no result for the stage.
     * @throws IDNotRecognisedException If the ID does not match any stage in the
     *                                  system.
     */
    @Override
    public int[] getRidersRankInStage(int stageId) throws IDNotRecognisedException {
        HelperMethods helperMethods = new HelperMethods();
        Stage stage = helperMethods.getStageById(stageId, stages);
        if (!stage.getStageStatus().equals("waiting for results")) {
            return new int[0];
        }

        HashMap<LocalTime, Integer> elapsedTimeRiderIDMap = new HashMap<>();
        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            if (result.getStage() == stage) {
                Integer riderID = result.getRider().getRiderID();
                LocalTime riderStartTime = getRiderResultsInStage(stageId,
                        riderID)[0];
                LocalTime riderEndTime = getRiderResultsInStage(stageId,
                        riderID)[getRiderResultsInStage(stageId, riderID).length - 2];

                LocalTime riderElapsedTime = LocalTime.MIDNIGHT.plus(Duration.between(riderStartTime, riderEndTime));
                elapsedTimeRiderIDMap.put(riderElapsedTime, riderID);
            }
        }

        // Convert HashMap to TreeMap for sorting
        TreeMap<LocalTime, Integer> sortedElapsedTimeRiderIDTree = new TreeMap<>(elapsedTimeRiderIDMap);
        // Retrieve the entries as a sorted set
        Set<Map.Entry<LocalTime, Integer>> sortedElapsedTimeRiderIDMap = sortedElapsedTimeRiderIDTree.entrySet();

        // getting ordered array of the rider IDs based on their finish time
        ArrayList<Integer> ridersRankInStage = new ArrayList<>();
        for (Map.Entry<LocalTime, Integer> entry : sortedElapsedTimeRiderIDMap) {
            ridersRankInStage.add(entry.getValue());
        }
        return ridersRankInStage.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Get the adjusted elapsed times of riders in a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any exceptions are
     * thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return The ranked list of adjusted elapsed times sorted by their finish
     *         time. An empty list if there is no result for the stage. These times
     *         should match the riders returned by
     *         {@link #getRidersRankInStage(int)}. Assume the total elapsed time of
     *         in a stage never exceeds 24h and, therefore, can be represented by a
     *         LocalTime variable. There is no need to check for this condition or
     *         raise any exception.
     * @throws IDNotRecognisedException If the ID does not match any stage in the
     *                                  system.
     */
    @Override
    public LocalTime[] getRankedAdjustedElapsedTimesInStage(int stageId) throws IDNotRecognisedException {
        int[] riderRankedIDs = getRidersRankInStage(stageId);
        ArrayList<LocalTime> rankedAdjustedElapsedTimesInStage = new ArrayList<>();
        for (int riderIDindex = 0; riderIDindex < riderRankedIDs.length; riderIDindex++) {
            rankedAdjustedElapsedTimesInStage
                    .add(getRiderAdjustedElapsedTimeInStage(stageId, riderRankedIDs[riderIDindex]));
        }
        return rankedAdjustedElapsedTimesInStage.stream().toArray(LocalTime[]::new);
    }

    /**
     * Get the number of points obtained by each rider in a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return The ranked list of points each riders received in the stage, sorted
     *         by their elapsed time. An empty list if there is no result for the
     *         stage. These points should match the riders returned by
     *         {@link #getRidersRankInStage(int)}.
     * @throws IDNotRecognisedException If the ID does not match any stage in the
     *                                  system.
     */
    @Override
    public int[] getRidersPointsInStage(int stageId) throws IDNotRecognisedException {
        HelperMethods helperMethods = new HelperMethods();

        List<LocalTime> rankedElapsedTimes = new ArrayList<>();

        int[] riderRankedIDs = getRidersRankInStage(stageId);
        for (int riderIDindex = 0; riderIDindex < riderRankedIDs.length; riderIDindex++) {
            LocalTime elapsedTime = getRiderResultsInStage(stageId,
                    riderRankedIDs[riderIDindex])[getRiderResultsInStage(stageId, riderRankedIDs[riderIDindex]).length
                            - 1];
            rankedElapsedTimes.add(elapsedTime);
        }

        int[] pointsAsPerRiderRanksInStage = helperMethods.getPointsAsPerRiderRanksInStage(stageId, rankedElapsedTimes,
                stages);
        LinkedHashMap<Integer, Integer> riderPointsInStageMap = new LinkedHashMap<>();

        for (int i = 0; i < riderRankedIDs.length; i++) {
            riderPointsInStageMap.put(riderRankedIDs[i], pointsAsPerRiderRanksInStage[i]);
        }

        Stage stage = helperMethods.getStageById(stageId, stages);

        Point point = new Point(PointType.POINT_IN_STAGE, riderPointsInStageMap, stage);
        points.add(point);

        return pointsAsPerRiderRanksInStage;
    }

    /**
     * Get the number of mountain points obtained by each rider in a stage.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param stageId The ID of the stage being queried.
     * @return The ranked list of mountain points each riders received in the stage,
     *         sorted by their finish time. An empty list if there is no result for
     *         the stage. These points should match the riders returned by
     *         {@link #getRidersRankInStage(int)}.
     * @throws IDNotRecognisedException If the ID does not match any stage in the
     *                                  system.
     */
    @Override
    public int[] getRidersMountainPointsInStage(int stageId) throws IDNotRecognisedException {
        return null;
    }

    /**
     * Method empties this CyclingPortal of its contents and resets all internal
     * counters.
     */
    @Override
    public void eraseCyclingPortal() {
        // reset all internal counters
        Race race = new Race();
        race.resetRaceIdCounter();
        Stage stage = new Stage();
        stage.resetStageIdCounter();
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.resetCheckpointIdCounter();
        Rider rider = new Rider();
        rider.resetRiderIdCounter();
        Team team = new Team();
        team.resetTeamIdCounter();
        Result result = new Result();
        result.resetResultIdCounter();
        Point point = new Point();
        point.resetPointIdCounter();

        // reset all content
        races.clear();
        stages.clear();
        checkpoints.clear();
        teams.clear();
        riders.clear();
        results.clear();
        points.clear();
    }

    /**
     * Method saves this MiniCyclingPortal contents into a serialised file,
     * with the filename given in the argument.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param filename Location of the file to be saved.
     * @throws IOException If there is a problem experienced when trying to save the
     *                     store contents to the file.
     */
    @Override
    public void saveCyclingPortal(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            // Serialize the relevant platform data to the output stream.
            out.writeObject(this.races);
            out.writeObject(this.stages);
            out.writeObject(this.checkpoints);
            out.writeObject(this.teams);
            out.writeObject(this.riders);
            out.writeObject(this.results);
            out.writeObject(this.points);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // using SuppressWarnings to avoid the unchecked warning
    @SuppressWarnings("unchecked")
    /**
     * Method should load and replace this MiniCyclingPortal contents with the
     * serialised contents stored in the file given in the argument.
     * <p>
     * The state of this MiniCyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param filename Location of the file to be loaded.
     * @throws IOException            If there is a problem experienced when trying
     *                                to load the store contents from the file.
     * @throws ClassNotFoundException If required class files cannot be found when
     *                                loading.
     */
    @Override
    public void loadCyclingPortal(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            this.races = (List<Race>) in.readObject();
            this.stages = (List<Stage>) in.readObject();
            this.checkpoints = (List<Checkpoint>) in.readObject();
            this.teams = (List<Team>) in.readObject();
            this.riders = (List<Rider>) in.readObject();
            this.results = (List<Result>) in.readObject();
            this.points = (List<Point>) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            // Print the error message and stack trace if any of the exceptions are thrown
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The method removes the race and all its related information, i.e., stages,
     * checkpoints, and results.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param name The name of the race to be removed.
     * @throws NameNotRecognisedException If the name does not match to any race in
     *                                    the system.
     */
    @Override
    public void removeRaceByName(String name) throws NameNotRecognisedException {
        Iterator<Race> iterator1 = races.iterator();
        while (iterator1.hasNext()) {
            Race race = iterator1.next();
            if (race.getRaceName().equals(name)) { // Changed == to .equals()
                Iterator<Stage> iterator2 = race.getStages().iterator();
                while (iterator2.hasNext()) {
                    Stage stage = iterator2.next();
                    Iterator<Checkpoint> iterator3 = stage.getCheckpoints().iterator();
                    while (iterator3.hasNext()) {
                        iterator3.next();
                        iterator3.remove();
                    }
                    Iterator<Result> iterator4 = results.iterator();
                    while (iterator4.hasNext()) {
                        Result result = iterator4.next();
                        if (result.getStage() == stage) {
                            iterator4.remove();
                        }
                    }
                    Iterator<Point> iterator5 = points.iterator();
                    while (iterator5.hasNext()) {
                        Point point = iterator5.next();
                        if (point.getStage() == stage) {
                            iterator5.remove();
                        }
                    }
                    iterator2.remove();
                }
                iterator1.remove();
                return; // Exit the method after successfully removing the race
            }
        }
        throw new NameNotRecognisedException("Invalid race name");
    }

    /**
     * Get the general classification rank of riders in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     * 
     * @param raceId The ID of the race being queried.
     * @return A ranked list of riders’ IDs sorted ascending by the sum of their
     *         adjusted elapsed times in all stages of the race. That is, the first
     *         in this list is the winner (least time). An empty list if there is no
     *         result for any stage in the race.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public int[] getRidersGeneralClassificationRank(int raceId) throws IDNotRecognisedException {
        HelperMethods helperMethods = new HelperMethods();

        LinkedHashMap<LocalTime, Integer> adjustedElapsedTimeRiderIdInRaceMap = new LinkedHashMap<>();

        Iterator<Stage> iterator1 = stages.iterator();
        while (iterator1.hasNext()) {
            Stage stage = iterator1.next();
            if (stage.getRace().getRaceID() == raceId) {
                int[] rankedRidersInStage = getRidersRankInStage(stage.getStageID());
                LocalTime[] rankedElapsedTimeInStage = getRankedAdjustedElapsedTimesInStage(stage.getStageID());
                adjustedElapsedTimeRiderIdInRaceMap = helperMethods.populateAdjustedElapsedTimeMap(rankedRidersInStage,
                        rankedElapsedTimeInStage,
                        adjustedElapsedTimeRiderIdInRaceMap);
            }
        }
        return helperMethods.getOrderedRidersRankInRace(adjustedElapsedTimeRiderIdInRaceMap);
    }

    /**
     * Get the general classification times of riders in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any exceptions are
     * thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return A list of riders’ times sorted by the sum of their adjusted elapsed
     *         times in all stages of the race. An empty list if there is no result
     *         for any stage in the race. These times should match the riders
     *         returned by {@link #getRidersGeneralClassificationRank(int)}. Assume
     *         the total elapsed time of a race (the sum of all of its stages) never
     *         exceeds 24h and, therefore, can be represented by a LocalTime
     *         variable. There is no need to check for this condition or raise any
     *         exception.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public LocalTime[] getGeneralClassificationTimesInRace(int raceId) throws IDNotRecognisedException {
        HelperMethods helperMethods = new HelperMethods();

        LinkedHashMap<LocalTime, Integer> adjustedElapsedTimeRiderIdInRaceMap = new LinkedHashMap<>();

        Iterator<Stage> iterator1 = stages.iterator();
        while (iterator1.hasNext()) {
            Stage stage = iterator1.next();
            if (stage.getRace().getRaceID() == raceId) {
                int[] rankedRidersInStage = getRidersRankInStage(stage.getStageID());
                LocalTime[] rankedElapsedTimeInStage = getRankedAdjustedElapsedTimesInStage(stage.getStageID());
                adjustedElapsedTimeRiderIdInRaceMap = helperMethods.populateAdjustedElapsedTimeMap(rankedRidersInStage,
                        rankedElapsedTimeInStage,
                        adjustedElapsedTimeRiderIdInRaceMap);
            }
        }

        int[] ridersRankInRace = getRidersGeneralClassificationRank(raceId);
        List<LocalTime> rankedAdjustedElapsedTimesInRace = new ArrayList<>();
        for (int riderIDindex = 0; riderIDindex < ridersRankInRace.length; riderIDindex++) {
            for (Map.Entry<LocalTime, Integer> entry : adjustedElapsedTimeRiderIdInRaceMap.entrySet()) {
                if (Integer.valueOf(ridersRankInRace[riderIDindex]) == entry.getValue()) {
                    rankedAdjustedElapsedTimesInRace.add(entry.getKey());
                    break;
                }
            }
        }
        return rankedAdjustedElapsedTimesInRace.stream().toArray(LocalTime[]::new);
    }

    /**
     * Get the overall points of riders in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return An array of riders’ points (i.e., the sum of their points in all
     *         stages of the race), sorted by the total adjusted elapsed time. An
     *         empty array if there is no result for any stage in the race. These
     *         points should match the riders returned by
     *         {@link #getRidersGeneralClassificationRank(int)}.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public int[] getRidersPointsInRace(int raceId) throws IDNotRecognisedException {
        int[] ridersRankInRace = getRidersGeneralClassificationRank(raceId);
        LinkedHashMap<Integer, Integer> riderPointRiderIdMap = new LinkedHashMap<>();

        Iterator<Result> iterator1 = results.iterator();
        while (iterator1.hasNext()) {
            Result result = iterator1.next();
            if (result.getStage().getRace().getRaceID() == raceId) {
                Stage stage = result.getStage();
                int[] rankedRiderIdInStage = getRidersRankInStage(stage.getStageID());
                int[] rankedRiderPointInStage = getRidersPointsInStage(stage.getStageID());
                for (int i = 0; i < rankedRiderIdInStage.length; i++) {
                    boolean riderFound = false;
                    for (Map.Entry<Integer, Integer> entry : riderPointRiderIdMap.entrySet()) {
                        if (entry.getValue().equals(rankedRiderIdInStage[i])) {
                            Integer totalPointsOfRiderInRace = entry.getKey() + rankedRiderPointInStage[i];
                            entry.setValue(totalPointsOfRiderInRace);
                            riderFound = true;
                            break;
                        }
                    }
                    if (!riderFound) {
                        riderPointRiderIdMap.put(rankedRiderPointInStage[i], rankedRiderIdInStage[i]);
                    }
                }
            }
        }
        ArrayList<Integer> ridersRankedPointsInRace = new ArrayList<>();
        for (int i = 0; i < ridersRankInRace.length; i++) {
            for (Map.Entry<Integer, Integer> entry : riderPointRiderIdMap.entrySet()) {
                if (Integer.valueOf(ridersRankInRace[i]) == entry.getValue()) {
                    ridersRankedPointsInRace.add(entry.getKey());
                    break;
                }
            }
        }
        return ridersRankedPointsInRace.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Get the overall mountain points of riders in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return An array of riders’ mountain points (i.e., the sum of their mountain
     * 
     *         points in all stages of the race), sorted by the total adjusted
     *         elapsed time.
     *         An empty array if there is no result for any stage in the race. These
     *         points should match the riders returned by
     *         {@link #getRidersGeneralClassificationRank(int)}.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public int[] getRidersMountainPointsInRace(int raceId) throws IDNotRecognisedException {
        return null;
    }

    /**
     * Get the ranked list of riders based on the points classification in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return A ranked list of riders’ IDs sorted descending by the sum of their
     *         points in all stages of the race. That is, the first in this list is
     *         the winner (more points). An empty list if there is no result for any
     *         stage in the race.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public int[] getRidersPointClassificationRank(int raceId) throws IDNotRecognisedException {
        return null;
    }

    /**
     * Get the ranked list of riders based on the mountain classification in a race.
     * <p>
     * The state of this CyclingPortal must be unchanged if any
     * exceptions are thrown.
     *
     * @param raceId The ID of the race being queried.
     * @return A ranked list of riders’ IDs sorted descending by the sum of their
     *         mountain points in all stages of the race. That is, the first in this
     *         list is the winner (more points). An empty list if there is no result
     *         for any stage in the race.
     * @throws IDNotRecognisedException If the ID does not match any race in the
     *                                  system.
     */
    @Override
    public int[] getRidersMountainPointClassificationRank(int raceId) throws IDNotRecognisedException {
        return null;
    }
}