package redemptionplayer;

import battlecode.common.*;

public class Drone extends Unit {
    static MapLocation minerLoc = null;
    static int enemyPotentialHQNumber = 0;
    static int firstMinerID = 0;
    static boolean droppedOffFirstMiner = false;
    static boolean notPickupDrone = false;
    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
//        System.out.println("Drone " + notPickupDrone + " " + enemyHqLoc);

        if (notPickupDrone) {

        } else {
            if (!rc.isCurrentlyHoldingUnit() && !droppedOffFirstMiner) {
                if (minerLoc == null) {
                    getUberRequest();
                }
                if (minerLoc != null) {
                    //This is the drone to pick up miner
                    System.out.println("Next HQ to try: " + enemyPotentialHQNumber);
                    System.out.println("Miner Found: " + firstMinerID);
                    if (rc.canPickUpUnit(firstMinerID)) {
                        System.out.println("I can pick up a miner!");
                        broadcastPickedUpFirstMiner(); //broadcast here because drone could be destroyed by net guns before dropping off
                        rc.pickUpUnit(firstMinerID);
                    } else {
                        goTo(minerLoc);
                    }
                } else {
                    //This is drones spawned near our HQ
                    notPickupDrone = true;
                }
            } else {
                potentialEnemyHQY = rc.getMapHeight() - hqLoc.y - 1;
                potentialEnemyHQX = rc.getMapWidth() - hqLoc.x - 1;

                System.out.println(enemyHqLoc);
                if (enemyHqLoc != null) {
                    //drop off miner before in HQ's shooting range
                    if (rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 21)) {
                        System.out.println("Thanks for choosing Uber!");
                        for (Direction dir : Util.directions) {
                            if (rc.canDropUnit(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                                rc.dropUnit(dir);
                                droppedOffFirstMiner = true;
                                System.out.println("Thanks for choosing Uber!");
                            }
                        }
                    } else {
                        droneMoveToEnemyHQ();
                    }
                } else if (turnCount > 5 && rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY),
                        rc.getCurrentSensorRadiusSquared())) {
                    if (nearbyEnemyRobot(RobotType.HQ)) {
                        System.out.println("Found real enemy HQ coordinates");
                        enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                    } else {
                        //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                        //switch to move to next potential location
                        enemyPotentialHQNumber++;
                    }
                }

                switch (enemyPotentialHQNumber) {
                    case 1:
                        targetEnemyX = hqLoc.x;
                        targetEnemyY = potentialEnemyHQY;
                        break;
                    case 2:
                        targetEnemyX = potentialEnemyHQX;
                        targetEnemyY = potentialEnemyHQY;
                        break;
                    case 3:
                        targetEnemyX = potentialEnemyHQX;
                        targetEnemyY = hqLoc.y;
                        break;
                }

                droneMoveToEnemyHQ();
            }

            if (droppedOffFirstMiner) {
                Team enemy = rc.getTeam().opponent();
                if (!rc.isCurrentlyHoldingUnit()) {
                    // See if there are any enemy robots within capturing range
                    RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);
                    for (RobotInfo robot : robots) {
                        // Pick up a first landscaper within range
                        if (robot.getType() == RobotType.LANDSCAPER) {
                            rc.pickUpUnit(robot.getID());
                            System.out.println("I picked up enemy landscaper" + robot.getID() + "!");
                        }
                    }
                    // No close robots, so search for robots within sight radius
                    tryMove(Util.randomDirection());
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
