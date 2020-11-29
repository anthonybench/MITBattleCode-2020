package redemptionplayer;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;
import scala.collection.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FulfillmentCenterTest {

    @Test
    public void BuildDroneTestOne() throws GameActionException {
        //Arrange
        RobotController rc = Mockito.mock(RobotController.class);

        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
        FulfillmentCenter.hqLoc = new MapLocation(1, 1);

        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyHQ;
        when(rc.isReady()).thenReturn(true);
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        when(rc.getTeamSoup()).thenReturn(180);
//        FulfillmentCenter.droneCount = 5;
//        FulfillmentCenter.madeDrone = false;
        when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));

        FulfillmentCenter fc = new FulfillmentCenter(rc);

        //Act
        fc.run();
    }

    @Test
    public void BuildDroneTestTwo() throws GameActionException {
        //Arrange
        RobotController rc = Mockito.mock(RobotController.class);

        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
        FulfillmentCenter.hqLoc = new MapLocation(1, 1);

        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyHQ;
        when(rc.isReady()).thenReturn(true);
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        when(rc.getTeamSoup()).thenReturn(200);
        FulfillmentCenter.droneCount = 4;
        FulfillmentCenter.madeDrone = false;
        when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));

        FulfillmentCenter fc = new FulfillmentCenter(rc);

        //Act
        fc.run();
    }

    @Test
    public void BuildDroneTestThree() throws GameActionException {
        //Arrange
        RobotController rc = Mockito.mock(RobotController.class);

        RobotInfo enemyHQ = new RobotInfo(10001, Team.NEUTRAL, RobotType.HQ, 0, false, 0, 0,0, null);
        FulfillmentCenter.hqLoc = new MapLocation(1, 1);


        MapLocation flop = new MapLocation(1, 1);
        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyHQ;
        when(rc.isReady()).thenReturn(true);
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        when(rc.getTeamSoup()).thenReturn(100);
        FulfillmentCenter.droneCount = 3;
        FulfillmentCenter.madeDrone = false;
//        when(rc.getLocation().isWithinDistanceSquared(any(MapLocation.class), 20)).thenReturn(false);
        when(rc.canBuildRobot(any(RobotType.class), any(Direction.class))).thenReturn(true);
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));

        FulfillmentCenter fc = new FulfillmentCenter(rc);

        //Act
        fc.run();
    }

}
