package RedemptionPlayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class HQTest {
	//static RobotController rc;


	@Test
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
	}

}
