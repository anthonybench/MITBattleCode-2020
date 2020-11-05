package RedemptionPlayer;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DesignSchoolTest {


	@Test
	public void BuildLandscaperTest() throws	GameActionException {
		//Arrange
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
		RobotInfo [] infoArr = new RobotInfo[1];
		infoArr[0] = enemyHQ;
		when(rc.isReady()).thenReturn(true);
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		when(rc.getTeamSoup()).thenReturn(200);
		when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
		when(rc.getLocation()).thenReturn(new MapLocation(1,1));
		DesignSchool ds = new DesignSchool(rc);
		//Act
		ds.run();
		//Assert
		verify(rc).buildRobot(RobotType.LANDSCAPER, Direction.NORTH);
	}

}
