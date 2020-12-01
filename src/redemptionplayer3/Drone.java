package redemptionplayer3;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Drone extends Unit {
    static MapLocation minerLoc = null;
    static int firstMinerID = 0;
    static int pickUpID = -1;
    static RobotType pickUpType;
    static MapLocation pickUpLocation;
    static boolean sameTeam = true;
    static MapLocation nearestWater;
    static int moveToEnemyBaseTurn = 2000;
    static int attackTurn = 1200;
    static Set<MapLocation> netGuns;
    static boolean enemyUnit = false;
    static boolean justBorn = true;
    static int randomDirectionCount = 2;
    public Drone(RobotController rc) throws GameActionException {
        super(rc);
        netGuns = new TreeSet<>();
    }

    public void run() throws GameActionException {
        super.run();
        if (enemyHqLoc == null) {
            getRealEnemyHQFromBlockchain(5);
        }

        if (turnCount == 1 && rc.getRoundNum() > 1200) {
            moveToEnemyBaseTurn = 2000;
            attackTurn = moveToEnemyBaseTurn + 200;
        }
//        if (justBorn) {
//            justBorn = false;
//            if (rc.getRoundNum() > 100) {
//                getRealEnemyHQFromBlockchain(rc.getRoundNum() - 100);
//                if (enemyHqLoc != null) {
//                    netGuns.add(enemyHqLoc);
//                }
//            }
//        }
        //attack enemy HQ, kill their landscapers so we have higher wall.
        if (rc.getRoundNum() > attackTurn) {
            if (enemyHqLoc == null) {
                getRealEnemyHQFromBlockchain(rc.getRoundNum() - 5);
            }
            if (enemyHqLoc == null) {
                attackTurn+=200;
            }
            if (!rc.isCurrentlyHoldingUnit()) {
                pickUpEnemyLandscaper();
                dfsWalk(enemyHqLoc);
            } else {
                dropInWater();
            }
        } else if (rc.getRoundNum() > moveToEnemyBaseTurn) {
            clearMovement();
            if (enemyHqLoc == null) {
                // if still null, search the blockchain
                getRealEnemyHQFromBlockchain();
            }

            //Checks if the enemyHQ is within the current robots sensor radius
            if (rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY), rc.getCurrentSensorRadiusSquared())) {
                if (nearbyEnemyRobot(RobotType.HQ) && enemyHqLoc == null) {
                    broadcastRealEnemyHQCoordinates();
                    enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                } else {
                    //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                    //switch to move to next potential location
                    enemyPotentialHQNumber++;
                }
            }


            //if enemy HQ is null, perform enemy base finding logic
            if (enemyHqLoc == null) {
                enemyBaseFindingLogic();
                navigation(new MapLocation(targetEnemyX, targetEnemyY));
            } else {
                goTo(enemyHqLoc);
            }
        } else {
//            getRealEnemyHQFromBlockchain(5);
//            if (enemyHqLoc != null) {
//                netGuns.add(enemyHqLoc);
//            }

//            pickUpAnyEnemyUnit();
//            if (enemyUnit) {
//                dropInWater();
//            }
            System.out.println("BC " + Clock.getBytecodesLeft());
            RobotInfo[] robotInfos = rc.senseNearbyRobots();
            //Has unit to pick up
            if (pickUpID == -1) {
                for (RobotInfo robot : robotInfos) {
                    if (robot.getType() == RobotType.LANDSCAPER &&
                            robot.getTeam() != rc.getTeam()) {
                        pickUpID = robot.ID;
                        pickUpLocation = robot.getLocation();
                        pickUpType = RobotType.LANDSCAPER;
                        sameTeam = false;
                        break;
                    }
                }

                for (RobotInfo robot : robotInfos) {
                    if (robot.getType() == RobotType.LANDSCAPER && !robot.getLocation().isAdjacentTo(hqLoc) &&
                            robot.getTeam() == rc.getTeam()) {
                        pickUpID = robot.ID;
                        pickUpLocation = robot.getLocation();
                        pickUpType = RobotType.LANDSCAPER;
                        sameTeam = true;
                        break;
                    }
//                else if (robot.getType() == RobotType.MINER && (!robot.getLocation().isAdjacentTo(hqLoc))) {
//                    dabating on adding this or just have miners suicide
//                    pickUpID = robot.ID;
//                    pickUpLocation = robot.getLocation();
//                    pickUpType = RobrotType.MINER;
//                }
                }
                if (pickUpID == -1) {
                    moveRandomly();
//                    moveRandomlyAroundHQ();
                }
            } else {
                if (rc.isCurrentlyHoldingUnit()) {
                    if (pickUpType == RobotType.LANDSCAPER) {
                        if (sameTeam) {
                            for (Direction dir : Util.directions) {
                                MapLocation targetLoc = rc.getLocation().add(dir);
                                if (targetLoc.isAdjacentTo(hqLoc)
                                        && rc.canDropUnit(rc.getLocation().directionTo(targetLoc))) {
                                    rc.dropUnit(dir);
                                    pickUpType = null;
                                    pickUpLocation = null;
                                    pickUpID = -1;
                                    sameTeam = true;
                                }
                            }
                            if (!rc.getLocation().equals(hqLoc)) {
                                dfsWalk(hqLoc);
                            }
                        } else {
                            dropInWater();
                        }
                    }
                } else {
                    if (rc.getLocation().isAdjacentTo(pickUpLocation) && !rc.canPickUpUnit(pickUpID)) {
                        pickUpID = -1;
                    } else if (rc.canPickUpUnit(pickUpID)) {
                        rc.pickUpUnit(pickUpID);
                    } else {
                        dfsWalk(pickUpLocation);
                    }
                }
            }
        }
    }

    public void getUberRequest() throws GameActionException {
        for (int i = rc.getRoundNum() - 20; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == UBER_REQUEST) {
                    minerLoc = new MapLocation(mess[4], mess[5]);
                    enemyPotentialHQNumber = mess[3];
                    firstMinerID = mess[2];
                }
            }
        }
    }

    public void broadcastPickedUpFirstMiner() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = PICKED_UP_MINER;
        message[2] = targetEnemyX; // possible x coord of enemy HQ
        message[3] = targetEnemyY; // possible y coord of enemy HQ
        message[4] = enemyPotentialHQNumber;
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }

    public void droneMoveToEnemyHQ() throws GameActionException {
        MapLocation targetLoc = new MapLocation(targetEnemyX, targetEnemyY);
        Direction dirToEnemyHQ = rc.getLocation().directionTo(targetLoc);
        Direction[] dirs = {dirToEnemyHQ, dirToEnemyHQ.rotateRight(), dirToEnemyHQ.rotateRight().rotateRight()};
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    @Override
    boolean tryMove(Direction dir) throws GameActionException {
        if (dir == null) {
            return false;
        }
        if (enemyHqLoc == null || rc.getRoundNum() > attackTurn) {
            //avoid netguns
            boolean hasNetGun = false;

            RobotInfo[] robotInfos = rc.senseNearbyRobots();
            for (RobotInfo robot : robotInfos) {
                if (robot.getType() == RobotType.NET_GUN) {
                    netGuns.add(robot.getLocation());
                }
                if (robot.getType() == RobotType.HQ && robot.getTeam() != rc.getTeam()) {
                    netGuns.add(robot.getLocation());
                    if (enemyHqLoc == null) {
                        enemyHqLoc = robot.getLocation();
                        targetEnemyX = robot.getLocation().x;
                        targetEnemyY = robot.getLocation().y;
                        broadcastRealEnemyHQCoordinates();
                    }
                }
            }
            System.out.println(enemyHqLoc);
            if (enemyHqLoc == null) {
                for (MapLocation loc : netGuns) {
                    System.out.println(loc + " " + rc.getLocation().add(dir).isWithinDistanceSquared(loc, 26));
                    if (rc.getLocation().add(dir).isWithinDistanceSquared(loc, 26)) {
                        hasNetGun = true;
                    }
                }
            }

            System.out.println("HAS " + hasNetGun);
            if (rc.isReady() && rc.canMove(dir) && !hasNetGun) {
                rc.move(dir);
                return true;
            } else return false;
        } else {
            if (rc.isReady() && rc.canMove(dir)
                    && !rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 25)) {
                rc.move(dir);
                return true;
            } else return false;
        }
    }

    void moveRandomlyAroundHQ() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 20)) {
                tryMove(dir);
                clearMovement();
            } else {
                dfsWalk(hqLoc);
            }
        }
    }

    void pickUpEnemyLandscaper() throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots();
        MapLocation landscaperLocation = null;
        for (RobotInfo robot : robotInfos) {
            if (robot.getType() == RobotType.LANDSCAPER &&
                    robot.getTeam() != rc.getTeam()) {
                if (rc.canPickUpUnit(robot.ID)) {
                    rc.pickUpUnit(robot.ID);
                } else {
                    landscaperLocation = rc.getLocation();
                }
            }
        }
        if (!rc.isCurrentlyHoldingUnit() && landscaperLocation != null) {
            goTo(landscaperLocation);
        }
    }

    void dropInWater() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (rc.canDropUnit(dir)) {
                rc.dropUnit(dir);
                pickUpID = -1;
                enemyUnit = false;
            }
        }

        if (nearestWater != null) {
            dfsWalk(nearestWater);
        } else {
            int sensorRadius = rc.getCurrentSensorRadiusSquared();
            for (int i = 0; i < sensorRadius; i++) {
                for (int j = 0; j < sensorRadius; j++) {
                    MapLocation mapLoc = new MapLocation(rc.getLocation().x + i, rc.getLocation().y + j);
                    if (rc.canSenseLocation(mapLoc) && rc.senseFlooding(mapLoc)) {
                        nearestWater = mapLoc;
                        break;
                    }
                }
            }
            goTo(Util.randomDirection());

        }
    }

    void moveRandomly() throws GameActionException {
        if (rc.getCooldownTurns() >= 0.5) {
            return;
        }
        System.out.println(rc.getCooldownTurns() + " " + randomDirection);
        if (randomDirection == null) {
            randomDirection = rc.getLocation().directionTo(hqLoc);
            if (rc.canMove(randomDirection)) {
                rc.move(randomDirection);
            }
        }
        System.out.println(randomDirection + " " + rc.canMove(randomDirection));
        if (!(randomDirectionCount-- > 0 && tryMove(randomDirection))) {
            randomDirectionCount = 1;
            randomDirection = randomDirection.rotateLeft();
        }
    }

    @Override
    public boolean goTo(MapLocation loc) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(loc);
        if (rc.canMove(dir)) {
            tryMove(dir);
        } else if (rc.canMove(dir.rotateLeft())) {
            tryMove(dir.rotateLeft());
        } else if (rc.canMove(dir.rotateRight())) {
            tryMove(dir.rotateRight());
        } else if (rc.canMove(dir.rotateLeft().rotateLeft())) {
            tryMove(dir.rotateLeft().rotateLeft());
        } else if (rc.canMove(dir.rotateRight().rotateRight())) {
            tryMove(dir.rotateRight().rotateRight());
        } else {
            return false;
        }
        return true;
    }

    public void pickUpAnyEnemyUnit() throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots();
        MapLocation enemyLoc = null;
        for (RobotInfo robot : robotInfos) {
            if (robot.getType() == RobotType.LANDSCAPER &&
                    robot.getTeam() != rc.getTeam()) {
                if (rc.canPickUpUnit(robot.ID)) {
                    rc.pickUpUnit(robot.ID);
                    enemyUnit = true;
                } else {
                    enemyLoc = rc.getLocation();
                }
            }
        }
        if (!rc.isCurrentlyHoldingUnit() && enemyLoc != null) {
            goTo(enemyLoc);
        }
    }

    public void getRealEnemyHQFromBlockchain(int rounds) throws GameActionException {
        for (int i = rc.getRoundNum() - rounds; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == ENEMY_HQ_LOC) {
                    System.out.println("got the real enemy HQ coord!");
                    enemyHqLoc = new MapLocation(mess[2], mess[3]);
                    netGuns.add(enemyHqLoc);
                    return;
                }
            }
        }
    }
}