package cycling;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class HelperMethods {

    // Function to get Stage by ID
    public Stage getStageById(int stageID, List<Stage> stages) throws IDNotRecognisedException {
        Iterator<Stage> iterator = stages.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (stage.getStageID() == stageID) {
                return stage;
            }
        }
        throw new IDNotRecognisedException("Invalid stage ID: " + stageID);
    }

    // Function to get Rider by ID
    public Rider getRiderById(int riderID, List<Rider> riders) throws IDNotRecognisedException {
        Iterator<Rider> iterator = riders.iterator();
        while (iterator.hasNext()) {
            Rider rider = iterator.next();
            if (rider.getRiderID() == riderID) {
                return rider;
            }
        }
        throw new IDNotRecognisedException("Invalid rider ID: " + riderID);
    }

    public LocalTime getRiderElapsedTimeInStage(int stageId, int riderId, List<Stage> stages, List<Rider> riders,
            List<Result> results)
            throws IDNotRecognisedException {
        CyclingPortalImpl portal = new CyclingPortalImpl();
        Stage stage = getStageById(stageId, stages);
        Rider rider = getRiderById(riderId, riders);

        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            if (result.getStage() == stage && result.getRider() == rider) {
                LocalTime riderElapsedTime = portal.getRiderResultsInStage(stageId,
                        riderId)[portal.getRiderResultsInStage(stageId, riderId).length - 1];
                return riderElapsedTime;
            }
        }
        return null;
    }

    // Define a map to store points for different types of stages
    private static final Map<StageType, List<Integer>> stagePointsMap = new EnumMap<>(StageType.class);

    // Static initialization block to populate the stagePointsMap
    static {
        stagePointsMap.put(StageType.FLAT, List.of(50, 30, 20, 18, 16, 14, 12, 10, 8, 7, 6, 5, 4, 3, 2));
        stagePointsMap.put(StageType.MEDIUM_MOUNTAIN, List.of(30, 25, 22, 19, 17, 15, 13, 11, 9, 7, 6, 5, 4, 3, 2));
        stagePointsMap.put(StageType.HIGH_MOUNTAIN, List.of(20, 17, 15, 13, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2));
        stagePointsMap.put(StageType.TT, List.of(20, 17, 15, 13, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2));
    }

    // Method to get points of rider ranks in a stage as per generation
    // classifications
    public int[] getPointsAsPerRiderRanksInStage(int stageID, List<LocalTime> rankedElapsedTimes,
            List<Stage> stages) {
        // Get the type of the stage based on its ID
        StageType stageType = getStageType(stageID, stages);
        // Get the corresponding points list based on the stage type
        List<Integer> points = stagePointsMap.getOrDefault(stageType, Collections.emptyList());
        // Calculate points for riders based on elapsed times and points list
        return calculatePoints(rankedElapsedTimes, points);
    }

    // Method to determine the type of the stage based on its ID
    private StageType getStageType(int stageID, List<Stage> stages) {
        for (Stage stage : stages) {
            // If the stage ID matches, return the stage type
            if (stage.getStageID() == stageID) {
                return stage.getStageType();
            }
        }
        // Return null if no matching stage ID is found
        return null;
    }

    // Method to calculate points based on ranked elapsed times and points list
    private int[] calculatePoints(List<LocalTime> rankedElapsedTimes, List<Integer> points) {
        // Initialize an array to store calculated points
        int[] calculatedPoints = new int[rankedElapsedTimes.size()];

        // Iterate over ranked elapsed times
        for (int i = 0; i < rankedElapsedTimes.size(); i++) {
            // If there are no more points available, assign 0 points
            if (i >= points.size()) {
                calculatedPoints[i] = 0;
            } else {
                // Calculate points based on the corresponding value in points list
                calculatedPoints[i] = points.get(i);
            }
        }
        return calculatedPoints;
    }

    // Static initialization block to populate the stagePointsMap
    private static final Map<CheckpointType, List<Integer>> stageMountainPointsMap = new EnumMap<>(
            CheckpointType.class);

    // Static initialization block to populate the stagePointsMap
    static {
        stageMountainPointsMap.put(CheckpointType.C4, List.of(1));
        stageMountainPointsMap.put(CheckpointType.C3, List.of(2, 1));
        stageMountainPointsMap.put(CheckpointType.C2, List.of(5, 3, 2, 1));
        stageMountainPointsMap.put(CheckpointType.C1, List.of(10, 8, 6, 4, 2, 1));
        stageMountainPointsMap.put(CheckpointType.HC, List.of(20, 15, 12, 10, 8, 6, 4, 2));
    }

    public int[] getMountainPointsAsPerRiderRanksInStage(int stageID, List<LocalTime> rankedElapsedTimes,
            List<Stage> stages, List<Checkpoint> checkpoints) {
        int[] points = new int[0];
        Iterator<Stage> iterator1 = stages.iterator();
        while (iterator1.hasNext()) {
            Stage stage = iterator1.next();
            if (stage.getStageID() == stageID) {
                Iterator<Checkpoint> iterator2 = stage.getCheckpoints().iterator();
                while (iterator2.hasNext()) {
                    Checkpoint checkpoint = iterator2.next();
                    CheckpointType checkpointType = checkpoint.getCheckpointType();
                    List<Integer> p = stagePointsMap.getOrDefault(checkpointType, Collections.emptyList());
                    int[] points2 = calculatePoints(rankedElapsedTimes, p);
                    if (points.length == 0) {
                        int[] points1 = new int[points2.length];
                        points = IntStream.range(0, points1.length)
                                .map(i -> points1[i] + points2[i]).toArray();
                    } else {
                        int[] points1 = points;
                        points = IntStream.range(0, points1.length)
                                .map(i -> points1[i] + points2[i]).toArray();
                    }
                }
            }
        }
        return points;
    }

    public LinkedHashMap<LocalTime, Integer> populateAdjustedElapsedTimeMap(int[] rankedRidersInStage,
            LocalTime[] rankedElapsedTimeInStage,
            LinkedHashMap<LocalTime, Integer> adjustedElapsedTimeRiderIdInRaceMap) {
        for (int i = 0; i < rankedRidersInStage.length; i++) {
            for (Map.Entry<LocalTime, Integer> entry : adjustedElapsedTimeRiderIdInRaceMap.entrySet()) {
                if (entry.getValue().equals(rankedRidersInStage[i])) {
                    Integer tempVariableForRiderID = entry.getValue();
                    LocalTime tempAdjustedElapsedTime = rankedElapsedTimeInStage[i];
                    LocalTime oldAdjustedElapsedTime = entry.getKey();
                    LocalTime newAdjustedElapsedTime = oldAdjustedElapsedTime
                            .plusHours(tempAdjustedElapsedTime.getHour())
                            .plusMinutes(tempAdjustedElapsedTime.getMinute())
                            .plusSeconds(tempAdjustedElapsedTime.getSecond());
                    adjustedElapsedTimeRiderIdInRaceMap.remove(entry.getKey());
                    adjustedElapsedTimeRiderIdInRaceMap.put(newAdjustedElapsedTime, tempVariableForRiderID);
                }
            }
            if (!adjustedElapsedTimeRiderIdInRaceMap.containsValue(rankedRidersInStage[i])) {
                adjustedElapsedTimeRiderIdInRaceMap.put(rankedElapsedTimeInStage[i],
                        Integer.valueOf(rankedRidersInStage[i]));
            }
        }
        return adjustedElapsedTimeRiderIdInRaceMap;
    }

    public int[] getOrderedRidersRankInRace(LinkedHashMap<LocalTime, Integer> adjustedElapsedTimeRiderIdInRaceMap) {
        // Convert HashMap to TreeMap for sorting
        TreeMap<LocalTime, Integer> adjustedElapsedTimeRiderIdInRaceTree = new TreeMap<>(
                adjustedElapsedTimeRiderIdInRaceMap);
        // Retrieve the entries as a sorted set
        Set<Map.Entry<LocalTime, Integer>> sortedAdjustedElapsedTimeRiderIdInRaceMap = adjustedElapsedTimeRiderIdInRaceTree
                .entrySet();

        // getting ordered array of the rider IDs based on their finish time
        ArrayList<Integer> ridersRankInRace = new ArrayList<>();
        for (Map.Entry<LocalTime, Integer> entry : sortedAdjustedElapsedTimeRiderIdInRaceMap) {
            ridersRankInRace.add(entry.getValue());
        }
        return ridersRankInRace.stream().mapToInt(Integer::intValue).toArray();
    }

    public LinkedHashMap<Integer, Integer> calculateRiderPoints(int raceId, List<Stage> stages)
            throws IDNotRecognisedException {
        CyclingPortalImpl portal = new CyclingPortalImpl();
        LinkedHashMap<Integer, Integer> riderPointRiderIdMap = new LinkedHashMap<>();
        Iterator<Stage> iterator1 = stages.iterator();
        while (iterator1.hasNext()) {
            Stage stage = iterator1.next();
            if (stage.getRace().getRaceID() == raceId) {
                int[] rankedRiderIdInStage = portal.getRidersRankInStage(stage.getStageID());
                int[] rankedRiderPointInStage = portal.getRidersPointsInStage(stage.getStageID());
                for (int i = 0; i < rankedRiderIdInStage.length; i++) {
                    for (Map.Entry<Integer, Integer> entry : riderPointRiderIdMap.entrySet()) {
                        if (rankedRiderIdInStage[i] == entry.getValue()) {
                            Integer tempVariableForTotalPointsOfRiderInRace = entry.getKey()
                                    + rankedRiderPointInStage[i];
                            Integer tempVariableForRiderID = entry.getValue();
                            riderPointRiderIdMap.remove(entry.getKey());
                            riderPointRiderIdMap.put(tempVariableForTotalPointsOfRiderInRace, tempVariableForRiderID);
                        }
                    }
                    if (!riderPointRiderIdMap.containsValue(rankedRiderIdInStage[i])) {
                        riderPointRiderIdMap.put(rankedRiderPointInStage[i], Integer.valueOf(rankedRiderIdInStage[i]));
                    }
                }
            }
        }
        return riderPointRiderIdMap;
    }

    public ArrayList<Integer> getRidersRankedPoints(int[] ridersRankInRace,
            LinkedHashMap<Integer, Integer> riderPointRiderIdMap) {
        ArrayList<Integer> ridersRankedPointsInRace = new ArrayList<>();
        for (int i = 0; i < ridersRankInRace.length; i++) {
            for (Map.Entry<Integer, Integer> entry : riderPointRiderIdMap.entrySet()) {
                if (Integer.valueOf(ridersRankInRace[i]) == entry.getValue()) {
                    ridersRankedPointsInRace.add(entry.getKey());
                }
            }
        }
        return ridersRankedPointsInRace;
    }

    public LinkedHashMap<Integer, Integer> calculateRiderMountainPoints(int raceId, List<Stage> stages)
            throws IDNotRecognisedException {
        CyclingPortalImpl portal = new CyclingPortalImpl();
        LinkedHashMap<Integer, Integer> riderMountainPointRiderIdMap = new LinkedHashMap<>();
        Iterator<Stage> iterator1 = stages.iterator();
        while (iterator1.hasNext()) {
            Stage stage = iterator1.next();
            if (stage.getRace().getRaceID() == raceId) {
                int[] rankedRiderIdInStage = portal.getRidersRankInStage(stage.getStageID());
                int[] rankedRiderPointInStage = portal.getRidersMountainPointsInStage(stage.getStageID());
                // int[] rankedRiderPointInStage =
                // portal.getRidersMountainPointsInStage(stage.getStageID());
                for (int i = 0; i < rankedRiderIdInStage.length; i++) {
                    for (Map.Entry<Integer, Integer> entry : riderMountainPointRiderIdMap.entrySet()) {
                        if (entry.getValue().equals(rankedRiderIdInStage[i])) {
                            Integer tempVariableForTotalPointsOfRiderInRace = entry.getKey()
                                    + rankedRiderPointInStage[i];
                            Integer tempVariableForRiderID = entry.getValue();
                            riderMountainPointRiderIdMap.remove(entry.getKey());
                            riderMountainPointRiderIdMap.put(tempVariableForTotalPointsOfRiderInRace,
                                    tempVariableForRiderID);
                        }
                    }
                    if (!riderMountainPointRiderIdMap.containsValue(rankedRiderIdInStage[i])) {
                        riderMountainPointRiderIdMap.put(rankedRiderPointInStage[i],
                                Integer.valueOf(rankedRiderIdInStage[i]));
                    }
                }
            }
        }
        return riderMountainPointRiderIdMap;
    }

    public ArrayList<Integer> getRidersRankedMountainPoints(int[] ridersRankInRace,
            LinkedHashMap<Integer, Integer> riderMountainPointRiderIdMap) {
        ArrayList<Integer> ridersRankedPointsInRace = new ArrayList<>();
        for (int i = 0; i < ridersRankInRace.length; i++) {
            for (Map.Entry<Integer, Integer> entry : riderMountainPointRiderIdMap.entrySet()) {
                if (Integer.valueOf(ridersRankInRace[i]) == entry.getValue()) {
                    ridersRankedPointsInRace.add(entry.getKey());
                    break;
                }
            }
        }
        return ridersRankedPointsInRace;
    }
}