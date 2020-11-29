package redemptionplayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SleepyLandscaperTest {
//    @Test
//    public void scapeTheLandsTest() throws GameActionException {
//        RobotController rc = Mockito.mock(RobotController.class);
//
//        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
//        Landscaper.hqLoc = new MapLocation(1, 1);
//
//        RobotInfo [] infoArr = new RobotInfo[1];
//        infoArr[0] = enemyHQ;
//        when(rc.isReady()).thenReturn(true);
//        when(rc.senseNearbyRobots()).thenReturn(infoArr);
//        when(rc.getTeamSoup()).thenReturn(200);
//        when(rc.getLocation()).thenReturn(new MapLocation(1,1));
//        //
//
//        Robot.turnCount = 1;
//        when(rc.getDirtCarrying()).thenReturn(8);
//        when(rc.getLocation().distanceSquaredTo(any(MapLocation.class))).thenReturn(3);
//        when(rc.senseElevation(any(MapLocation.class))).thenReturn(1);
//        when(rc.getRoundNum()).thenReturn(10);
//        Landscaper.rushType = true;
//        Landscaper.outerCircle = new ArrayList<MapLocation>();
//        Landscaper.firstLandscaper = false;
//        Landscaper.tryMoveToEnemyHQTurns = 5;
//        Landscaper ls = new Landscaper(rc);
//        when(ls.nearbyEnemyRobot(RobotType.HQ)).thenReturn(true);
//
//
//        //
//
//
//        //
//        ls.run();
//    }

    @Test
    public void scapeTheLandsTest() throws GameActionException {

        RobotController rc = Mockito.mock(RobotController.class);
        Landscaper.hqLoc = new MapLocation(1,1);
        Landscaper.rushType = false;
        Landscaper.firstLandscaper = false;
        Landscaper.enemyHqLoc = new MapLocation(4,4);
        Landscaper.turnCount = 1;
        //
        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyHQ;
        when(rc.isReady()).thenReturn(true);
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        when(rc.getTeamSoup()).thenReturn(200);
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));

        Landscaper ls = new Landscaper(rc);
        ls.run();
    }
}
