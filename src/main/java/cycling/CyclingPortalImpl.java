package cycling;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * In-memory implementation of the staged cycling race management API.
 *
 * <p>The implementation is intentionally single-process and single-threaded.
 * It focuses on domain modelling, validation, ranking, scoring and persistence
 * rather than providing a database, web API or user interface.</p>
 */
public class CyclingPortalImpl implements CyclingPortal {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String WAITING_FOR_RESULTS = "waiting for results";
    private static final Duration ONE_SECOND = Duration.ofSeconds(1);

    private static final Map<StageType, List<Integer>> FINISH_POINTS = new EnumMap<>(StageType.class);
    private static final Map<CheckpointType, List<Integer>> MOUNTAIN_POINTS =
            new EnumMap<>(CheckpointType.class);
    private static final List<Integer> SPRINT_POINTS =
            List.of(20, 17, 15, 13, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);

    static {
        FINISH_POINTS.put(StageType.FLAT,
                List.of(50, 30, 20, 18, 16, 14, 12, 10, 8, 7, 6, 5, 4, 3, 2));
        FINISH_POINTS.put(StageType.MEDIUM_MOUNTAIN,
                List.of(30, 25, 22, 19, 17, 15, 13, 11, 9, 7, 6, 5, 4, 3, 2));
        FINISH_POINTS.put(StageType.HIGH_MOUNTAIN,
                List.of(20, 17, 15, 13, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2));
        FINISH_POINTS.put(StageType.TT,
                List.of(20, 17, 15, 13, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2));

        MOUNTAIN_POINTS.put(CheckpointType.C4, List.of(1));
        MOUNTAIN_POINTS.put(CheckpointType.C3, List.of(2, 1));
        MOUNTAIN_POINTS.put(CheckpointType.C2, List.of(5, 3, 2, 1));
        MOUNTAIN_POINTS.put(CheckpointType.C1, List.of(10, 8, 6, 4, 2, 1));
        MOUNTAIN_POINTS.put(CheckpointType.HC, List.of(20, 15, 12, 10, 8, 6, 4, 2));
    }

    private List<Race> races = new ArrayList<>();
    private List<Stage> stages = new ArrayList<>();
    private List<Checkpoint> checkpoints = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<Rider> riders = new ArrayList<>();
    private List<Result> results = new ArrayList<>();

    private int nextRaceId = 1;
    private int nextStageId = 1;
    private int nextCheckpointId = 1;
    private int nextTeamId = 1;
    private int nextRiderId = 1;

    @Override
    public int[] getRaceIds() {
        return races.stream().mapToInt(Race::getRaceID).toArray();
    }

    @Override
    public int createRace(String name, String description)
            throws IllegalNameException, InvalidNameException {
        validateEntityName(name, "Race");
        if (races.stream().anyMatch(race -> race.getRaceName().equals(name))) {
            throw new IllegalNameException("A race named '" + name + "' already exists");
        }

        Race race = new Race(nextRaceId++, name, description);
        races.add(race);
        return race.getRaceID();
    }

    @Override
    public String viewRaceDetails(int raceId) throws IDNotRecognisedException {
        return requireRace(raceId).getRaceDetails();
    }

    @Override
    public void removeRaceById(int raceId) throws IDNotRecognisedException {
        removeRace(requireRace(raceId));
    }

    @Override
    public int getNumberOfStages(int raceId) throws IDNotRecognisedException {
        return requireRace(raceId).getStages().size();
    }

    @Override
    public int addStageToRace(int raceId, String stageName, String description,
            double length, LocalDateTime startTime, StageType type)
            throws IDNotRecognisedException, IllegalNameException,
            InvalidNameException, InvalidLengthException {
        Race race = requireRace(raceId);
        validateEntityName(stageName, "Stage");
        if (!Double.isFinite(length) || length < 5.0) {
            throw new InvalidLengthException("A stage must have a finite length of at least 5 km");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Stage start time cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Stage type cannot be null");
        }
        if (stages.stream().anyMatch(stage -> stage.getStageName().equals(stageName))) {
            throw new IllegalNameException("A stage named '" + stageName + "' already exists");
        }

        Stage stage = new Stage(nextStageId++, stageName, description, length, startTime, type, race);
        stages.add(stage);
        race.addStage(stage);
        return stage.getStageID();
    }

    @Override
    public int[] getRaceStages(int raceId) throws IDNotRecognisedException {
        return requireRace(raceId).getStages().stream().mapToInt(Stage::getStageID).toArray();
    }

    @Override
    public double getStageLength(int stageId) throws IDNotRecognisedException {
        return requireStage(stageId).getStageLength();
    }

    @Override
    public void removeStageById(int stageId) throws IDNotRecognisedException {
        removeStage(requireStage(stageId));
    }

    @Override
    public int addCategorizedClimbToStage(int stageId, Double location,
            CheckpointType type, Double averageGradient, Double length)
            throws IDNotRecognisedException, InvalidLocationException,
            InvalidStageStateException, InvalidStageTypeException {
        Stage stage = requireStage(stageId);
        validateCheckpointMutation(stage);
        validateCheckpointLocation(stage, location);
        if (type == null || type == CheckpointType.SPRINT) {
            throw new IllegalArgumentException("A categorised climb requires a climb checkpoint type");
        }
        if (averageGradient == null || !Double.isFinite(averageGradient)
                || length == null || !Double.isFinite(length) || length <= 0.0) {
            throw new IllegalArgumentException(
                    "Climb gradient and a finite positive length are required");
        }

        return addCheckpoint(stage, location, type, averageGradient, length);
    }

    @Override
    public int addIntermediateSprintToStage(int stageId, double location)
            throws IDNotRecognisedException, InvalidLocationException,
            InvalidStageStateException, InvalidStageTypeException {
        Stage stage = requireStage(stageId);
        validateCheckpointMutation(stage);
        validateCheckpointLocation(stage, location);
        return addCheckpoint(stage, location, CheckpointType.SPRINT, null, null);
    }

    @Override
    public void removeCheckpoint(int checkpointId)
            throws IDNotRecognisedException, InvalidStageStateException {
        Checkpoint checkpoint = requireCheckpoint(checkpointId);
        Stage stage = checkpoint.getStage();
        if (stage.isWaitingForResults()) {
            throw new InvalidStageStateException("Cannot change checkpoints after stage preparation");
        }

        stage.removeCheckpoint(checkpoint);
        checkpoints.remove(checkpoint);
    }

    @Override
    public void concludeStagePreparation(int stageId)
            throws IDNotRecognisedException, InvalidStageStateException {
        Stage stage = requireStage(stageId);
        if (stage.isWaitingForResults()) {
            throw new InvalidStageStateException("The stage is already " + WAITING_FOR_RESULTS);
        }
        stage.concludePreparation();
    }

    @Override
    public int[] getStageCheckpoints(int stageId) throws IDNotRecognisedException {
        return requireStage(stageId).getCheckpoints().stream()
                .mapToInt(Checkpoint::getCheckpointID)
                .toArray();
    }

    @Override
    public int createTeam(String name, String description)
            throws IllegalNameException, InvalidNameException {
        validateEntityName(name, "Team");
        if (teams.stream().anyMatch(team -> team.getTeamName().equals(name))) {
            throw new IllegalNameException("A team named '" + name + "' already exists");
        }

        Team team = new Team(nextTeamId++, name, description);
        teams.add(team);
        return team.getTeamID();
    }

    @Override
    public void removeTeam(int teamId) throws IDNotRecognisedException {
        Team team = requireTeam(teamId);
        for (Rider rider : new ArrayList<>(team.getRiders())) {
            removeRider(rider);
        }
        teams.remove(team);
    }

    @Override
    public int[] getTeams() {
        return teams.stream().mapToInt(Team::getTeamID).toArray();
    }

    @Override
    public int[] getTeamRiders(int teamId) throws IDNotRecognisedException {
        return requireTeam(teamId).getRiders().stream().mapToInt(Rider::getRiderID).toArray();
    }

    @Override
    public int createRider(int teamID, String name, int yearOfBirth)
            throws IDNotRecognisedException, IllegalArgumentException {
        Team team = requireTeam(teamID);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rider name cannot be null or blank");
        }
        if (yearOfBirth < 1900) {
            throw new IllegalArgumentException("Rider year of birth cannot be before 1900");
        }

        Rider rider = new Rider(nextRiderId++, name, yearOfBirth, team);
        riders.add(rider);
        team.addRider(rider);
        return rider.getRiderID();
    }

    @Override
    public void removeRider(int riderId) throws IDNotRecognisedException {
        removeRider(requireRider(riderId));
    }

    @Override
    public void registerRiderResultsInStage(int stageId, int riderId,
            LocalTime... checkpointTimes)
            throws IDNotRecognisedException, DuplicatedResultException,
            InvalidCheckpointTimesException, InvalidStageStateException {
        Stage stage = requireStage(stageId);
        Rider rider = requireRider(riderId);

        if (!stage.isWaitingForResults()) {
            throw new InvalidStageStateException("The stage is not waiting for results");
        }
        validateCheckpointTimes(stage, checkpointTimes);
        if (findResult(stage, rider) != null) {
            throw new DuplicatedResultException("The rider already has a result for this stage");
        }

        results.add(new Result(rider, stage, List.of(checkpointTimes)));
    }

    @Override
    public LocalTime[] getRiderResultsInStage(int stageId, int riderId)
            throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Rider rider = requireRider(riderId);
        Result result = findResult(stage, rider);
        if (result == null) {
            return new LocalTime[0];
        }

        List<LocalTime> times = result.getCheckpointTimes();
        LocalTime[] response = new LocalTime[times.size() + 1];
        for (int index = 0; index < times.size(); index++) {
            response[index] = times.get(index);
        }
        response[response.length - 1] = toLocalTime(elapsed(result));
        return response;
    }

    @Override
    public LocalTime getRiderAdjustedElapsedTimeInStage(int stageId, int riderId)
            throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Rider rider = requireRider(riderId);
        Duration adjusted = adjustedTimes(stage).get(rider);
        return adjusted == null ? null : toLocalTime(adjusted);
    }

    @Override
    public void deleteRiderResultsInStage(int stageId, int riderId)
            throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Rider rider = requireRider(riderId);
        results.removeIf(result -> result.getStage() == stage && result.getRider() == rider);
    }

    @Override
    public int[] getRidersRankInStage(int stageId) throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        return rankedResults(stage).stream()
                .map(Result::getRider)
                .mapToInt(Rider::getRiderID)
                .toArray();
    }

    @Override
    public LocalTime[] getRankedAdjustedElapsedTimesInStage(int stageId)
            throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Map<Rider, Duration> adjusted = adjustedTimes(stage);
        return rankedResults(stage).stream()
                .map(Result::getRider)
                .map(adjusted::get)
                .map(CyclingPortalImpl::toLocalTime)
                .toArray(LocalTime[]::new);
    }

    @Override
    public int[] getRidersPointsInStage(int stageId) throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Map<Rider, Integer> pointsByRider = stagePoints(stage);
        return rankedResults(stage).stream()
                .map(Result::getRider)
                .mapToInt(rider -> pointsByRider.getOrDefault(rider, 0))
                .toArray();
    }

    @Override
    public int[] getRidersMountainPointsInStage(int stageId)
            throws IDNotRecognisedException {
        Stage stage = requireStage(stageId);
        Map<Rider, Integer> pointsByRider = stageMountainPoints(stage);
        return rankedResults(stage).stream()
                .map(Result::getRider)
                .mapToInt(rider -> pointsByRider.getOrDefault(rider, 0))
                .toArray();
    }

    @Override
    public void eraseCyclingPortal() {
        races.clear();
        stages.clear();
        checkpoints.clear();
        teams.clear();
        riders.clear();
        results.clear();
        nextRaceId = 1;
        nextStageId = 1;
        nextCheckpointId = 1;
        nextTeamId = 1;
        nextRiderId = 1;
    }

    @Override
    public void saveCyclingPortal(String filename) throws IOException {
        Objects.requireNonNull(filename, "filename");
        PortalState state = new PortalState(
                races, stages, checkpoints, teams, riders, results,
                nextRaceId, nextStageId, nextCheckpointId, nextTeamId, nextRiderId);
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filename))) {
            output.writeObject(state);
        }
    }

    @Override
    public void loadCyclingPortal(String filename) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(filename, "filename");
        PortalState state;
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(filename))) {
            Object loaded = input.readObject();
            if (!(loaded instanceof PortalState portalState)) {
                throw new IOException("The file does not contain a cycling portal snapshot");
            }
            state = portalState;
        }

        // Assignment happens only after the complete snapshot has been read.
        races = new ArrayList<>(state.races);
        stages = new ArrayList<>(state.stages);
        checkpoints = new ArrayList<>(state.checkpoints);
        teams = new ArrayList<>(state.teams);
        riders = new ArrayList<>(state.riders);
        results = new ArrayList<>(state.results);
        nextRaceId = state.nextRaceId;
        nextStageId = state.nextStageId;
        nextCheckpointId = state.nextCheckpointId;
        nextTeamId = state.nextTeamId;
        nextRiderId = state.nextRiderId;
    }

    @Override
    public void removeRaceByName(String name) throws NameNotRecognisedException {
        Race race = races.stream()
                .filter(candidate -> candidate.getRaceName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NameNotRecognisedException(
                        "No race named '" + name + "' exists"));
        removeRace(race);
    }

    @Override
    public int[] getRidersGeneralClassificationRank(int raceId)
            throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        Map<Rider, Duration> totals = raceAdjustedTimes(race);
        return totals.entrySet().stream()
                .sorted(Map.Entry.<Rider, Duration>comparingByValue()
                        .thenComparingInt(entry -> entry.getKey().getRiderID()))
                .map(Map.Entry::getKey)
                .mapToInt(Rider::getRiderID)
                .toArray();
    }

    @Override
    public LocalTime[] getGeneralClassificationTimesInRace(int raceId)
            throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        Map<Rider, Duration> totals = raceAdjustedTimes(race);
        return totals.entrySet().stream()
                .sorted(Map.Entry.<Rider, Duration>comparingByValue()
                        .thenComparingInt(entry -> entry.getKey().getRiderID()))
                .map(Map.Entry::getValue)
                .map(CyclingPortalImpl::toLocalTime)
                .toArray(LocalTime[]::new);
    }

    @Override
    public int[] getRidersPointsInRace(int raceId) throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        Map<Integer, Integer> totals = racePointTotals(race, this::stagePoints);
        int[] generalClassification = getRidersGeneralClassificationRank(raceId);
        int[] points = new int[generalClassification.length];
        for (int index = 0; index < generalClassification.length; index++) {
            points[index] = totals.getOrDefault(generalClassification[index], 0);
        }
        return points;
    }

    @Override
    public int[] getRidersMountainPointsInRace(int raceId)
            throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        Map<Integer, Integer> totals = racePointTotals(race, this::stageMountainPoints);
        int[] generalClassification = getRidersGeneralClassificationRank(raceId);
        int[] points = new int[generalClassification.length];
        for (int index = 0; index < generalClassification.length; index++) {
            points[index] = totals.getOrDefault(generalClassification[index], 0);
        }
        return points;
    }

    @Override
    public int[] getRidersPointClassificationRank(int raceId)
            throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        return rankByPoints(race, racePointTotals(race, this::stagePoints));
    }

    @Override
    public int[] getRidersMountainPointClassificationRank(int raceId)
            throws IDNotRecognisedException {
        Race race = requireRace(raceId);
        return rankByPoints(race, racePointTotals(race, this::stageMountainPoints));
    }

    private void validateEntityName(String name, String entity) throws InvalidNameException {
        if (name == null || name.isBlank() || name.length() > 30
                || name.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidNameException(
                    entity + " name must be one non-blank word of at most 30 characters");
        }
    }

    private Race requireRace(int raceId) throws IDNotRecognisedException {
        return races.stream()
                .filter(race -> race.getRaceID() == raceId)
                .findFirst()
                .orElseThrow(() -> new IDNotRecognisedException("Invalid race ID: " + raceId));
    }

    private Stage requireStage(int stageId) throws IDNotRecognisedException {
        return stages.stream()
                .filter(stage -> stage.getStageID() == stageId)
                .findFirst()
                .orElseThrow(() -> new IDNotRecognisedException("Invalid stage ID: " + stageId));
    }

    private Checkpoint requireCheckpoint(int checkpointId) throws IDNotRecognisedException {
        return checkpoints.stream()
                .filter(checkpoint -> checkpoint.getCheckpointID() == checkpointId)
                .findFirst()
                .orElseThrow(() -> new IDNotRecognisedException(
                        "Invalid checkpoint ID: " + checkpointId));
    }

    private Team requireTeam(int teamId) throws IDNotRecognisedException {
        return teams.stream()
                .filter(team -> team.getTeamID() == teamId)
                .findFirst()
                .orElseThrow(() -> new IDNotRecognisedException("Invalid team ID: " + teamId));
    }

    private Rider requireRider(int riderId) throws IDNotRecognisedException {
        return riders.stream()
                .filter(rider -> rider.getRiderID() == riderId)
                .findFirst()
                .orElseThrow(() -> new IDNotRecognisedException("Invalid rider ID: " + riderId));
    }

    private void removeRace(Race race) {
        for (Stage stage : new ArrayList<>(race.getStages())) {
            removeStage(stage);
        }
        races.remove(race);
    }

    private void removeStage(Stage stage) {
        checkpoints.removeIf(checkpoint -> checkpoint.getStage() == stage);
        results.removeIf(result -> result.getStage() == stage);
        stage.getRace().removeStage(stage);
        stages.remove(stage);
    }

    private void removeRider(Rider rider) {
        results.removeIf(result -> result.getRider() == rider);
        rider.getTeam().removeRider(rider);
        riders.remove(rider);
    }

    private void validateCheckpointMutation(Stage stage)
            throws InvalidStageStateException, InvalidStageTypeException {
        if (stage.isWaitingForResults()) {
            throw new InvalidStageStateException("Cannot add checkpoints after stage preparation");
        }
        if (stage.getStageType() == StageType.TT) {
            throw new InvalidStageTypeException("Time-trial stages cannot contain checkpoints");
        }
    }

    private void validateCheckpointLocation(Stage stage, Double location)
            throws InvalidLocationException {
        if (location == null || !Double.isFinite(location)
                || location <= 0.0 || location > stage.getStageLength()) {
            throw new InvalidLocationException(
                    "Checkpoint location must be greater than 0 and within the stage");
        }
    }

    private int addCheckpoint(Stage stage, double location, CheckpointType type,
            Double averageGradient, Double length) {
        Checkpoint checkpoint = new Checkpoint(
                nextCheckpointId++, location, type, averageGradient, length, stage);
        checkpoints.add(checkpoint);
        stage.addCheckpoint(checkpoint);
        return checkpoint.getCheckpointID();
    }

    private void validateCheckpointTimes(Stage stage, LocalTime[] checkpointTimes)
            throws InvalidCheckpointTimesException {
        int expectedLength = stage.getCheckpoints().size() + 2;
        if (checkpointTimes == null || checkpointTimes.length != expectedLength) {
            throw new InvalidCheckpointTimesException(
                    "Expected " + expectedLength + " ordered times including start and finish");
        }
        for (int index = 0; index < checkpointTimes.length; index++) {
            if (checkpointTimes[index] == null) {
                throw new InvalidCheckpointTimesException("Checkpoint times cannot contain null");
            }
            if (index > 0 && checkpointTimes[index].isBefore(checkpointTimes[index - 1])) {
                throw new InvalidCheckpointTimesException(
                        "Checkpoint times must be in chronological order");
            }
        }
    }

    private Result findResult(Stage stage, Rider rider) {
        return results.stream()
                .filter(result -> result.getStage() == stage && result.getRider() == rider)
                .findFirst()
                .orElse(null);
    }

    private List<Result> resultsForStage(Stage stage) {
        return results.stream().filter(result -> result.getStage() == stage).toList();
    }

    private List<Result> rankedResults(Stage stage) {
        return resultsForStage(stage).stream()
                .sorted(Comparator.comparing(CyclingPortalImpl::elapsed)
                        .thenComparingInt(result -> result.getRider().getRiderID()))
                .toList();
    }

    private static Duration elapsed(Result result) {
        List<LocalTime> times = result.getCheckpointTimes();
        return durationBetween(times.get(0), times.get(times.size() - 1));
    }

    private static Duration durationBetween(LocalTime start, LocalTime end) {
        Duration duration = Duration.between(start, end);
        return duration.isNegative() ? duration.plusHours(24) : duration;
    }

    private static LocalTime toLocalTime(Duration duration) {
        return LocalTime.MIDNIGHT.plusNanos(duration.toNanos());
    }

    private Map<Rider, Duration> adjustedTimes(Stage stage) {
        List<Result> ranked = rankedResults(stage);
        Map<Rider, Duration> adjusted = new LinkedHashMap<>();
        if (stage.getStageType() == StageType.TT) {
            ranked.forEach(result -> adjusted.put(result.getRider(), elapsed(result)));
            return adjusted;
        }

        Duration previousActual = null;
        Duration groupTime = null;
        for (Result result : ranked) {
            Duration actual = elapsed(result);
            if (previousActual == null
                    || actual.minus(previousActual).compareTo(ONE_SECOND) >= 0) {
                groupTime = actual;
            }
            adjusted.put(result.getRider(), groupTime);
            previousActual = actual;
        }
        return adjusted;
    }

    private Map<Rider, Integer> stagePoints(Stage stage) {
        List<Result> finishRank = rankedResults(stage);
        Map<Rider, Integer> totals = initialisePointTotals(finishRank);
        applyPoints(finishRank, FINISH_POINTS.get(stage.getStageType()), totals);

        List<Checkpoint> stageCheckpoints = stage.getCheckpoints();
        for (int checkpointIndex = 0; checkpointIndex < stageCheckpoints.size(); checkpointIndex++) {
            if (stageCheckpoints.get(checkpointIndex).getCheckpointType() == CheckpointType.SPRINT) {
                applyPoints(rankAtCheckpoint(stage, checkpointIndex), SPRINT_POINTS, totals);
            }
        }
        return totals;
    }

    private Map<Rider, Integer> stageMountainPoints(Stage stage) {
        List<Result> finishRank = rankedResults(stage);
        Map<Rider, Integer> totals = initialisePointTotals(finishRank);
        List<Checkpoint> stageCheckpoints = stage.getCheckpoints();
        for (int checkpointIndex = 0; checkpointIndex < stageCheckpoints.size(); checkpointIndex++) {
            Checkpoint checkpoint = stageCheckpoints.get(checkpointIndex);
            List<Integer> scale = MOUNTAIN_POINTS.get(checkpoint.getCheckpointType());
            if (scale != null) {
                applyPoints(rankAtCheckpoint(stage, checkpointIndex), scale, totals);
            }
        }
        return totals;
    }

    private Map<Rider, Integer> initialisePointTotals(List<Result> rankedResults) {
        Map<Rider, Integer> totals = new LinkedHashMap<>();
        rankedResults.forEach(result -> totals.put(result.getRider(), 0));
        return totals;
    }

    private void applyPoints(List<Result> ranking, List<Integer> scale,
            Map<Rider, Integer> totals) {
        int awardedPlaces = Math.min(ranking.size(), scale.size());
        for (int index = 0; index < awardedPlaces; index++) {
            Rider rider = ranking.get(index).getRider();
            totals.merge(rider, scale.get(index), Integer::sum);
        }
    }

    private List<Result> rankAtCheckpoint(Stage stage, int checkpointIndex) {
        return resultsForStage(stage).stream()
                .sorted(Comparator
                        .comparing((Result result) -> checkpointElapsed(result, checkpointIndex))
                        .thenComparingInt(result -> result.getRider().getRiderID()))
                .toList();
    }

    private Duration checkpointElapsed(Result result, int checkpointIndex) {
        List<LocalTime> times = result.getCheckpointTimes();
        return durationBetween(times.get(0), times.get(checkpointIndex + 1));
    }

    private Map<Rider, Duration> raceAdjustedTimes(Race race) {
        Map<Rider, Duration> totals = new LinkedHashMap<>();
        for (Stage stage : race.getStages()) {
            adjustedTimes(stage).forEach(
                    (rider, duration) -> totals.merge(rider, duration, Duration::plus));
        }
        return totals;
    }

    private Map<Integer, Integer> racePointTotals(
            Race race, StagePointCalculator calculator) {
        Map<Integer, Integer> totals = new HashMap<>();
        for (Stage stage : race.getStages()) {
            calculator.calculate(stage).forEach(
                    (rider, points) -> totals.merge(rider.getRiderID(), points, Integer::sum));
        }
        return totals;
    }

    private int[] rankByPoints(Race race, Map<Integer, Integer> totals)
            throws IDNotRecognisedException {
        int[] generalClassification = getRidersGeneralClassificationRank(race.getRaceID());
        Map<Integer, Integer> generalClassificationPosition = new HashMap<>();
        for (int index = 0; index < generalClassification.length; index++) {
            generalClassificationPosition.put(generalClassification[index], index);
        }

        Set<Integer> riderIds = new LinkedHashSet<>();
        for (Stage stage : race.getStages()) {
            resultsForStage(stage).forEach(result -> riderIds.add(result.getRider().getRiderID()));
        }

        return riderIds.stream()
                .sorted(Comparator
                        .comparingInt((Integer riderId) -> totals.getOrDefault(riderId, 0))
                        .reversed()
                        .thenComparingInt(riderId ->
                                generalClassificationPosition.getOrDefault(riderId, Integer.MAX_VALUE))
                        .thenComparingInt(Integer::intValue))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    @FunctionalInterface
    private interface StagePointCalculator {
        Map<Rider, Integer> calculate(Stage stage);
    }

    private static final class PortalState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final List<Race> races;
        private final List<Stage> stages;
        private final List<Checkpoint> checkpoints;
        private final List<Team> teams;
        private final List<Rider> riders;
        private final List<Result> results;
        private final int nextRaceId;
        private final int nextStageId;
        private final int nextCheckpointId;
        private final int nextTeamId;
        private final int nextRiderId;

        private PortalState(List<Race> races, List<Stage> stages,
                List<Checkpoint> checkpoints, List<Team> teams,
                List<Rider> riders, List<Result> results,
                int nextRaceId, int nextStageId, int nextCheckpointId,
                int nextTeamId, int nextRiderId) {
            this.races = new ArrayList<>(races);
            this.stages = new ArrayList<>(stages);
            this.checkpoints = new ArrayList<>(checkpoints);
            this.teams = new ArrayList<>(teams);
            this.riders = new ArrayList<>(riders);
            this.results = new ArrayList<>(results);
            this.nextRaceId = nextRaceId;
            this.nextStageId = nextStageId;
            this.nextCheckpointId = nextCheckpointId;
            this.nextTeamId = nextTeamId;
            this.nextRiderId = nextRiderId;
        }
    }
}
