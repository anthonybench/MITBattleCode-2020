package redemptionplayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class UnitTest {
	//static RobotController rc;


	@Test
	public void findEnemyHqTest () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		Unit.enemyHqLoc = null;
		Unit.hqLoc = new MapLocation(2,2);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
		Unit unit = new Unit(rc);
		unit.findEnemyHQ();
	}

	@Test
	public void clearMovementTest () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		Unit.hqLoc = new MapLocation(2,2);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
		Unit unit = new Unit(rc);
		unit.clearMovement();
	}

	@Test
	public void broadcastRealEnemyHQCoordsTest () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		Unit.hqLoc = new MapLocation(2,2);
		Unit.targetEnemyX = 2;
		Unit.targetEnemyY = 2;
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
		when(rc.canSubmitTransaction(any(int[].class), any(int.class))).thenReturn(true);
		Unit unit = new Unit(rc);
		unit.broadcastRealEnemyHQCoordinates();
	}

	@Test
	public void tileGoingToFloodTest () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		Unit.hqLoc = new MapLocation(2,2);
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
		when(rc.adjacentLocation(any(Direction.class))).thenReturn(new MapLocation(2,2));
		when(rc.senseElevation(new MapLocation(2,2))).thenReturn(0);
		Unit unit = new Unit(rc);
		unit.broadcastRealEnemyHQCoordinates();
	}

}
