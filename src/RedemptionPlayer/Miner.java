package RedemptionPlayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class Miner extends Unit {
    static int potentialEnemyHQX = -1;
    static int potentialEnemyHQY = -1;
    static boolean firstMiner = false;
    static int enemyPotentialHQNumber = 1;

    static int stuckMoves = 0;
    static int designSchoolCount = 0; //only first miner cares about this for now
    static int currentElevation = 0;
    static boolean startAttacking = false;
    static boolean giveUpMinerRush = false;
    static boolean pauseForFlight = false;
    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        mapLocations = new HashMap<>();
    }

    public void run() throws GameActionException {
        super.run();

        currentElevation = rc.senseElevation(rc.getLocation());

        System.out.println(rc.getMapHeight() + " " + rc.getMapWidth() + " " + currentElevation + " "
                + rc.senseElevation(rc.getLocation().add(Util.directions[0])) + " " + rc.senseElevation(rc.getLocation().add(Util.directions[2])));

        if (turnCount == 1 && rc.getRoundNum() == 2) {
            //Sets the first spawned miner to the first miner (that will be discovring enemy HQ)
            firstMiner = true;
            potentialEnemyHQY = rc.getMapHeight() - hqLoc.y - 1;
            potentialEnemyHQX = rc.getMapWidth() - hqLoc.x - 1;
        }

        // HQ broadcasts the position the round right before
//        if (turnCount == 5) {
//            getPotentialEnemyHQCoordinates();
//            System.out.println("potential coordinates " + potentialEnemyHQX + " " + potentialEnemyHQY);
//        }

        if (pauseForFlight)
            return;


        if (firstMiner) {
            System.out.println("First miner and Enemy hq is " + enemyHqLoc);
            System.out.println(startAttacking + " " + giveUpMinerRush);
            if (rc.getRoundNum() > 150 && !startAttacking) {
                giveUpMinerRush = true;
            }
            if (enemyHqLoc != null && !giveUpMinerRush) {
                System.out.println("rushing");
                //if miner is the first miner and enemy HQ is found, keep broadcasting
                //enemy HQ to new units and (build design schools nearby enemy HQ) -> this behavior should be optimized
                if (turnCount % 10 == 0) {
                    //Temporary way to stop broadcasting every turn when miner is around enemy HQ, because it uses too much soup.
                    broadcastRealEnemyHQCoordinates();
                }

                if (rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 6)) {
                    //create design school next to enemy HQ
                    startAttacking = true;
                    if (designSchoolCount < 1) {
                        if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection())) {
                            System.out.println("created a design school next to enemy HQ");
                            designSchoolCount++;
                        }
                    }
                } else {
                    //move towards enemy HQ to make sure your not divided by water, example map: DidMonkeyMakeThis
                    minerGoToEnemyHQ();
                }

            } else if (giveUpMinerRush && rc.getRoundNum() > 150) {
                System.out.println("Switching to drone delivery!");

                //Make fulfillment center
                for (Direction dir : Util.directions) {
                    if (tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                        System.out.println("\n=====================");
                        System.out.println("Fulfillmment center created!");
                        System.out.println("=====================\n");

                        pauseForFlight = true;
                        int[] message = new int[7];
                        message[0] = teamSecret;
                        message[1] = 222;
                        message[2] = rc.getID(); // supply id for pickup
                        message[3] = enemyPotentialHQNumber; // supply next target location
                        message[4] = rc.getLocation().x;
                        message[5] = rc.getLocation().y;
                        if (rc.canSubmitTransaction(message, 3))
                            rc.submitTransaction(message, 3);
                        System.out.println("Broadcasting uber request");
                        break;
                    }
                }
                //Band-aid function!  Part of turtle
//                if (nearbyTeamRobot(RobotType.HQ)) {
//                    if (designSchoolCount < 1) {
//                        if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection())) {
//                            System.out.println("created a design school next to HQ");
//                            designSchoolCount++;
//                        }
//                    }
//                }
//                goTo(hqLoc);
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

                minerGoToEnemyHQ();
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

            //Band-aid function!  Part of turtle
            if (enemyHqLoc == null && rc.getRoundNum() > 250 && rc.getTeamSoup() > 500) {
                if (designSchoolCount < 1) {
                    if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection())) {
                        System.out.println("created a design school next to HQ");
                        designSchoolCount++;
                    }
                }
            }

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
                MapLocation closestSoup = null;
                MapLocation[] soupToMine = rc.senseNearbySoup();
                int shortestDistance = 100;
                //mine the closest soup to avoid bug that occurs on map like "spiral"
                for (MapLocation soupLoc : soupToMine) {
                    int distanceToSoup = soupLoc.distanceSquaredTo(rc.getLocation());
                    if (distanceToSoup < shortestDistance) {
                        shortestDistance = distanceToSoup;
                        closestSoup = soupLoc;
                    }
                }
                if (soupToMine.length == 0 || closestSoup == null) {
                    // If no soup is nearby, search for soup by moving randomly
                    System.out.println("No soup nearby");
                    if (goTo(Util.randomDirection())) {
                        System.out.println("I moved randomly!");
                    }
                } else {
                    if (Math.abs(closestSoup.x - rc.getLocation().x) < 2 &&
                            Math.abs(closestSoup.y - rc.getLocation().y) < 2) {
                        System.out.println("Soup nearby and within mine distance");
                        // Else if soup is adjacent, mine it
                        for (Direction dir : Util.directions) {
                            if (tryMine(dir)) {
                                System.out.println("I mined soup! " + rc.getSoupCarrying());
                            }
                        }
                    } else {
                        System.out.println("Moving towards soup to mine " + closestSoup);
                        // Otherwise, travel towards the detected soup
                        if (closestSoup.x > rc.getLocation().x) {
    //                        if (rc.canMove(Direction.EAST))
    //                            rc.move(Direction.EAST);
                            tryMove(Direction.EAST);
                        } else if (closestSoup.x < rc.getLocation().x) {
    //                        if (rc.canMove(Direction.WEST))
    //                            rc.move(Direction.WEST);
                            tryMove(Direction.WEST);
                        }
                        if (closestSoup.y > rc.getLocation().y) {
    //                        if (rc.canMove(Direction.NORTH))
    //                            rc.move(Direction.NORTH);
                            tryMove(Direction.NORTH);
                        } else if (closestSoup.y < rc.getLocation().y) {
    //                        if (rc.canMove(Direction.SOUTH))
    //                            rc.move(Direction.SOUTH);
                            tryMove(Direction.SOUTH);
                        }
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



    public void minerGoToEnemyHQ () throws GameActionException{
        if (stuckMoves > 0) {
            for (Direction dir : Util.directions) {
                if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.senseFlooding(rc.getLocation().add(dir))
                        || !rc.canMove(dir)) {
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
}
