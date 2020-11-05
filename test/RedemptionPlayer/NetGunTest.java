package RedemptionPlayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class NetGunTest {


	@Test
	public void ShootEnemyTest() throws	GameActionException {
		//Arrange
		RobotController rc = Mockito.mock(RobotController.class);
		RobotInfo drone = new RobotInfo(10001, Team.NEUTRAL, RobotType.DELIVERY_DRONE, 0, false, 0, 0, 0, null);
		RobotInfo[] infoArr = new RobotInfo [1];
		infoArr[0] = drone;
		when(rc.isReady()).thenReturn(true);
		when(rc.canShootUnit(anyInt())).thenReturn(true);
		when(rc.senseNearbyRobots()).thenReturn(infoArr);
		NetGun netGun = new NetGun(rc);
		//Act
		netGun.run();
		//Assert
		verify(rc).shootUnit(anyInt());
	}

}
