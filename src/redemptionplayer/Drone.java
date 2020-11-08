package redemptionplayer;

import battlecode.common.*;

public class Drone extends Unit {
    static MapLocation minerLoc = null;
    static int enemyPotentialHQNumber = 0;
    static int firstMinerID = 0;
    static int pickUpID = -1;
    static RobotType pickUpType;
    static MapLocation pickUpLocation;

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
                if (robot.getType() == RobotType.LANDSCAPER && (!robot.getLocation().equals(topLeft) &&
                        !robot.getLocation().equals(topRight) && !robot.getLocation().equals(bottomLeft)
                        && !robot.getLocation().equals(bottomRight))) {
                    pickUpID = robot.ID;
                    pickUpLocation = robot.getLocation();
                    pickUpType = RobotType.LANDSCAPER;
                }
            }
        } else {
            if (rc.isCurrentlyHoldingUnit()) {
                if (pickUpType == RobotType.LANDSCAPER) {
                    for (Direction dir : Util.directions) {
                        MapLocation targetLoc = rc.getLocation().add(dir);
                        if ((targetLoc.equals(topLeft) || targetLoc.equals(topRight)
                                || targetLoc.equals(bottomLeft) || targetLoc.equals(bottomRight))
                                && rc.canDropUnit(dir)) {
                            rc.dropUnit(dir);
                            pickUpType = null;
                            pickUpLocation = null;
                            pickUpID = -1;
                        }
                    }
                    if (!rc.getLocation().equals(hqLoc)) {
                        dfsWalk(hqLoc);
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
}
