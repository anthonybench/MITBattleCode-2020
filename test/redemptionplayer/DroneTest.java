package redemptionplayer;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DroneTest {

    public void DroneTest () {
        Drone.randomDirection = null;
        Drone.potentialEnemyHQX = -1;
        Drone.potentialEnemyHQY = -1;
        Drone.enemyPotentialHQNumber = 1;
        Drone.targetEnemyX = -100;
        Drone.targetEnemyY = -100;
        Drone.prevSplitLocations = null;
        Drone.discoverDir = "right"; // prioritizes discovering to the right;
        Drone.prevLocations = null;
        Drone.headBackToPrevSplitLocation = false;
        Drone.prevSplitLocation = null;
        Drone.split = false;
        Drone.stuckCount = 3;
        Drone.hugDirection = 0; // 0 for left, 1 for right;
        Drone.prevLocation = null;
        Drone.hqLoc = new MapLocation(1,1);
        Drone.enemyHqLoc = new MapLocation(1,1);;
        Drone.haltProduction = false;
        Drone.haltTurn = 0;
        Drone.continueTurn = 0;
        Drone.broadcastedHalt = false;
        Drone.broadcastedCont = false;
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
    }


    @Test
    public void DroneTestOne() throws GameActionException {

//        RobotController rc = Mockito.mock(RobotController.class);
//        Drone drone = new Drone(rc);
////        RobotInfo enemyHQ = new RobotInfo(10001, Team.B, RobotType.HQ, 0, false, 0, 0,0, new MapLocation(3,3));
////        RobotInfo [] infoArr = new RobotInfo[1];
////        infoArr[0] = enemyHQ;
////        when(rc.isReady()).thenReturn(true);
////        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
////        when(rc.getTeamSoup()).thenReturn(200);
////        when(rc.getLocation()).thenReturn(new MapLocation(1,1));
////        when(rc.getRoundNum()).thenReturn(601);
////        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
////        when(rc.getTeam()).thenReturn(Team.A);
//        //        when(d.nearbyEnemyRobot(RobotType.HQ)).thenReturn(true);
////        when(rc.getLocation().thenReturn(new MapLocation(1,1)));
//        //
//
//        drone.run();
    }
}
