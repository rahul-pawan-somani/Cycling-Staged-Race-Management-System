import cycling.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * A short program to illustrate an app testing some minimal functionality of a
 * concrete implementation of the CyclingPortal interface -- note you
 * will want to increase these checks, and run it on your CyclingPortalImpl
 * class
 * (not the BadCyclingPortal class).
 *
 * 
 * @author Diogo Pacheco
 * @version 2.0
 */
public class CyclingPortalTestApp {

	/**
	 * Test method.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("The system compiled and started the execution...");

		// TODO replace BadMiniCyclingPortalImpl by CyclingPortalImpl
		CyclingPortalImpl portal1 = new CyclingPortalImpl();

		// Test getRaceIDs

		boolean isPortal1RaceIdsEmpty = portal1.getRaceIds().length == 0;

		assert isPortal1RaceIdsEmpty
				: "Initial Portal not empty as required or not returning an empty array.";

		if (isPortal1RaceIdsEmpty) {
			System.out.println("1. getRaceIDs empty : Test Success");
		} else {
			System.out.println("1. getRaceIDs empty : Test Failed");
		}

		try {
			int raceId = portal1.createRace("Fifa World Cup", "My favorite race");
			System.out.println("1. getRaceIDs : Test Success" + " " + raceId);
		} catch (IllegalNameException e) {
			e.printStackTrace();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}

		boolean isPortal1RaceIdsIsNotEmpty = portal1.getRaceIds().length == 1;

		assert isPortal1RaceIdsIsNotEmpty
				: "Initial Portal not empty as required or not returning an empty array.";

		if (isPortal1RaceIdsIsNotEmpty) {
			System.out.println("1. getRaceIDs not empty : Test Success");
		} else {
			System.out.println("1. getRaceIDs not empty : Test Failed");
		}

		// Test createRace
		try {
			int raceId = portal1.createRace("Tour de France", "Best Race");
			System.out.println("2. createRace valid input : Test Success");
			System.out.println("2. created race with ID: " + raceId);
		} catch (IllegalNameException | InvalidNameException e) {
			System.out.println("2. createRace valid input : Test Failed");
			System.out.println("Unexpected exception: " + e.getMessage());

		}

		try {
			portal1.createRace(null, "Description");
			System.out.println("2. createRace null : Test Failed");
		} catch (IllegalNameException | InvalidNameException e) {
			System.out.println("2. createRace null : Test Success");
		}

		try {
			portal1.createRace("", "Description");
			System.out.println("2. createRace empty : Test Failed");
		} catch (IllegalNameException | InvalidNameException e) {
			System.out.println("2. createRace empty : Test Success");
		}

		try {
			portal1.createRace("FernandoTorresCristianoRonaldoLionnelMessiKylianMbappe", "Description");
			System.out.println("2. createRace long name>30 : Test Failed");
		} catch (IllegalNameException | InvalidNameException e) {
			System.out.println("2. createRace longname > 30 : Test Success");
		}

		try {
			portal1.createRace("Tour de France", "My Favourite Race");
			System.out.println("2. createRace duplicate : Test Failed");
		} catch (IllegalNameException e) {
			System.out.println("2. createRace duplicate : Test Success");
		} catch (InvalidNameException e) {
			System.out.println("2. createRace duplicate: Test Failed");
			System.out.println("Unexpected exception: " + e.getMessage());
		}

		// Test viewRaceDetails
		try {
			String details = portal1.viewRaceDetails(1);
			assert "Best Race".equals(details) : "4. viewRaceDetails : Test Failed";
			System.out.println("3. viewRaceDetails : Test Success");
			System.out.println("3. Details: " + details);
		} catch (IDNotRecognisedException e) {
			assert false : "3. viewRaceDetails : Test Failed. " +
					"Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.viewRaceDetails(100);
			assert false : "3. viewRaceDetails invalid ID : Test Failed";
		} catch (IDNotRecognisedException e) {
			System.out.println("3. viewRaceDetails invalid ID : Test Success");
		}

		// Test removeRaceById
		try {
			portal1.removeRaceById(2);
			System.out.println("4. removeRaceById : Test Success");
			assert portal1.getRaceIds().length == 1 : "4. removeRaceById : Test Success";
		} catch (IDNotRecognisedException e) {
			assert false : "4. removeRaceById : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.removeRaceById(100);
			assert false
					: "4. removeRaceById invalid ID : Test Failed. Expected IDNotRecognisedException was not thrown";
		} catch (IDNotRecognisedException e) {
			System.out.println("4. removeRaceById invalid ID : Test Success");
		}

		// Test addStageToRace
		try {
			LocalDateTime startTime = LocalDateTime.now();
			int stageId = portal1.addStageToRace(1, "Chill", "Mostly flat as fuck", 100.0, startTime, StageType.FLAT);
			assert stageId == 1 : "Expected positive stage ID";
			System.out.println("5. addStageToRace : Test Success");
		} catch (Exception e) {
			assert false : "5. addStageToRace : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.addStageToRace(100, "Oyeah", "Hilly As Fuck", 150.0, LocalDateTime.now(),
					StageType.MEDIUM_MOUNTAIN);
			assert false
					: "5. addStageToRace invalid ID : Test Failed. Expected IDNotRecognisedException was not thrown";
		} catch (IDNotRecognisedException e) {
			System.out.println("5. addStageToRace invalid ID : Test Success");
		} catch (Exception e) {
			assert false : "5. addStageToRace invalid ID : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.addStageToRace(1, "Chill", "Mostly flat as fuck", 100.0, LocalDateTime.now(), StageType.FLAT);
			assert false : "5. addStageToRace duplicate : Test Failed. Expected IllegalNameException was not thrown";
		} catch (IllegalNameException e) {
			System.out.println("5. addStageToRace duplicate : Test Success");
		} catch (Exception e) {
			assert false : "5. addStageToRace duplicate : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.addStageToRace(1, null, "Description for Stage 1", 3.0, LocalDateTime.now(),
					StageType.HIGH_MOUNTAIN);
			assert false : "5. addStageToRace invalid name : Test Failed. Expected InvalidNameException was not thrown";
		} catch (InvalidNameException e) {
			System.out.println("5. addStageToRace invalid name : Test Success");
		} catch (Exception e) {
			assert false : "5. addStageToRace invalid name : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.addStageToRace(1, "Stage 1", "Description for Stage 1", 1.0, LocalDateTime.now(), StageType.TT);
			assert false
					: "5. addStageToRace invalid length : Test Failed. Expected InvalidLengthException was not thrown";
		} catch (InvalidLengthException e) {
			System.out.println("5. addStageToRace invalid length : Test Success");
		} catch (Exception e) {
			assert false : "5. addStageToRace invalid length : Test Failed. Unexpected exception: " + e.getMessage();
		}

		// Test getNumberOfStages
		try {
			int numberOfStages = portal1.getNumberOfStages(1);
			assert numberOfStages == 1 : "Expected number of stages: 1, Actual: " + numberOfStages;
			System.out.println("6. getNumberOfStages : Test Success");
		} catch (IDNotRecognisedException e) {
			assert false : "6. getNumberOfStages : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.getNumberOfStages(100);
			assert false
					: "6. getNumberOfStages invalid ID : Test Failed. Expected IDNotRecognisedException was not thrown";
		} catch (IDNotRecognisedException e) {
			System.out.println("6. getNumberOfStages invalid ID : Test Success");
		} catch (Exception e) {
			assert false : "6. getNumberOfStages invalid ID : Test Failed. Unexpected exception: " + e.getMessage();
		}

		// Test getRaceStages
		try {
			int[] stageIDs = portal1.getRaceStages(1);
			System.out.println("7. getRaceStages : Test Success");
			System.out.println("7. Stages for Race " + 1 + ": " + Arrays.toString(stageIDs));
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		// Test getStageLength
		try {
			double stageLength = portal1.getStageLength(1);
			System.out.println("8. getStageLength : Test Success");
			System.out.println("8. Length of Stage " + 1 + ": " + stageLength);
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		// Test removeStageById
		try {
			portal1.removeStageById(1);
			assert portal1.getRaceStages(1).length == 0 : "Expected 0 race after removal";
			System.out.println("9. removeStageById : Test Success");
		} catch (IDNotRecognisedException e) {
			assert false : "9. removeStageById : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.removeRaceById(100);
			assert false
					: "9. removeStageById invalid ID : Test Failed. Expected IDNotRecognisedException was not thrown";
		} catch (IDNotRecognisedException e) {
			System.out.println("9. removeStageById invalid ID : Test Success");
		}

		// Test addCategorizedClimbToRace
		try {
			LocalDateTime startTime = LocalDateTime.now();
			portal1.addStageToRace(1, "Hard AF", "Mostly flat as fuck", 100.0, startTime, StageType.MEDIUM_MOUNTAIN);
		} catch (Exception e) {
			assert false : "10. Unexpected exception: " + e.getMessage();
		}

		try {
			int checkpointID = portal1.addCategorizedClimbToStage(2, 10.0, CheckpointType.C3, 5.0, 10.0);
			System.out.println("10. addCategorizedClimbToStage : Test Success Checkpoint ID: " + checkpointID);
		} catch (Exception e) {
			System.err.println("10. Exception occurred: " + e.getMessage());
		}

		// Test addIntermediatesSprintToStage
		try {
			int checkpointID = portal1.addIntermediateSprintToStage(2, 50.0);
			System.out.println("11. addIntermediatesSPrintToStage : Test Success Checkpoint ID: " + checkpointID);
		} catch (Exception e) {
			System.err.println("11. Exception occurred: " + e.getMessage());
		}

		// Test getStageCheckpoint
		try {
			int[] stage4Checkpoints = portal1.getStageCheckpoints(2);
			System.out.println("12. getStageCheckpoint : Test Success");
			System.out.println("12. checkpoint for stage :" + Arrays.toString(stage4Checkpoints));
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		// Test removeCheckpoint
		try {
			portal1.removeCheckpoint(1);
		} catch (IDNotRecognisedException | InvalidStageStateException e) {
			System.out.println("13. Exception thrown: " + e.getMessage());
		}

        try {
            if (1 == portal1.getStageCheckpoints(2).length) {
                System.out.println("13. removeCheckpoint : Test Success");
            } else {
                System.out.println("13. Checkpoint with ID 1 was not removed.");
            }
        } catch (IDNotRecognisedException e) {
            throw new RuntimeException(e);
        }

        // Test concludeStagePreparation
		try {
			portal1.concludeStagePreparation(2);
			System.out.println("14. concludeStagePreparation : Test Success");
		} catch (Exception e) {
			System.out.println("14. concludeStagePreparation : Test Failed " + e.getMessage());
		}

		try {
			portal1.concludeStagePreparation(100);
			System.out.println("14. concludeStagePreparation invalid id : Test Failed");
		} catch (Exception e) {
			System.out.println("14. concludeStagePreparation invalid id : Test Success :" + e.getMessage());
		}

		// Test getTeams
		boolean isPortal1TeamsEmpty = portal1.getTeams().length == 0;

		assert isPortal1TeamsEmpty
				: "15. Initial Portal not empty as required or not returning an empty array.";

		if (isPortal1TeamsEmpty) {
			System.out.println("15. getTeams : Test Success");
		} else {
			System.out.println("15. getTeams : Test Failed");
		}

		// Test createTeam
		try {
			portal1.createTeam("TeamOne", "My favorite");
		} catch (IllegalNameException e) {
			e.printStackTrace();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}

		boolean Portal1ShouldHave1Team = portal1.getTeams().length == 1;

		assert (portal1.getTeams().length == 1)
				: "16. Portal1 should have one team.";

		if (Portal1ShouldHave1Team) {
			System.out.println("16. createTeam : Test Success");
		} else {
			System.out.println("16. createTeam : Test Failed");
		}

		// Test removeTeam
		try {
			portal1.removeTeam(1);
			assert portal1.getTeams().length == 0 : "17. Expected 0 race after removal";
			System.out.println("17. removeTeam : Test Success");
		} catch (IDNotRecognisedException e) {
			assert false : "17. removeTeam : Test Failed. Unexpected exception: " + e.getMessage();
		}

		try {
			portal1.removeTeam(100);
			assert false : "17. removeTeam invalid ID : Test Failed. Expected IDNotRecognisedException was not thrown";
		} catch (IDNotRecognisedException e) {
			System.out.println("17. removeTeam invalid ID : Test Success");
		}

		// Test createRider
		try {
			portal1.createTeam("TeamTwo", "My favorite");
		} catch (IllegalNameException e) {
			e.printStackTrace();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}

		try {
			int riderId = portal1.createRider(2, "Torres", 2003);
			System.out.println("18. createRider valid input : Test Success");
			System.out.println("18. created rider with ID: " + riderId);
		} catch (IDNotRecognisedException | IllegalArgumentException e) {
			System.out.println("18. createRider valid input : Test Failed");
			System.out.println("18. Unexpected exception: " + e.getMessage());
		}

		// Test getTeamRider
		try {
			int[] riderIDs = portal1.getTeamRiders(2);
			System.out.println("19. getTeamRider : Test Success");
			System.out.println("19. team rider :" + Arrays.toString(riderIDs));
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		try {
			int[] riderIDs = portal1.getTeamRiders(100);
			System.out.println("19. getTeamRider invalid ID : Test Failed");
		} catch (IDNotRecognisedException e) {
			System.out.println("19. getTeamRider invalid ID : Test Success :" + e.getMessage());
		}

		// Test removeRider
		try {
			portal1.removeRider(1);
			assert portal1.getTeamRiders(2).length == 0 : "Expected 0 race after removal";
			System.out.println("20. removeRider : Test Success");
		} catch (IDNotRecognisedException e) {
			assert false : "20. removeTeam : Test Failed. Unexpected exception: " + e.getMessage();
		}

		// Test registerRiderResultsInStage
		try {
			portal1.createRider(2, "David Villa", 2001);
		} catch (IDNotRecognisedException | IllegalArgumentException e) {
			System.out.println("21. createRider valid input : Test Failed");
			System.out.println("21. Unexpected exception: " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 2, LocalTime.of(9, 0), LocalTime.of(10, 0),
					LocalTime.of(11, 00, 30, 50));
			System.out.println("21. registerRiderResultsInStage : Test Success");
		} catch (Exception e) {
			System.out.println("21. registerRiderResultsInStage : Test Failed : " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(100, 2, LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0));
			System.out.println("21. registerRiderResultsInStage invalid stage ID : Test Failed");
		} catch (Exception e) {
			System.out.println("21. registerRiderResultsInStage invalid stage ID : Test Success: " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 50, LocalTime.of(9, 0));
			System.out.println("21. registerRiderResultsInStage invalid rider ID : Test Failed ");
		} catch (Exception e) {
			System.out.println("21. registerRiderResultsInStage invalid rider ID : Test Success: " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 2, LocalTime.of(9, 0), LocalTime.of(10, 0));
			System.out.println("21. registerRiderResultsInStage invalid checkpoint times : Test Failed");
		} catch (Exception e) {
			System.out.println(
					"21. registerRiderResultsInStage invalid checkpoint times : Test Success: " + e.getMessage());
		}

		// Test getRiderResultsInStage
        LocalTime[] times1 = new LocalTime[0];
        try {
            times1 = portal1.getRiderResultsInStage(2, 2);
        } catch (IDNotRecognisedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("22. getRiderResultsInStage : Test Success");
		System.out.println("22. Times and Elapsed time for Rider : " + Arrays.toString(times1));

        LocalTime[] times2 = new LocalTime[0];
        try {
            times2 = portal1.getRiderResultsInStage(2, 3);
        } catch (IDNotRecognisedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("22. getRiderResultsInStage no result : Test Success");
		System.out.println("22. Times and Elapsed time for Rider : " + Arrays.toString(times2));

		// Test getRiderAdjustedElapsedTimeInStage
		try {
			portal1.createRider(2, "David Moreno", 2000);
		} catch (IDNotRecognisedException | IllegalArgumentException e) {
			System.out.println("23. Unexpected exception: " + e.getMessage());
		}

		try {
			portal1.createRider(2, "David Santos", 2005);
		} catch (IDNotRecognisedException | IllegalArgumentException e) {
			System.out.println("23. Unexpected exception: " + e.getMessage());
		}

		try {
			portal1.createRider(2, "David Loucas", 1996);
		} catch (IDNotRecognisedException | IllegalArgumentException e) {
			System.out.println("23. Unexpected exception: " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 3, LocalTime.of(9, 0), LocalTime.of(10, 0),
					LocalTime.of(10, 59, 59, 59));
		} catch (Exception e) {
			System.out.println("23. registerRiderResultsInStage : Test Failed : " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 4, LocalTime.of(9, 0), LocalTime.of(10, 0),
					LocalTime.of(10, 59, 30, 39));
		} catch (Exception e) {
			System.out.println("23. registerRiderResultsInStage : Test Failed : " + e.getMessage());
		}

		try {
			portal1.registerRiderResultsInStage(2, 5, LocalTime.of(9, 0), LocalTime.of(10, 0),
					LocalTime.of(10, 59, 01, 89));
		} catch (Exception e) {
			System.out.println("23. registerRiderResultsInStage : Test Failed : " + e.getMessage());
		}

		try {
			LocalTime adjustedTime = portal1.getRiderAdjustedElapsedTimeInStage(2, 3);
			System.out.println(
					"23. getRiderAdjustedElapsedTimeInStage : Test Success : Adjusted Elapsed Time : " + adjustedTime);
		} catch (IDNotRecognisedException e) {
			System.out.println("23. IDNotRecognisedException occurred: " + e.getMessage());
		}

		// Test deleteRiderResultsInStage
		try {
			portal1.deleteRiderResultsInStage(2, 2);
		} catch (IDNotRecognisedException e) {
			System.out.println("24. IDNotRecognisedException occurred: " + e.getMessage());
		}
		try {
			LocalTime[] checkpointTimes = portal1.getRiderResultsInStage(2, 2);
			System.out.println("24. deleteRiderResultsInStage : Test Success");
			System.out.println("24. checkpoint times for Stage " + 2 + " and Rider " + 2 + ":");
			System.out.println("24. checkpoint time = " + Arrays.toString(checkpointTimes));
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		// Test getRidersRankInStage
		try {
			int[] ranks = portal1.getRidersRankInStage(2);
			System.out.println("25. getRidersRankInStage : Test Success");
			if (ranks.length == 0) {
				System.out.println("No riders in the specified stage.");
			} else {
				for (int i = 0; i < ranks.length; i++) {
					System.out.println("25. Rank " + (i + 1) + ": Rider ID " + ranks[i]);
				}
			}
		} catch (IDNotRecognisedException e) {
			System.out.println(e.getMessage());
		}

		try {
			int[] ranks = portal1.getRidersRankInStage(100);
			System.out.println("25. getRidersRankInStage invalid ID : Test Failed");
			for (int i = 0; i < ranks.length; i++) {
				System.out.println("Rank " + (i + 1) + ": Rider ID " + ranks[i]);
			}
		} catch (IDNotRecognisedException e) {
			System.out.println("25. getRidersRankInStage invalid ID : Test Success " + e.getMessage());
		}

		// Test getRankedAdjustedElapsedTimesInStage
		try {
			LocalTime[] result = portal1.getRankedAdjustedElapsedTimesInStage(2);
			System.out.println("26. getRankedAdjustedElapsedTimesInStage : Test Success");
			System.out.println("26. Ranked Adjusted Elapsed Times in Stage: ");
			System.out.println("26. " + Arrays.toString(result));
		} catch (IDNotRecognisedException e) {
			System.err.println("Error: Stage ID not recognized.");
			e.printStackTrace();
		}

		// Test getRidersPointInStage
		try {
			int[] points = portal1.getRidersPointsInStage(2);
			System.out.println("27. Points for riders: " + Arrays.toString(points));
		} catch (IDNotRecognisedException e) {
			System.err.println("27. IDNotRecognisedException occurred: " + e.getMessage());
		}

		// Test getRidersGeneralClassificationRank
        int[] generalClassificationRank = new int[0];
        try {
            generalClassificationRank = portal1.getRidersGeneralClassificationRank(1);
        } catch (IDNotRecognisedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("29. getRidersGeneralClassifictionRank : Test Success");
		for (int i = 0; i < generalClassificationRank.length; i++) {
			System.out.println("29. Rank " + (i + 1) + ": Rider ID " + generalClassificationRank[i]);
		}

		// Test getGeneralClassificationTimesInRace
		try {
			LocalTime[] generalClassificationTimes = portal1.getGeneralClassificationTimesInRace(1);

			System.out.println("30. getGeneralClassificationTimesInRace : Test Success");
			for (LocalTime time : generalClassificationTimes) {
				System.out.println("30. " + time);
			}
		} catch (IDNotRecognisedException e) {
			e.printStackTrace();
		}

		// Test getRidersPointsInRace
		try {
			// Call the method and get the result
			int[] ridersPoints = portal1.getRidersPointsInRace(1);
			// Print the results
			System.out.println("31. getRidersPointsInRace : Test Success");
			for (int i = 0; i < ridersPoints.length; i++) {
				System.out.println("31. Rider " + (i + 1) + ": " + ridersPoints[i]);
			}
		} catch (IDNotRecognisedException e) {
			// Handle the exception if race ID is not recognized
			System.out.println("31. Race ID not recognized: " + e.getMessage());
		}

		// Test saveCyclingPortal
		String filename = "cycling_portal_data.ser";

		// Saving the cycling portal data
		try {
			portal1.saveCyclingPortal(filename);
			System.out.println("35. saveCyclingPortal : Test Success");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Test eraseCyclingPortal
		// Call eraseCyclingPortal method
		portal1.eraseCyclingPortal();

		// Verify cleared state
		boolean Portal1RaceEmpty = portal1.getRaceIds().length == 0;
		boolean Portal1TeamsEmpty = portal1.getTeams().length == 0;

		if (Portal1RaceEmpty && Portal1TeamsEmpty) {
			System.out.println("36. eraseCyclingPortal : Test Success");
		} else {
			System.out.println("36. eraseCyclingPortal : Test Failed");
		}

		// Test loadCyclingPortal
		String Filename = "cycling_portal_data.ser";

		try {
			portal1.loadCyclingPortal(Filename);
			System.out.println("37. loadCyclingPortal : Test Success");
		} catch (Exception e) {
			// Handle any exceptions
			e.printStackTrace();
			System.out.println("Exception occurred: " + e.getMessage());
		}

		// Test removeRaceByName
		try {
			portal1.removeRaceByName("Fifa World Cup");
		} catch (NameNotRecognisedException e) {
			System.out.println("38. Error: " + e.getMessage());
		}

		boolean Portal1RaceIds = portal1.getRaceIds().length == 0;

		if (Portal1RaceIds) {
			System.out.println("38. removeRaceByName : Test Success");
		} else {
			System.out.println("38. removeRaceByName : Test Failed");
		}

	}

}
