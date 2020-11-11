package examplefuncsplayer;

import battlecode.common.*;
import redemptionplayer.Util;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static MapLocation hqLoc;
    static MapLocation enemyHqLoc;
    static int numMiners = 0;
    static int firstMiner = -1;
    static int secondMiner = -1;
    static int teamSecret = 123456;
    static int mapWidth = 30;
    static int mapHeight = 30;
    static int enemyHqX = -1;
    static int enemyHqY = -1;
    static int possibleY = 0;
    static int possibleX = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        findHQ();
//        findEnemyHQ();
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:
                        runHQ();
                        break;
                    case MINER:
                        runMiner();
                        break;
                    case REFINERY:
                        runRefinery();
                        break;
                    case VAPORATOR:
                        runVaporator();
                        break;
                    case DESIGN_SCHOOL:
                        runDesignSchool();
                        break;
                    case FULFILLMENT_CENTER:
                        runFulfillmentCenter();
                        break;
                    case LANDSCAPER:
                        runLandscaper();
                        break;
                    case DELIVERY_DRONE:
                        runDeliveryDrone();
                        break;
                    case NET_GUN:
                        runNetGun();
                        break;
                    default:
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                }
            }
            if (hqLoc == null) {
                // if still null, search the blockchain
                getHqLocFromBlockchain();
            }
        }
    }

    static void findEnemyHQ() throws GameActionException {
        if (enemyHqLoc == null) {
            // search surroundings for enemy HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                    enemyHqLoc = robot.location;
                }
            }
        }
    }

    static void runHQ() throws GameActionException {
        if (turnCount == 1) {
            sendHqLoc(rc.getLocation());
        }

        int targetID = nearbyEnemyDrone();
        if (targetID != -1 && rc.canShootUnit(targetID)) {
            rc.shootUnit(targetID);
        }

        if (mapWidth == 30 && mapHeight == 30) {
            //find current map width
            while (rc.onTheMap(new MapLocation(mapWidth + 1, 0))) {
                mapWidth++;
            }
            //find current map height
            while (rc.onTheMap(new MapLocation(0, mapHeight + 1))) {
                mapHeight++;
            }
            System.out.println("BOUNDARIES" + mapWidth + " " + mapHeight);

            //horizontally symmetric
            possibleX = mapWidth - rc.getLocation().x;
            possibleY = mapHeight - rc.getLocation().y;
        }
        System.out.println("Possible points" + possibleX + " " + possibleY);

//        broadcastEnemyHQCoordinates();

        if (numMiners < 6) {
            for (Direction dir : directions)
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                }
        }
    }

    static boolean nearbyTeamRobot(RobotType target) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == target && r.team == rc.getTeam()) {
                return true;
            }
        }
        return false;
    }

    static void runMiner() throws GameActionException {
        tryBlockchain();

        if (rc.getLocation().isAdjacentTo(hqLoc) && !nearbyTeamRobot(RobotType.DESIGN_SCHOOL)) {
            for (Direction dir : directions) {
                if (!hqLoc.isAdjacentTo(rc.getLocation().add(dir)) && tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                    System.out.println("created a design school next to HQ");
                }
            }
        }

        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
//        for (Direction dir : directions)
//            if (tryMine(dir))
//                System.out.println("I mined soup! " + rc.getSoupCarrying());

        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
            // time to go back to the HQ
            if (goTo(hqLoc))
                System.out.println("moved towards HQ");
        } else {
            // Try to find soup
            MapLocation[] soupToMine = rc.senseNearbySoup();
            if (soupToMine.length == 0) {
                // If no soup is nearby, search for soup by moving randomly
                if (goTo(randomDirection())) {
                    System.out.println("I moved randomly!");
                }
            } else if (Math.abs(soupToMine[0].x - rc.getLocation().x) < 2 &&
                    Math.abs(soupToMine[0].y - rc.getLocation().y) < 2) {
                // Else if soup is adjacent, mine it
                for (Direction dir : directions) {
                    if (tryMine(dir)) {
                        System.out.println("I mined soup! " + rc.getSoupCarrying());
                    }
                }
            } else {
                // Otherwise, travel towards the detected soup
                if (soupToMine[0].x > rc.getLocation().x) {
                    if (rc.canMove(Direction.EAST))
                        rc.move(Direction.EAST);
                } else if (soupToMine[0].x < rc.getLocation().x) {
                    if (rc.canMove(Direction.WEST))
                        rc.move(Direction.WEST);
                }
                if (soupToMine[0].y > rc.getLocation().y) {
                    if (rc.canMove(Direction.NORTH))
                        rc.move(Direction.NORTH);
                } else if (soupToMine[0].y < rc.getLocation().y) {
                    if (rc.canMove(Direction.SOUTH))
                        rc.move(Direction.SOUTH);
                }
            }
        }
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        for (Direction dir : directions) {
            if (tryBuild(RobotType.LANDSCAPER, dir)) {
                System.out.println("Made a landscaper");
            }
        }
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
// first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
            if (rc.canDigDirt(dirtohq)) {
                rc.digDirt(dirtohq);
            }
        }

        if (rc.getDirtCarrying() == 0) {
            tryDig();
        }

        MapLocation bestPlaceToBuildWall = null;
        // find best place to build
        if (hqLoc != null) {
            int lowestElevation = 9999999;
            for (Direction dir : directions) {
                MapLocation tileToCheck = hqLoc.add(dir);
                if (rc.getLocation().distanceSquaredTo(tileToCheck) < 4
                        && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                    if (rc.senseElevation(tileToCheck) < lowestElevation) {
                        lowestElevation = rc.senseElevation(tileToCheck);
                        bestPlaceToBuildWall = tileToCheck;
                    }
                }
            }
        }

        if (Math.random() < 0.8) {
            // build the wall
            if (bestPlaceToBuildWall != null) {
                rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                System.out.println("building a wall");
            }
        }

        // otherwise try to get to the hq
        if (hqLoc != null) {
            goTo(hqLoc);
        } else {
            goTo(randomDirection());
        }
    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within capturing range
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir  The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }

    // tries to move in the general direction of dir
    static boolean goTo(Direction dir) throws GameActionException {
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry) {
            if (tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    static boolean goTo(MapLocation destination) throws GameActionException {
        return goTo(rc.getLocation().directionTo(destination));
    }

    static boolean nearbyRobot(RobotType target) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == target) {
                return true;
            }
        }
        return false;
    }

    static boolean tryDig() throws GameActionException {
        Direction dir = randomDirection();
        if (rc.canDigDirt(dir)) {
            rc.digDirt(dir);
            return true;
        }
        return false;
    }

    public static void broadcastFirstMiner() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 1;
        message[2] = rc.getID(); // x coord of HQ
        message[3] = 0;
        if (rc.canSubmitTransaction(message, 3)) {
            rc.submitTransaction(message, 3);
            firstMiner = rc.getID();
        }
    }

    public static void updateFirstMiner() throws GameActionException {
        for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] mess = tx.getMessage();
            if (mess[0] == teamSecret && mess[1] == 1) {
                System.out.println("Got a first miner");
                firstMiner = mess[2];
            }
        }
    }

    public static void broadcastSecondMiner() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 2;
        message[2] = rc.getID(); // x coord of HQ
        message[3] = 0;
        if (rc.canSubmitTransaction(message, 3)) {
            rc.submitTransaction(message, 3);
            secondMiner = rc.getID();
        }
    }

    public static void updateSecondMiner() throws GameActionException {
        for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] mess = tx.getMessage();
            if (mess[0] == teamSecret && mess[1] == 2) {
                System.out.println("Got a first miner");
                secondMiner = mess[2];
            }
        }
    }

    public static void sendHqLoc(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 0;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }

    public static void getHqLocFromBlockchain() throws GameActionException {
        System.out.println("B L O C K C H A I N");
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == 0) {
                    System.out.println("found the HQ!");
                    hqLoc = new MapLocation(mess[2], mess[3]);
                }
            }
        }
    }

    public static void broadcastEnemyHQCoordinates() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 11;
        message[2] = possibleX; // possible x coord of enemy HQ
        message[3] = possibleY; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }

    public static void getEnemyHQCoordinates() throws GameActionException {
        for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] mess = tx.getMessage();
            if (mess[0] == teamSecret && mess[1] == 11) {
                System.out.println("Got enemy HQ coordinates");
                enemyHqX = mess[2];
                enemyHqY = mess[3];
                System.out.println(enemyHqX + enemyHqY);
            }
        }
    }

    public static int nearbyEnemyDrone() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.DELIVERY_DRONE && r.team != rc.getTeam()) {
                return r.getID();
            }
        }
        return -1;
    }
}
