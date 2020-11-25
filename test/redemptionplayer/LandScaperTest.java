package redemptionplayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LandScaperTest {
	//static RobotController rc;


	@Test
	public void LandScaperTest () throws GameActionException {
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.B, RobotType.HQ, 0, false, 0, 0,0, new MapLocation(5,5));
		RobotInfo allyHQ = new RobotInfo(10002, Team.A, RobotType.HQ, 0, false, 0, 0,0, new MapLocation(2,2));
		RobotInfo [] infoArr = new RobotInfo[2];
		infoArr[0] = enemyHQ;
		infoArr[1] = allyHQ;
		when(rc.isReady()).thenReturn(true);
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getDirtCarrying()).thenReturn(0);

		//when(rc.getLocation()).thenReturn(new MapLocation(2,2));
		//when(rc.getLocation()).thenReturn(new MapLocation(1,1));
		//when(rc.getLocation().distanceSquaredTo(any(MapLocation.class))).thenReturn(2);
		//when(rc.canDepositDirt(any(Direction.class))).thenReturn(true);
        Landscaper.hqLoc = new MapLocation(3,3);
        Landscaper.enemyHqLoc = new MapLocation(5,5);
		Landscaper ls = new Landscaper(rc);



		ls.run();

		verify(ls).tryDig(true);
	}

	@Test
	public void TryDidTest () {
		RobotController rc = Mockito.mock(RobotController.class);


	}

}
