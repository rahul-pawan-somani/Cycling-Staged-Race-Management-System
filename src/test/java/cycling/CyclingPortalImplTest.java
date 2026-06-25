package cycling;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CyclingPortalImplTest {
    private CyclingPortalImpl portal;

    @BeforeEach
    void setUp() {
        portal = new CyclingPortalImpl();
    }

    @Test
    void createsViewsAndDeletesRaceWithRelatedStages() throws Exception {
        int raceId = portal.createRace("Tour", "A staged road race");
        int stageId = portal.addStageToRace(
                raceId, "Opening", "Opening stage", 5.0,
                LocalDateTime.of(2026, 7, 1, 9, 0), StageType.FLAT);

        String details = portal.viewRaceDetails(raceId);
        assertAll(
                () -> assertArrayEquals(new int[] {raceId}, portal.getRaceIds()),
                () -> assertTrue(details.contains("Number of Stages: 1")),
                () -> assertTrue(details.contains("Total Length: 5.0 km")));

        portal.removeRaceById(raceId);

        assertAll(
                () -> assertArrayEquals(new int[0], portal.getRaceIds()),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getStageLength(stageId)));
    }

    @Test
    void validatesRaceNamesByValueAndFormat() throws Exception {
        portal.createRace(new String("Tour"), "First");

        assertAll(
                () -> assertThrows(IllegalNameException.class,
                        () -> portal.createRace(new String("Tour"), "Duplicate")),
                () -> assertThrows(InvalidNameException.class,
                        () -> portal.createRace("Tour France", "Whitespace")),
                () -> assertThrows(InvalidNameException.class,
                        () -> portal.createRace("", "Blank")));
    }

    @Test
    void createsAndDeletesAnyStageWithoutCorruptingRaceState() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int firstStage = portal.addStageToRace(
                raceId, "First", null, 20.0, LocalDateTime.now(), StageType.FLAT);
        int secondStage = portal.addStageToRace(
                raceId, "Second", null, 30.0, LocalDateTime.now(), StageType.FLAT);
        int checkpointId = portal.addIntermediateSprintToStage(secondStage, 10.0);
        portal.concludeStagePreparation(secondStage);
        int teamId = portal.createTeam("Velocity", null);
        int riderId = portal.createRider(teamId, "Rider One", 2000);
        portal.registerRiderResultsInStage(
                secondStage, riderId,
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));

        portal.removeStageById(secondStage);

        assertAll(
                () -> assertArrayEquals(new int[] {firstStage}, portal.getRaceStages(raceId)),
                () -> assertEquals(1, portal.getNumberOfStages(raceId)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getStageLength(secondStage)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.removeCheckpoint(checkpointId)),
                () -> assertThrows(InvalidLengthException.class,
                        () -> portal.addStageToRace(
                                raceId, "Short", null, 4.99,
                                LocalDateTime.now(), StageType.FLAT)),
                () -> assertThrows(InvalidLengthException.class,
                        () -> portal.addStageToRace(
                                raceId, "Invalid", null, Double.NaN,
                                LocalDateTime.now(), StageType.FLAT)));
    }

    @Test
    void managesTeamsAndCascadesRiderResultsOnRemoval() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int stageId = portal.addStageToRace(
                raceId, "Stage", null, 20.0, LocalDateTime.now(), StageType.FLAT);
        portal.concludeStagePreparation(stageId);
        int teamId = portal.createTeam("Velocity", "Test team");
        int riderId = portal.createRider(teamId, "Rider One", 2000);
        portal.registerRiderResultsInStage(
                stageId, riderId, LocalTime.of(9, 0), LocalTime.of(10, 0));

        portal.removeTeam(teamId);

        assertAll(
                () -> assertArrayEquals(new int[0], portal.getTeams()),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getTeamRiders(teamId)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getRiderResultsInStage(stageId, riderId)));
    }

    @Test
    void validatesRiderInput() throws Exception {
        int teamId = portal.createTeam("Velocity", null);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> portal.createRider(teamId, " ", 2000)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> portal.createRider(teamId, "Young Rider", 1899)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.createRider(999, "Unknown Team", 2000)));
    }

    @Test
    void ordersCheckpointsByLocationAndLocksThemAfterPreparation() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int stageId = portal.addStageToRace(
                raceId, "Road", null, 100.0, LocalDateTime.now(), StageType.FLAT);
        int lateSprint = portal.addIntermediateSprintToStage(stageId, 80.0);
        int earlyClimb = portal.addCategorizedClimbToStage(
                stageId, 20.0, CheckpointType.C3, 6.0, 4.0);

        assertArrayEquals(new int[] {earlyClimb, lateSprint},
                portal.getStageCheckpoints(stageId));

        portal.removeCheckpoint(lateSprint);
        assertArrayEquals(new int[] {earlyClimb}, portal.getStageCheckpoints(stageId));

        portal.concludeStagePreparation(stageId);
        assertThrows(InvalidStageStateException.class,
                () -> portal.removeCheckpoint(earlyClimb));
    }

    @Test
    void rejectsCheckpointsForTimeTrialsAndInvalidLocations() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int timeTrial = portal.addStageToRace(
                raceId, "Trial", null, 20.0, LocalDateTime.now(), StageType.TT);
        int roadStage = portal.addStageToRace(
                raceId, "Road", null, 20.0, LocalDateTime.now(), StageType.FLAT);

        assertAll(
                () -> assertThrows(InvalidStageTypeException.class,
                        () -> portal.addIntermediateSprintToStage(timeTrial, 10.0)),
                () -> assertThrows(InvalidLocationException.class,
                        () -> portal.addIntermediateSprintToStage(roadStage, 0.0)),
                () -> assertThrows(InvalidLocationException.class,
                        () -> portal.addIntermediateSprintToStage(roadStage, 21.0)),
                () -> assertThrows(InvalidLocationException.class,
                        () -> portal.addIntermediateSprintToStage(
                                roadStage, Double.NaN)));
    }

    @Test
    void recordsResultsAndRejectsDuplicatesWrongCountsAndUnorderedTimes() throws Exception {
        StageFixture fixture = createRoadStageWithRider();
        portal.addIntermediateSprintToStage(fixture.stageId, 10.0);
        portal.concludeStagePreparation(fixture.stageId);

        portal.registerRiderResultsInStage(
                fixture.stageId, fixture.riderId,
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));

        assertAll(
                () -> assertArrayEquals(
                        new LocalTime[] {
                            LocalTime.of(9, 0),
                            LocalTime.of(9, 30),
                            LocalTime.of(10, 0),
                            LocalTime.of(1, 0)
                        },
                        portal.getRiderResultsInStage(fixture.stageId, fixture.riderId)),
                () -> assertThrows(DuplicatedResultException.class,
                        () -> portal.registerRiderResultsInStage(
                                fixture.stageId, fixture.riderId,
                                LocalTime.of(9, 0),
                                LocalTime.of(9, 30),
                                LocalTime.of(10, 0))));

        int secondRider = portal.createRider(fixture.teamId, "Rider Two", 2001);
        assertAll(
                () -> assertThrows(InvalidCheckpointTimesException.class,
                        () -> portal.registerRiderResultsInStage(
                                fixture.stageId, secondRider,
                                LocalTime.of(9, 0), LocalTime.of(10, 0))),
                () -> assertThrows(InvalidCheckpointTimesException.class,
                        () -> portal.registerRiderResultsInStage(
                                fixture.stageId, secondRider,
                                LocalTime.of(9, 0),
                                LocalTime.of(10, 0),
                                LocalTime.of(9, 30))));
    }

    @Test
    void ranksAllRidersIncludingEqualElapsedTimes() throws Exception {
        StageFixture fixture = createRoadStageWithRider();
        int secondRider = portal.createRider(fixture.teamId, "Rider Two", 2001);
        int thirdRider = portal.createRider(fixture.teamId, "Rider Three", 2002);
        portal.concludeStagePreparation(fixture.stageId);
        portal.registerRiderResultsInStage(
                fixture.stageId, fixture.riderId,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        portal.registerRiderResultsInStage(
                fixture.stageId, secondRider,
                LocalTime.of(9, 5), LocalTime.of(10, 5));
        portal.registerRiderResultsInStage(
                fixture.stageId, thirdRider,
                LocalTime.of(9, 0), LocalTime.of(9, 50));

        assertArrayEquals(
                new int[] {thirdRider, fixture.riderId, secondRider},
                portal.getRidersRankInStage(fixture.stageId));
    }

    @Test
    void appliesChainedSubSecondTimeAdjustmentsExceptForTimeTrials() throws Exception {
        StageFixture fixture = createRoadStageWithRider();
        int secondRider = portal.createRider(fixture.teamId, "Rider Two", 2001);
        int thirdRider = portal.createRider(fixture.teamId, "Rider Three", 2002);
        portal.concludeStagePreparation(fixture.stageId);
        portal.registerRiderResultsInStage(
                fixture.stageId, fixture.riderId,
                LocalTime.NOON, LocalTime.of(13, 0, 0, 0));
        portal.registerRiderResultsInStage(
                fixture.stageId, secondRider,
                LocalTime.NOON, LocalTime.of(13, 0, 0, 500_000_000));
        portal.registerRiderResultsInStage(
                fixture.stageId, thirdRider,
                LocalTime.NOON, LocalTime.of(13, 0, 1, 0));

        assertArrayEquals(
                new LocalTime[] {LocalTime.of(1, 0), LocalTime.of(1, 0), LocalTime.of(1, 0)},
                portal.getRankedAdjustedElapsedTimesInStage(fixture.stageId));

        int trialStage = portal.addStageToRace(
                fixture.raceId, "Trial", null, 20.0, LocalDateTime.now(), StageType.TT);
        portal.concludeStagePreparation(trialStage);
        portal.registerRiderResultsInStage(
                trialStage, fixture.riderId, LocalTime.NOON, LocalTime.of(13, 0));
        portal.registerRiderResultsInStage(
                trialStage, secondRider,
                LocalTime.NOON, LocalTime.of(13, 0, 0, 500_000_000));

        assertArrayEquals(
                new LocalTime[] {
                    LocalTime.of(1, 0),
                    LocalTime.of(1, 0, 0, 500_000_000)
                },
                portal.getRankedAdjustedElapsedTimesInStage(trialStage));
    }

    @Test
    void calculatesFinishSprintAndMountainPoints() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int stageId = portal.addStageToRace(
                raceId, "Points", null, 100.0, LocalDateTime.now(), StageType.FLAT);
        portal.addIntermediateSprintToStage(stageId, 20.0);
        portal.addCategorizedClimbToStage(
                stageId, 40.0, CheckpointType.C4, 7.0, 3.0);
        portal.concludeStagePreparation(stageId);
        int teamId = portal.createTeam("Velocity", null);
        int riderOne = portal.createRider(teamId, "Rider One", 2000);
        int riderTwo = portal.createRider(teamId, "Rider Two", 2001);

        portal.registerRiderResultsInStage(
                stageId, riderOne,
                LocalTime.of(9, 0),
                LocalTime.of(9, 10),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0));
        portal.registerRiderResultsInStage(
                stageId, riderTwo,
                LocalTime.of(9, 0),
                LocalTime.of(9, 11),
                LocalTime.of(9, 29),
                LocalTime.of(9, 59));

        assertAll(
                () -> assertArrayEquals(new int[] {riderTwo, riderOne},
                        portal.getRidersRankInStage(stageId)),
                () -> assertArrayEquals(new int[] {67, 50},
                        portal.getRidersPointsInStage(stageId)),
                () -> assertArrayEquals(new int[] {1, 0},
                        portal.getRidersMountainPointsInStage(stageId)),
                () -> assertArrayEquals(new int[] {67, 50},
                        portal.getRidersPointsInRace(raceId)),
                () -> assertArrayEquals(new int[] {1, 0},
                        portal.getRidersMountainPointsInRace(raceId)),
                () -> assertArrayEquals(new int[] {riderTwo, riderOne},
                        portal.getRidersPointClassificationRank(raceId)),
                () -> assertArrayEquals(new int[] {riderTwo, riderOne},
                        portal.getRidersMountainPointClassificationRank(raceId)));
    }

    @Test
    void aggregatesGeneralClassificationAcrossMultipleStages() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int firstStage = portal.addStageToRace(
                raceId, "First", null, 20.0, LocalDateTime.now(), StageType.TT);
        int secondStage = portal.addStageToRace(
                raceId, "Second", null, 20.0, LocalDateTime.now(), StageType.TT);
        portal.concludeStagePreparation(firstStage);
        portal.concludeStagePreparation(secondStage);
        int teamId = portal.createTeam("Velocity", null);
        int riderOne = portal.createRider(teamId, "Rider One", 2000);
        int riderTwo = portal.createRider(teamId, "Rider Two", 2001);

        portal.registerRiderResultsInStage(
                firstStage, riderOne, LocalTime.of(9, 0), LocalTime.of(10, 0));
        portal.registerRiderResultsInStage(
                firstStage, riderTwo, LocalTime.of(9, 0), LocalTime.of(10, 10));
        portal.registerRiderResultsInStage(
                secondStage, riderOne, LocalTime.of(11, 0), LocalTime.of(12, 10));
        portal.registerRiderResultsInStage(
                secondStage, riderTwo, LocalTime.of(11, 0), LocalTime.of(12, 0));

        assertAll(
                () -> assertArrayEquals(new int[] {riderOne, riderTwo},
                        portal.getRidersGeneralClassificationRank(raceId)),
                () -> assertArrayEquals(
                        new LocalTime[] {LocalTime.of(2, 10), LocalTime.of(2, 10)},
                        portal.getGeneralClassificationTimesInRace(raceId)),
                () -> assertArrayEquals(new int[] {37, 37},
                        portal.getRidersPointsInRace(raceId)));
    }

    @Test
    void validatesIdsBeforeReturningResults() {
        assertAll(
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getRiderResultsInStage(999, 999)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getRidersGeneralClassificationRank(999)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.removeCheckpoint(999)));
    }

    @Test
    void savesLoadsAndContinuesWithUniqueIds(@TempDir Path temporaryDirectory)
            throws Exception {
        int raceId = portal.createRace("Tour", "Persisted race");
        int teamId = portal.createTeam("Velocity", "Persisted team");
        int riderId = portal.createRider(teamId, "Rider One", 2000);
        Path saveFile = temporaryDirectory.resolve("portal.ser");

        portal.saveCyclingPortal(saveFile.toString());
        portal.eraseCyclingPortal();
        portal.loadCyclingPortal(saveFile.toString());

        int nextRaceId = portal.createRace("Giro", "New race");
        assertAll(
                () -> assertArrayEquals(new int[] {raceId, nextRaceId}, portal.getRaceIds()),
                () -> assertTrue(nextRaceId > raceId),
                () -> assertArrayEquals(new int[] {riderId}, portal.getTeamRiders(teamId)));
    }

    @Test
    void failedLoadLeavesExistingStateUnchanged(@TempDir Path temporaryDirectory)
            throws Exception {
        int raceId = portal.createRace("Tour", null);
        Path invalidFile = temporaryDirectory.resolve("invalid.ser");
        Files.writeString(invalidFile, "not a serialised portal");

        assertThrows(IOException.class, () -> portal.loadCyclingPortal(invalidFile.toString()));
        assertArrayEquals(new int[] {raceId}, portal.getRaceIds());
    }

    @Test
    void deletingResultsRemovesThemWithoutRemovingRiderOrStage() throws Exception {
        StageFixture fixture = createRoadStageWithRider();
        portal.concludeStagePreparation(fixture.stageId);
        portal.registerRiderResultsInStage(
                fixture.stageId, fixture.riderId,
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        portal.deleteRiderResultsInStage(fixture.stageId, fixture.riderId);

        assertAll(
                () -> assertArrayEquals(new LocalTime[0],
                        portal.getRiderResultsInStage(fixture.stageId, fixture.riderId)),
                () -> assertArrayEquals(new int[] {fixture.riderId},
                        portal.getTeamRiders(fixture.teamId)),
                () -> assertEquals(20.0, portal.getStageLength(fixture.stageId)));
    }

    @Test
    void removingRiderCascadesRegisteredResults() throws Exception {
        StageFixture fixture = createRoadStageWithRider();
        portal.concludeStagePreparation(fixture.stageId);
        portal.registerRiderResultsInStage(
                fixture.stageId, fixture.riderId,
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        portal.removeRider(fixture.riderId);

        assertAll(
                () -> assertArrayEquals(new int[0], portal.getTeamRiders(fixture.teamId)),
                () -> assertArrayEquals(new int[0], portal.getRidersRankInStage(fixture.stageId)),
                () -> assertThrows(IDNotRecognisedException.class,
                        () -> portal.getRiderResultsInStage(
                                fixture.stageId, fixture.riderId)));
    }

    @Test
    void removingRaceByNameUsesStringValueEquality() throws Exception {
        portal.createRace(new String("Tour"), null);
        portal.removeRaceByName(new String("Tour"));
        assertArrayEquals(new int[0], portal.getRaceIds());
    }

    private StageFixture createRoadStageWithRider() throws Exception {
        int raceId = portal.createRace("Tour", null);
        int stageId = portal.addStageToRace(
                raceId, "Road", null, 20.0, LocalDateTime.now(), StageType.FLAT);
        int teamId = portal.createTeam("Velocity", null);
        int riderId = portal.createRider(teamId, "Rider One", 2000);
        return new StageFixture(raceId, stageId, teamId, riderId);
    }

    private record StageFixture(int raceId, int stageId, int teamId, int riderId) {
    }
}
