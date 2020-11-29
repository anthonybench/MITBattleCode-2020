package redemptionplayer;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DroneTest {

    @Test
    public void DroneTestOne() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyHQ;
        when(rc.isReady()).thenReturn(true);
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        when(rc.getTeamSoup()).thenReturn(200);
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));


       Drone d = new Drone(rc);
        d.run();
    }
}
