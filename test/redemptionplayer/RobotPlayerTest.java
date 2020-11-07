package redemptionplayer;

import battlecode.common.*;
import battlecode.common.RobotController;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

		import static org.junit.Assert.assertEquals;
		import static org.mockito.Mockito.when;
		import static org.junit.Assert.assertNotEquals;


public class RobotPlayerTest {
	//static RobotController rc;


	@Test
	public void RobotCreationTest() {
			RobotPlayer robotPlayer1 = Mockito.mock(RobotPlayer.class);
			RobotPlayer robotPlayer2 = Mockito.mock(RobotPlayer.class);
			Assert.assertNotSame("RobotPlayer1 is the same as RobotPlayer2",robotPlayer1, robotPlayer2);
	}

	@Test
	public void testGetType() {
		RobotController rc = Mockito.mock(RobotController.class);
		when(rc.getType()).thenReturn(RobotType.MINER);
		assertEquals(rc.getType(), RobotType.MINER);
	}

}
