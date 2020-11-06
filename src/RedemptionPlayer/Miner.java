package RedemptionPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Miner extends Unit {
    static int potentialEnemyHQX = -1;
    static int potentialEnemyHQY = -1;
    static boolean firstMiner = false;
    static boolean builtDesignSchool = false;
    static int stuckMoves = 0;
    static int designSchoolCount = 0; //only first miner cares about this for now
    static int currentElevation = 0;
    static boolean startAttacking = false;
    static boolean pauseForFlight = false;
    static boolean droppedOff = false;
    static boolean switchToDroneRush = false;
    static ArrayList<MapLocation> refineLocations;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        mapLocations = new HashMap<>();
        refineLocations = new ArrayList<>();
    }

    public void run() throws GameActionException {
        super.run();

        System.out.println("MINER!!!!");
        currentElevation = rc.senseElevation(rc.getLocation());

        if (turnCount == 1 && rc.getRoundNum() == 2) {
            //Sets the first spawned miner to the first miner (that will be discovring enemy HQ)
            firstMiner = true;
            potentialEnemyHQY = rc.getMapHeight() - hqLoc.y - 1;
            potentialEnemyHQX = rc.getMapWidth() - hqLoc.x - 1;
            refineLocations.add(hqLoc);
        }

//        getRefineryLocation();
        System.out.println("Bytecode 1" + Clock.getBytecodeNum());
        if (firstMiner && pauseForFlight && !droppedOff) {
            //only check for this for the first miner after it built a fulfillment center and waited to be picked up
            getPickedUpFirstMiner();
        }
        System.out.println("Bytecode 2" + Clock.getBytecodeNum());

        if (droppedOff) {
            //to restart rush after drone dropping miner off
            pauseForFlight = false;
            startAttacking = true;
            switchToDroneRush = false;
            nearbyEnemyHQLocation();
        }

        if (pauseForFlight)
            return;

        if (firstMiner) {
            System.out.println("First miner and Enemy hq is " + enemyHqLoc);
            if (rc.getRoundNum() > 200 && !startAttacking) {
                switchToDroneRush = true;
            }

            if (enemyHqLoc != null && !switchToDroneRush) {
                System.out.println("rushing");
                //if miner is the first miner and enemy HQ is found, keep broadcasting
                //enemy HQ to new units and (build design schools nearby enemy HQ) -> this behavior should be optimized
                if (turnCount % 10 == 0) {
                    //Temporary way to stop broadcasting every turn when miner is around enemy HQ, because it uses too much soup.
                    broadcastRealEnemyHQCoordinates();
                }

                if (rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 6)) {
                    System.out.println("Start Attack");
                    //create design school next to enemy HQ
                    startAttacking = true;
                    //build net gun if there's enemy delievery drones nearby
                    if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
                        for (Direction dir : Util.directions) {
                            if (tryBuild(RobotType.NET_GUN, dir)) {
                                break;
                            } else {
                                if (!broadcastedHalt && haltProduction) {
                                    broadcastHaltProduction();
                                    broadcastedHalt = true;
                                }
                                if (!broadcastedCont && !haltProduction) {
                                    broadcastContinueProduction();
                                    broadcastedCont = true;
                                }
                            }
                        }
                    } else if (designSchoolCount < 1) {
                        for (Direction dir : Util.directions) {
                            if (tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                                designSchoolCount++;
                                if (!broadcastedHalt && haltProduction) {
                                    broadcastContinueProduction();
                                    broadcastedHalt = true;
                                }
                                break;
                            } else {
                                if (!broadcastedCont && !haltProduction) {
                                    broadcastHaltProduction();
                                    broadcastedCont = true;
                                }
                            }
                        }
                    }
                } else {
                    //move towards enemy HQ to make sure your not divided by water, example map: DidMonkeyMakeThis
                    discoverEnemyHQ(new MapLocation(targetEnemyX, targetEnemyY));
                }
            } else if (switchToDroneRush && !droppedOff) {
                System.out.println("Switching to drone delivery!");
                if (switchToDroneRush)
                    droneRush();
                //Make fulfillment center
            } else {
                //If enemy HQ is not found yet and is within miner's sensor radius, broadcast enemy HQ position
                System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                //Checks if the enemyHQ is within the current robots sensor radius
                if (turnCount > 5 &&
                        rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY), rc.getCurrentSensorRadiusSquared())) {
                    if (nearbyEnemyRobot(RobotType.HQ)) {
                        System.out.println("Found real enemy HQ coordinates");
                        nearbyEnemyHQLocation(); //Special case is map spiral, two potential locations can be sensed together.
                        System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                        broadcastRealEnemyHQCoordinates();
                        enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                    } else {
                        //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                        //switch to move to next potential location
                        enemyPotentialHQNumber++;
                    }
                }

                if (enemyHqLoc == null) {
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
                    System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                }
                //Hills
                if (rc.getRoundNum() < 16) {
                    minerGoToEnemyHQ();
                } else {
                    discoverEnemyHQ(new MapLocation(targetEnemyX, targetEnemyY));
                }
            }
        } else {
            System.out.println("Not first miner");

            //HQ defense
            if (rc.getTeamSoup() > 150 && rc.getLocation().isAdjacentTo(hqLoc) && !nearbyTeamRobot(RobotType.DESIGN_SCHOOL)) {
                for (Direction dir : Util.directions) {
                    if (!hqLoc.isAdjacentTo(rc.getLocation().add(dir)) && tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                        System.out.println("created a design school next to HQ");
                        designSchoolCount++;
                    }
                }
            }


            for (Direction dir : Util.directions) {
                if (tryRefine(dir)) {
                    System.out.println("I refined soup! " + rc.getTeamSoup());
                }
            }

//            buildRefineryAtSoupAreaFarFromHQ();

            int whenToStopMiningSoup = RobotType.MINER.soupLimit; //used to be RobotType.MINER.soupLimit;
            if (rc.getSoupCarrying() >= whenToStopMiningSoup) {
                depositSoupAtNearestRefineLocation();
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
                        dfsWalk(closestSoup);
//                        if (closestSoup.x > rc.getLocation().x) {
//    //                        if (rc.canMove(Direction.EAST))
//    //                            rc.move(Direction.EAST);
//                            tryMove(Direction.EAST);
//                        } else if (closestSoup.x < rc.getLocation().x) {
//    //                        if (rc.canMove(Direction.WEST))
//    //                            rc.move(Direction.WEST);
//                            tryMove(Direction.WEST);
//                        }
//                        if (closestSoup.y > rc.getLocation().y) {
//    //                        if (rc.canMove(Direction.NORTH))
//    //                            rc.move(Direction.NORTH);
//                            tryMove(Direction.NORTH);
//                        } else if (closestSoup.y < rc.getLocation().y) {
//    //                        if (rc.canMove(Direction.SOUTH))
//    //                            rc.move(Direction.SOUTH);
//                            tryMove(Direction.SOUTH);
//                        }
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


    public void minerGoToEnemyHQ() throws GameActionException {
        System.out.println("Moving to enemy HQ");
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
                System.out.println("Stuck");
                stuckMoves = 5;
            } else {
                mapLocations.put(rc.getLocation(), 1);
            }
        }
    }

    public void getPickedUpFirstMiner() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == PICKED_UP_MINER) {
                    enemyPotentialHQNumber = mess[4];
                    System.out.println("This is the miner after being dropped off! " + enemyPotentialHQNumber);
                    droppedOff = true;
                }
            }
        }
    }

    void nearbyEnemyHQLocation() {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.HQ && r.team != rc.getTeam()) {
                targetEnemyX = r.getLocation().x;
                targetEnemyY = r.getLocation().y;
                enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                return;
            }
        }
    }

    void droneRush() throws GameActionException {
        //ensure that there's enough soup to send message after building fulfillment center
        if (rc.getTeamSoup() > 154) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                    System.out.println("\n=====================");
                    System.out.println("Fulfillmment center created!");
                    System.out.println("=====================\n");

                    pauseForFlight = true;
                    int[] message = new int[7];
                    message[0] = teamSecret;
                    message[1] = UBER_REQUEST;
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
        }
    }

    void discoverEnemyHQ(MapLocation enemyHQ) throws GameActionException {
        Direction targetDirection = rc.getLocation().directionTo(enemyHQ);
        System.out.println("============================================");
        System.out.println("Bytecode 3" + Clock.getBytecodeNum());
        System.out.println("!!!Moving towards " + targetDirection + " " + discoverDir + " " + prevSplitLocations.size());
        System.out.println("Prev split " + prevSplitLocation + " " + rc.getLocation());

        if (prevSplitLocation != null && rc.getLocation().equals(prevSplitLocation.getKey())) {
            System.out.println("At previous split loc " + prevSplitLocation.getKey() + " " + prevSplitLocation.getValue());
            if (discoverDir.equals("right")) {
                discoverDir = "left";
                headBackToPrevSplitLocation = false;
            } else if (discoverDir.equals("left")) {
                headBackToPrevSplitLocation = true;
                System.out.println("Left " + prevSplitLocations.size());
                if (!prevSplitLocations.empty()) {
                    prevSplitLocation = prevSplitLocations.pop();
                    discoverDir = "right";
                }
                if (prevSplitLocations.empty()) {
                    //broadcast
                    discoverDir = "left"; //to get back into this condition.
                    System.out.println("Stuck, switching to drones");
                    switchToDroneRush = true;
                    return;
                }
            }
        } else if (prevSplitLocation != null && rc.getLocation() != prevSplitLocation.getKey() && headBackToPrevSplitLocation) {
            if (!prevLocations.empty()) {
                MapLocation prevLocation = prevLocations.pop();
//                if (prevLocation.equals(rc.getLocation()) && !prevLocations.empty()) {
//                    prevLocation = prevLocations.pop();
//                }
                System.out.println("Backtracking to " + prevLocation);
                tryMove(rc.getLocation().directionTo(prevLocation));
            }
//            else if (enemyHqLoc == null){ //this method needs more testing
//                enemyPotentialHQNumber++;
//            }
        }

        //first two conditions are to make sure you don't walk back to previous location when discovering, that
        //sometimes causes unit to just move back and forth.
        if (!prevLocations.empty() && !targetDirection.equals(rc.getLocation().directionTo(prevLocations.peek()))
                && tryMove(targetDirection)) {
            prevLocations.push(rc.getLocation());
            if (split) {
                split = false;
                discoverDir = "right";
            }
        } else {
            System.out.println("Couldn't move towards target direction " + split);
            if (!split) {
                System.out.println("SPLIT - push" + rc.getLocation());
                prevSplitLocations.push(new Pair(rc.getLocation(), discoverDir));
                split = true;
            }
            Direction[] dirs = null;
            if (discoverDir.equals("right")) {
                dirs = new Direction[]{targetDirection.rotateRight(), targetDirection.rotateRight().rotateRight(),
                        targetDirection.rotateRight().rotateRight().rotateRight()};
            } else if (discoverDir.equals("left")) {
                dirs = new Direction[]{targetDirection.rotateLeft(), targetDirection.rotateLeft().rotateLeft(),
                        targetDirection.rotateLeft().rotateLeft().rotateLeft()};
            }
            //make sure miner couldn't move when actually trying to move
            if (rc.getCooldownTurns() < 1) {
                boolean moved = false;
                MapLocation temp = rc.getLocation(); //add the loc before moving
                for (Direction dir : dirs) {
                    if (tryMove(dir)) {
                        moved = true;
                        prevLocations.push(temp);
                    }
                }
                if (!moved) {
                    System.out.println("Couldn't move " + discoverDir + " " + prevSplitLocations.size());
                    headBackToPrevSplitLocation = true;
                    if (!prevSplitLocations.empty()) {
                        prevSplitLocation = prevSplitLocations.peek();
                        if (discoverDir.equals("left")) {
                            System.out.println("pop " + prevSplitLocations.size());
                            prevSplitLocations.pop();
                        }
                    }
                }
            }
        }
        System.out.println("Bytecode 4" + Clock.getBytecodeNum());
        System.out.println("============================================");
    }

    public void getRefineryLocation() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == REFINERY_LOCATION) {
                    refineLocations.add(new MapLocation(mess[2], mess[3]));
                    System.out.println("GOT REFINERY LOCATION");
                }
            }
        }
    }

    public void depositSoupAtNearestRefineLocation() throws GameActionException {
        // time to deposit soup, go to the nearest soup refine location
        MapLocation closestRefineLoc = hqLoc;
        int closestRefineDistance = 1000;
        for (MapLocation refineLoc : refineLocations) {
            if (rc.getLocation().distanceSquaredTo(refineLoc) < closestRefineDistance) {
                closestRefineLoc = refineLoc;
            }
        }
        System.out.println("Deposit at " + closestRefineLoc);
        dfsWalk(closestRefineLoc);
    }

    public void buildRefineryAtSoupAreaFarFromHQ() throws GameActionException {
        if (rc.senseNearbySoup().length > 5) {
            //check if have refine spots nearby
            boolean hasNearby = false;
            for (MapLocation loc : refineLocations) {
                if (loc.distanceSquaredTo(rc.getLocation()) < 50) {
                    hasNearby = true;
                }
            }
            if (!hasNearby) {
                for (Direction dir : Util.directions) {
                    if (tryBuild(RobotType.REFINERY, dir)) {
                        broadcastNewRefinery();
                    }
                }
            }
        }
    }

    public void broadcastNewRefinery () {

    }
}
