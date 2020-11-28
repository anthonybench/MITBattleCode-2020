package redemptionplayer;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MinerTest {
    @Test
    public void tryBuildTest() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        when(rc.isReady()).thenReturn(false);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner miner = new Miner(rc);
        Assert.assertFalse(miner.tryBuild(RobotType.DELIVERY_DRONE, Direction.EAST));
    }

    @Test(expected = NullPointerException.class)
    public void testMoveAroundHQ() throws GameActionException{
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner miner = new Miner(rc);
        miner.moveAroundHQ();
    }

    @Test
    public void testEnemyBaseFindingLogic() throws GameActionException{
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner.enemyPotentialHQNumber = 1;
        Miner.targetEnemyX = 1;
        Miner.targetEnemyY = 2;
        Miner miner = new Miner(rc);
        miner.enemyBaseFindingLogic();
    }

    @Test(expected = NullPointerException.class)
    public void testRunNull() throws GameActionException{
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner miner = new Miner(rc);
        miner.run();
    }

    @Test
    public void testBuildPriority() throws GameActionException{
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner.rushing = false;
        Miner.refineLocations = new TreeSet<>();
        Miner.backupMiner = false;
        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        miner.setBuildPriority();
        Assert.assertEquals(Miner.buildPriority, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testGetSoupLocation() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.hqLoc = new MapLocation(1, 1);
        Miner.rushing = false;
        Miner.refineLocations = new TreeSet<>();
        Miner.backupMiner = false;
        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        miner.getSoupLocation();
    }

    @Test
    public void testRunNonFirstMiner() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.firstMiner = false;
        Miner.stuckMoves = 0;
        Miner.designSchoolCount = 0; //only first miner cares about this for now
        Miner.currentElevation = 0;
        Miner.startAttacking = false;
        Miner.pauseForFlight = false;
        Miner.giveUpMinerRush = false;
        Miner.refineLocations = null;
        Miner.soupLocations = null;
        Miner.soupLocation = null;
        Miner.seenSoupLocs = null;
        Miner.backupMiner = false;
        Miner.rushDesignSchoolLocation = new MapLocation(1, 1);
        Miner.buildPriority = 0;
        Miner.rushing = false;
        Miner.broadCastedGiveUpMinerRush = false;
        Miner.fulfillmentCenterCount = 0;
        Miner.checkGiveUpRush = false;
        Miner.moveAroundHQDir = "left";
        Miner.randomDirection = null;
        Miner.recentPosition = null;
        Miner.potentialEnemyHQX = -1;
        Miner.potentialEnemyHQY = -1;
        Miner.enemyPotentialHQNumber = 1;
        Miner.targetEnemyX = -100;
        Miner.targetEnemyY = -100;
        Miner.prevSplitLocations = null;
        Miner.discoverDir = "right"; // prioritizes discovering to the right;
        Miner.prevLocations = null;
        Miner.headBackToPrevSplitLocation = false;
        Miner.prevSplitLocation = null;
        Miner.split = false;
        Miner.stuckCount = 3;
        Miner.hugDirection = 0; // 0 for left, 1 for right;
        Miner.prevLocation = null;
        Miner.hqLoc = new MapLocation(1,1);
        Miner.enemyHqLoc = new MapLocation(1,1);;
        Miner.haltProduction = false;
        Miner.haltTurn = 0;
        Miner.continueTurn = 0;
        Miner.broadcastedHalt = false;
        Miner.broadcastedCont = false;

        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        miner.run();
    }

    @Test
    public void testRunFirstMiner() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Miner.firstMiner = true;
        Miner.stuckMoves = 0;
        Miner.designSchoolCount = 0; //only first miner cares about this for now
        Miner.currentElevation = 0;
        Miner.startAttacking = false;
        Miner.pauseForFlight = false;
        Miner.giveUpMinerRush = false;
        Miner.refineLocations = null;
        Miner.soupLocations = null;
        Miner.soupLocation = null;
        Miner.seenSoupLocs = null;
        Miner.backupMiner = false;
        Miner.rushDesignSchoolLocation = null;
        Miner.buildPriority = 0;
        Miner.rushing = false;
        Miner.broadCastedGiveUpMinerRush = false;
        Miner.fulfillmentCenterCount = 0;
        Miner.checkGiveUpRush = false;
        Miner.moveAroundHQDir = "left";
        Miner.randomDirection = null;
        Miner.recentPosition = null;
        Miner.potentialEnemyHQX = -1;
        Miner.potentialEnemyHQY = -1;
        Miner.enemyPotentialHQNumber = 1;
        Miner.targetEnemyX = -100;
        Miner.targetEnemyY = -100;
        Miner.prevSplitLocations = null;
        Miner.discoverDir = "right"; // prioritizes discovering to the right;
        Miner.prevLocations = null;
        Miner.headBackToPrevSplitLocation = false;
        Miner.prevSplitLocation = null;
        Miner.split = false;
        Miner.stuckCount = 3;
        Miner.hugDirection = 0; // 0 for left, 1 for right;
        Miner.prevLocation = null;
        Miner.hqLoc = new MapLocation(1,1);
        Miner.enemyHqLoc = new MapLocation(1,1);;
        Miner.haltProduction = false;
        Miner.haltTurn = 0;
        Miner.continueTurn = 0;
        Miner.broadcastedHalt = false;
        Miner.broadcastedCont = false;

        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        miner.run();
    }
}
