package RedemptionPlayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
<<<<<<< HEAD
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
=======
import RedemptionPlayer.RobotPlayer;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
>>>>>>> a2a7610991fc253411a3ca427d19a13b42cf0dee


public class HQTest {
	//static RobotController rc;

	@Test
	public void testGetType() {
		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.getType()).thenReturn(RobotType.MINER);
		assertEquals(rc.getType(), RobotType.MINER);
	}

	@Test
<<<<<<< HEAD
	public void BuildMinerTest() {
		HQ hq1 = Mockito.mock(HQ.class);
		hq1.rc = Mockito.mock(RobotController.class);
		hq1.mapHeight = 30;
		hq1.mapWidth=30;
		MapLocation loc = new MapLocation(15,15);
		when(hq1.rc.adjacentLocation(Direction.NORTH)).thenReturn(loc);
		when(hq1.rc.getType()).thenReturn(RobotType.HQ);
		when(hq1.rc.getTeamSoup()).thenReturn(100);
		when(hq1.rc.getRoundNum()).thenReturn(2);
		when(hq1.rc.isReady()).thenReturn(true);
		when(hq1.rc.canBuildRobot(RobotType.MINER, Direction.NORTH)).thenReturn(true);
		try {
			assertTrue(hq1.tryBuild(RobotType.MINER, Direction.NORTH));
		}
		catch (GameActionException ex){
			return;
		}
	}

	@Test
	public void sendHQloctest() {
		HQ hq = Mockito.mock(HQ.class);
		hq.rc = Mockito.mock(RobotController.class);
		int[] message = new int[7];
		when(hq.rc.canSubmitTransaction(message, 3)).thenReturn(true);
		try {
			hq.sendHqLoc(hq.rc.getLocation());
			assertNotNull(hq.rc.getBlock(3));
		}
		catch (GameActionException ex) {
			return;
		}
=======
	public void testSendHQLocSuccess() throws GameActionException {
		//Prepare test
		int expectedX = 1;
		int expectedY = 1;
		int[] expectedMessage = new int[7];
		expectedMessage[0] = 384392; //TeamSecret
		expectedMessage[1] = 0; //HQ_Loc
		expectedMessage[2] = expectedX;
		expectedMessage[3] = expectedY;

		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.canSubmitTransaction(any(int[].class), anyInt())).thenReturn(true);

		//Execute Test
		HQ hq = new HQ(rc);
		hq.sendHqLoc(new MapLocation(expectedX,expectedY));

		//Verify test
		verify(rc).submitTransaction(expectedMessage,3);
	}

	@Test
	public void testSendHQLocFail() throws GameActionException {
		//Prep
		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.canSubmitTransaction(any(int[].class), anyInt())).thenReturn(false);

		//Execute
		HQ hq = new HQ(rc);
		hq.sendHqLoc(new MapLocation(1,1));

		//Verify
		verify(rc, times(0)).submitTransaction(any(int[].class), anyInt());
	}

	@Test
	public void testHQRunBuildMiners() throws GameActionException {
		//Prep
		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.isReady()).thenReturn(true);
		when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
		when(rc.getLocation()).thenReturn(new MapLocation(1,1));
		when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);

		//Execute
		HQ hq = new HQ(rc);
		hq.numMiners = 4;
		hq.run();

		//verify
		assertEquals(hq.numMiners, 12);
	}

	@Test
	public void testHQRunNoBuildMiners() throws GameActionException {
		//Prep
		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.isReady()).thenReturn(true);
		when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
		when(rc.getLocation()).thenReturn(new MapLocation(1,1));
		when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);

		//Execute
		HQ hq = new HQ(rc);
		hq.numMiners = 5;
		hq.run();

		//verify
		assertEquals(hq.numMiners, 5);
>>>>>>> a2a7610991fc253411a3ca427d19a13b42cf0dee
	}

	@Test
	public void testHQRunSenseNearby() throws GameActionException {
		int robotId = 10001;
		//Prep test
		RobotController rc = mock(RobotController.class);
		when(rc.isReady()).thenReturn(true);
		when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
		when(rc.getLocation()).thenReturn(new MapLocation(1,1));
		when(rc.getTeam()).thenReturn(null);
		when(rc.canShootUnit(anyInt())).thenReturn(true);

		//Only the first three values in RobotInfo matter
		RobotInfo ri = new RobotInfo(robotId, Team.NEUTRAL, RobotType.DELIVERY_DRONE, 0, false, 0, 0,0, null);
		RobotInfo[] r = new RobotInfo[1];
		r[0] = ri;
		when(rc.senseNearbyRobots()).thenReturn(r);

		//Execute
		HQ hq = new HQ(rc);
		hq.run();

		//verify
		verify(rc).shootUnit(robotId);
	}

	@Test
	public void testHQRunSendHQ() throws GameActionException {
		int expectedX = 100;
		int expectedY = 101;
		int[] expectedMessage = new int[7];
		expectedMessage[0] = 384392; //TeamSecret
		expectedMessage[1] = 0; //HQ_Loc \
		expectedMessage[2] = expectedX;
		expectedMessage[3] = expectedY;

		//Prep test
		RobotController rc = mock(RobotController.class);
		when(rc.isReady()).thenReturn(true);
		when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
		when(rc.getLocation()).thenReturn(new MapLocation(expectedX,expectedY));
		when(rc.canSubmitTransaction(any(int[].class), anyInt())).thenReturn(true);
		when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);

		//Execute
		HQ hq = new HQ(rc);
		hq.turnCount = 0;
		hq.run();

		//verify
		verify(rc).submitTransaction(expectedMessage,3);
	}
}
