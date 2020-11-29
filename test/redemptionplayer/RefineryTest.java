package redemptionplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import org.junit.Test;
import org.mockito.Mockito;

public class RefineryTest {
    @Test

    public void testRefinery () throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Refinery refine = new Refinery(rc);
        refine.run();
    }
}
