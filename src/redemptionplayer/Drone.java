package redemptionplayer;

import battlecode.common.*;

public class Drone extends Unit {
    static MapLocation minerLoc = null;
    static int enemyPotentialHQNumber = 0;
    static int firstMinerID = 0;
    static int pickUpID = -1;
    static RobotType pickUpType;
    static MapLocation pickUpLocation;
    static boolean sameTeam = true;
    static MapLocation nearestWater;

    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        MapLocation topLeft = hqLoc.add(Util.directions[7]);
        MapLocation topRight = hqLoc.add(Util.directions[1]);
        MapLocation bottomLeft = hqLoc.add(Util.directions[5]);
        MapLocation bottomRight = hqLoc.add(Util.directions[3]);

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
                if (rc.canPickUpUnit(pickUpID)) {
                    rc.pickUpUnit(pickUpID);
                } else {
                    dfsWalk(pickUpLocation);
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
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
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

    void dropInWater () throws GameActionException {
        if (nearestWater != null) {
            if (rc.getLocation().isAdjacentTo(nearestWater)
                    && rc.canDropUnit(rc.getLocation().directionTo(nearestWater))) {
                rc.dropUnit(rc.getLocation().directionTo(nearestWater));
                pickUpID = -1;
            }
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
