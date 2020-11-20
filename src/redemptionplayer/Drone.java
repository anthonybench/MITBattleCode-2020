package redemptionplayer;

import battlecode.common.*;

public class Drone extends Unit {
    static MapLocation minerLoc = null;
    static int firstMinerID = 0;
    static int pickUpID = -1;
    static RobotType pickUpType;
    static MapLocation pickUpLocation;
    static boolean sameTeam = true;
    static MapLocation nearestWater;
    static int moveToEnemyBaseTurn = 1000;
    static int attackTurn = 1200;

    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        //attack enemy HQ, kill their landscapers so we have higher wall.
        if (rc.getRoundNum() > attackTurn) {
            System.out.println("PU " + pickUpID + " " + enemyHqLoc);
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
                System.out.println("Sensor " + rc.getCurrentSensorRadiusSquared());
                if (nearbyEnemyRobot(RobotType.HQ)) {
                    System.out.println("Found real enemy HQ coordinates");
                    System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                    broadcastRealEnemyHQCoordinates();
                    enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                } else {
                    System.out.println("Sensor 111" + rc.getCurrentSensorRadiusSquared());
                    //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                    //switch to move to next potential location
                    enemyPotentialHQNumber++;
                }
            }

            System.out.println("targeting coordinates " + enemyPotentialHQNumber + " " + targetEnemyX + " " + targetEnemyY);

            //if enemy HQ is null, perform enemy base finding logic
            if (enemyHqLoc == null) {
                enemyBaseFindingLogic();
                dfsWalk(new MapLocation(targetEnemyX, targetEnemyY));
            } else {
                dfsWalk(enemyHqLoc);
            }
        } else {

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
                        break;
                    }
//                else if (robot.getType() == RobotType.MINER && (!robot.getLocation().isAdjacentTo(hqLoc))) {
//                    dabating on adding this or just have miners suicide
//                    pickUpID = robot.ID;
//                    pickUpLocation = robot.getLocation();
//                    pickUpType = RobotType.MINER;
//                }
                }
                if (pickUpID == -1) {
                    moveRandomlyAroundHQ();
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
                    System.out.println("Retrieved uber instructions!");
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
        System.out.println("Picked Up First Miner");
    }

    public void droneMoveToEnemyHQ() throws GameActionException {
        MapLocation targetLoc = new MapLocation(targetEnemyX, targetEnemyY);
        Direction dirToEnemyHQ = rc.getLocation().directionTo(targetLoc);
        Direction[] dirs = {dirToEnemyHQ, dirToEnemyHQ.rotateRight(), dirToEnemyHQ.rotateRight().rotateRight()};
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                System.out.println("Moving towards enemy HQ " + dir);
                rc.move(dir);
            }
        }
    }

    @Override
    boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (enemyHqLoc == null || rc.getRoundNum() > attackTurn) {
            if (rc.isReady() && rc.canMove(dir)) {
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
            if (hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 10)) {
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
            }
        }

        if (nearestWater != null) {
            dfsWalk(nearestWater);
        } else {
            int sensorRadius = rc.getCurrentSensorRadiusSquared();
            for (int i = -sensorRadius; i < sensorRadius; i++) {
                for (int j = -sensorRadius; j < sensorRadius; j++) {
                    MapLocation mapLoc = new MapLocation(rc.getLocation().x + i, rc.getLocation().y + j);
                    if (rc.canSenseLocation(mapLoc) && rc.senseFlooding(mapLoc)) {
                        nearestWater = mapLoc;
                        break;
                    }
                }
            }
            goTo(Util.randomDirection());

            System.out.println("AFTER ALL THAT " + Clock.getBytecodesLeft());
        }
    }
}