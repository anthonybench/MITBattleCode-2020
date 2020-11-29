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

    public MinerTest () {
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
    }

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
        Miner.refineLocations = new ArrayList<>();
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
        Miner.refineLocations = new ArrayList<>();
        Miner.backupMiner = false;
        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        miner.getSoupLocation();
    }

    @Test
    public void testRunNonFirstMiner() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);

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
        Miner miner = new Miner(rc);
        when(rc.getRoundNum()).thenReturn(100);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        miner.run();

        when(rc.getRoundNum()).thenReturn(100);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        Miner.rushDesignSchoolLocation = new MapLocation(3, 3);
        when(rc.getTeam()).thenReturn(Team.A);
        RobotInfo enemyDrone = new RobotInfo(10001, Team.B, RobotType.DELIVERY_DRONE, 0, false, 0, 0,0, new MapLocation(5, 5));
        RobotInfo [] infoArr = new RobotInfo[1];
        infoArr[0] = enemyDrone;
        when(rc.senseNearbyRobots()).thenReturn(infoArr);
        miner.run();

        when(rc.getRoundNum()).thenReturn(200);
        Miner.startAttacking = false;
        miner.run();

        when(rc.getRoundNum()).thenReturn(100);
        Miner.enemyHqLoc = null;
        Miner.giveUpMinerRush = false;
        miner.run();
    }

//    @Test void testRunBackUpMiner() throws GameActionException {
//
//    }

    @Test
    public void testRunNormalMiner() throws GameActionException {
        Miner.firstMiner = false;
        Miner.backupMiner = false;
        RobotController rc = Mockito.mock(RobotController.class);
        Miner miner = new Miner(rc);

        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.getRoundNum()).thenReturn(40);
        when(rc.senseNearbySoup()).thenReturn(new MapLocation[0]);
        when(rc.getLocation()).thenReturn(new MapLocation(10, 10));

        miner.run();
    }

    @Test
    public void testTryMine() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Miner miner = new Miner(rc);
        Direction dir = Util.directions[0];
        when(rc.isReady()).thenReturn(true);
        when(rc.canMineSoup(dir)).thenReturn(true);
        miner.tryMine(dir);

        when(rc.isReady()).thenReturn(false);
        miner.tryMine(dir);
    }

    @Test
    public void testBuildRefineryNearSoupArea() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);
        Miner miner = new Miner(rc);
        Direction dir = Util.directions[0];
        when(rc.isReady()).thenReturn(true);
        when(rc.canMineSoup(dir)).thenReturn(true);
        miner.tryMine(dir);
        Miner.refineLocations = new ArrayList<>();
        Miner.refineLocations.add(new MapLocation(2, 2));
        when(rc.getLocation()).thenReturn(new MapLocation(10, 10));
        when(rc.getTeamSoup()).thenReturn(210);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        when(rc.isReady()).thenReturn(false);
        miner.buildRefineryNearSoupArea();

        when(rc.getLocation()).thenReturn(new MapLocation(30, 30));
        when(miner.tryBuild(RobotType.REFINERY, Util.directions[3])).thenReturn(true);
        when(miner.tryBuild(RobotType.REFINERY, Util.directions[0])).thenReturn(true);
        Miner.broadcastedCont = false;
        Miner.broadcastedHalt = true;
        miner.buildRefineryNearSoupArea();

        when(rc.getTeamSoup()).thenReturn(4);
        Miner.broadcastedHalt = false;
        miner.buildRefineryNearSoupArea();
    }

    @Test
    public void testRunMiner() throws GameActionException {
        RobotController rc = Mockito.mock(RobotController.class);

        Miner miner = new Miner(rc);
        Miner.backupMiner = false;
        Miner.firstMiner = false;
        Miner.giveUpMinerRush = false;
        when(rc.getRoundNum()).thenReturn(100);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        when(rc.senseNearbySoup()).thenReturn(new MapLocation[0]);
        miner.run();

        Miner.backupMiner = false;
        Miner.firstMiner = false;
        Miner.giveUpMinerRush = false;
        Miner.turnCount = 100;
        when(rc.getRoundNum()).thenReturn(70);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        MapLocation[] mapLocs = new MapLocation[1];
        mapLocs[0] = new MapLocation(1, 1);
        when(rc.senseNearbySoup()).thenReturn(mapLocs);
        miner.run();

        Miner.backupMiner = false;
        Miner.firstMiner = false;
        Miner.giveUpMinerRush = false;
        Miner.turnCount = 100;
        Miner.soupLocation = null;
        when(rc.getRoundNum()).thenReturn(70);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.getBlock(any(int.class))).thenReturn(new Transaction[0]);
        when(rc.senseNearbyRobots()).thenReturn(new RobotInfo[0]);
        when(rc.senseNearbySoup()).thenReturn(new MapLocation[0]);
        miner.run();
    }
}
