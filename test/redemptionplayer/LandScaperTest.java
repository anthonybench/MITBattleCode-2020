package redemptionplayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class LandScaperTest {
	//static RobotController rc;


	@Test
	public void LandScaperRushTestGetsDirt () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(1,1);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(2,2);
		Landscaper.turnCount = 1;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(0);
		when(rc.getLocation()).thenReturn(new MapLocation(1,2));
		when(rc.canDigDirt(Direction.NORTH)).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.run();
	}

	@Test
	public void LandScaperRushTestDepositDirt () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(1,1);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(2,2);
		Landscaper.turnCount = 1;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(1);
		when(rc.getLocation()).thenReturn(new MapLocation(1,2));
		when(rc.canDepositDirt(any(Direction.class))).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.run();
	}

	@Test
	public void LandScaperRushTestWalkToHq () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(1,1);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(4,4);
		Landscaper.turnCount = 1;
		Landscaper.tryMoveToEnemyHQTurns = 5;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(1);
		when(rc.getLocation()).thenReturn(new MapLocation(1,2));
		when(rc.canDepositDirt(any(Direction.class))).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.run();
	}
	@Test
	public void LandScaperRushDontDepositOnWall () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(1,1);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(3,3);
		Landscaper.turnCount = 1;
		Landscaper.tryMoveToEnemyHQTurns = 6;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(1);
		when(rc.getLocation()).thenReturn(new MapLocation(1,2));
		when(rc.canDepositDirt(any(Direction.class))).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.run();
	}

	@Test
	public void TryDigTestFalseEnemyHqDigTop () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(1,1);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(3,3);
		Landscaper.turnCount = 1;
		Landscaper.tryMoveToEnemyHQTurns = 6;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(1);
		when(rc.getLocation()).thenReturn(new MapLocation(1,2));
		when(rc.canDigDirt(any(Direction.class))).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.tryDig(false);
	}
	@Test
	public void TryDigTestFalseEnemyHqDigLeftBottom () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		Landscaper.hqLoc = new MapLocation(10,10);
		Landscaper.rushType = true;
		Landscaper.firstLandscaper = false;
		Landscaper.enemyHqLoc = new MapLocation(3,3);
		Landscaper.turnCount = 1;
		Landscaper.tryMoveToEnemyHQTurns = 6;
		when(rc.getRoundNum()).thenReturn(10);
		when(rc.getDirtCarrying()).thenReturn(1);
		when(rc.getLocation()).thenReturn(new MapLocation(5,5));
		when(rc.canDigDirt(any(Direction.class))).thenReturn(true);
		Landscaper ls = new Landscaper(rc);
		ls.tryDig(false);
	}



}
