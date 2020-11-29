package redemptionplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import org.junit.Test;
import org.mockito.Mockito;

public class VaporatorTest {

    @Test
    public void testVaporator () throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Vaporator vape = new Vaporator(rc);
        vape.run();
    }
}
