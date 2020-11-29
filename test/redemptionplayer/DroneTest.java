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
        when(rc.getRoundNum()).thenReturn(601);
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.getTeam()).thenReturn(Team.A);
//        when(rc.getLocation().thenReturn(new MapLocation(1,1)));
        //
        Drone.minerLoc = new MapLocation(1,1);
        Drone.firstMinerID = 1;
        Drone.pickUpID = -1;
        Drone.pickUpType = RobotType.LANDSCAPER;
        Drone.pickUpLocation = new MapLocation(5,5);
        Drone.sameTeam = true;
        Drone.nearestWater = new MapLocation (20,20);
        Drone.moveToEnemyBaseTurn = 500;
        Drone.attackTurn = 600;
        Drone.enemyHqLoc = null;
        Drone.hqLoc = new MapLocation(10,10);

        Drone d = new Drone(rc);
        when(d.nearbyEnemyRobot(RobotType.HQ)).thenReturn(true);

        d.run();
    }
}
