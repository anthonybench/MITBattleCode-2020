package RedemptionPlayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class Miner extends Unit {

    static boolean firstMiner = false;
    static int enemyPotentialHQNumber = 1;
    static int targetEnemyX = -100;
    static int targetEnemyY = -100;
    static int stuckMoves = 0;
    static int designSchoolCount = 0; //only first miner cares about this for now

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        mapLocations = new HashMap<>();
    }

    public void run() throws GameActionException {
        super.run();

        //Sets the first spawned miner to the first miner (that will be discovring enemy HQ)
        if (turnCount == 1 && rc.getRoundNum() == 2) {
            firstMiner = true;
        }

        // HQ broadcasts the position the round right before
        if (turnCount == 5) {
            getPotentialEnemyHQCoordinates();
            System.out.println("potential coordinates " + potentialEnemyHQX + " " + potentialEnemyHQY);
        }

        if (firstMiner) {
            System.out.println("First miner and Enemy hq is " + enemyHqLoc);
            if (enemyHqLoc != null) {
                //if miner is the first miner and enemy HQ is found, keep broadcasting
                //enemy HQ to new units and (build design schools nearby enemy HQ) -> this behavior should be optimized
                if (turnCount % 10 == 0) {
                    //Temporary way to stop broadcasting every turn when miner is around enemy HQ, because it uses too much soup.
                    broadcastRealEnemyHQCoordinates();
                }
                //create design school next to enemy HQ
                if (designSchoolCount < 1) {
                    if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection()))
                        System.out.println("created a design school next to enemy HQ");
                    designSchoolCount++;
                }
            } else if (enemyHqLoc == null && rc.getRoundNum() > 200) {
                if (nearbyRobot(RobotType.HQ)) {
                    if (designSchoolCount < 3) {
                        if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection()))
                            System.out.println("created a design school next to HQ");
                        designSchoolCount++;
                    }
                }
                goTo(hqLoc);
            } else {
                //If enemy HQ is not found yet and is within miner's sensor radius, broadcast enemy HQ position
                System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                //Checks if the enemyHQ is within the current robots sensor radius
                if (turnCount > 5 &&
                        rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY), rc.getCurrentSensorRadiusSquared())) {
                    if (nearbyEnemyRobot(RobotType.HQ)) {
                        System.out.println("Found real enemy HQ coordinates");
                        broadcastRealEnemyHQCoordinates();
                        enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);

                        System.out.println("cooldown" + rc.getCooldownTurns());
                    } else {
                        //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                        //switch to move to next potential location
                        enemyPotentialHQNumber++;
                    }
                }

                System.out.println("Target HQ " + enemyPotentialHQNumber);
                //Sets the first miner's targeted locations
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
                System.out.println("1 " + hqLoc.x + " " + potentialEnemyHQY);
                System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);

                if (stuckMoves > 0) {
                    System.out.println(mapWidth + "Latest!" + mapHeight);
                    for (Direction dir : Util.directions) {
                        System.out.println("Dir " + dir);
                        if (rc.senseFlooding(rc.getLocation().add(dir)) || !rc.canMove(dir)) {
                            System.out.println("Dir1 " + dir);
                            tryMove(dir.rotateRight());
                        } else {
                            tryMove(rc.getLocation().directionTo(new MapLocation(targetEnemyX, targetEnemyY)));
                        }
                    }
//                    tryMove( rc.getLocation().directionTo(new MapLocation(mapWidth / 2, mapHeight / 2)));
                    stuckMoves--;
                }

                if (goTo(new MapLocation(targetEnemyX, targetEnemyY))) {
                    if (mapLocations.containsKey(rc.getLocation())) {
                        stuckMoves = 5;
                    } else {
                        mapLocations.put(rc.getLocation(), 1);
                    }
                }
            }
        } else {
            System.out.println("Not first miner");
            //        if (enemyHqX == -1 && enemyHqY == -1) {
//            getEnemyHQCoordinates();
//            enemyHqLoc = new MapLocation(enemyHqX, enemyHqY);
//        } else if (enemyHqY != -1 && enemyHqX != -1) {
//            System.out.println("Enemy HQ" + enemyHqLoc);
//            if (goTo(enemyHqLoc)) {
//                System.out.println("Went to possible enemy HQ coordinate" + enemyHqX + ", " + enemyHqY);
//            } else {
//                System.out.println("Couldn't move to enemy HQ");
//            }
//        }
//            if (!nearbyRobot(RobotType.DESIGN_SCHOOL)) {
//                if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection()))
//                    System.out.println("created a design school");
//            }

//        for (Direction dir : directions)
//            if (tryMine(dir))
//                System.out.println("I mined soup! " + rc.getSoupCarrying());
            for (Direction dir : Util.directions)
                if (tryRefine(dir))
                    System.out.println("I refined soup! " + rc.getTeamSoup());

            int whenToStopMiningSoup = 50; //used to be RobotType.MINER.soupLimit;
            if (rc.getSoupCarrying() >= whenToStopMiningSoup) {
                // time to go back to the HQ
                if (goTo(hqLoc))
                    System.out.println("moved towards HQ");
            } else {
                System.out.println("Before trying to find soup");
                // Try to find soup
                MapLocation[] soupToMine = rc.senseNearbySoup();
                if (soupToMine.length == 0) {
                    // If no soup is nearby, search for soup by moving randomly
                    if (goTo(Util.randomDirection())) {
                        System.out.println("I moved randomly!");
                    }
                } else if (Math.abs(soupToMine[0].x - rc.getLocation().x) < 2 &&
                        Math.abs(soupToMine[0].y - rc.getLocation().y) < 2) {
                    // Else if soup is adjacent, mine it
                    for (Direction dir : Util.directions) {
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
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMine(Direction dir) throws GameActionException {
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
    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    public void broadcastRealEnemyHQCoordinates() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 111;
        message[2] = targetEnemyX; // possible x coord of enemy HQ
        message[3] = targetEnemyY; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
        System.out.println("Broadcasting real enemy HQ coordinates");
    }
}
