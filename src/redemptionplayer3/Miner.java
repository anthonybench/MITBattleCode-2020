package redemptionplayer3;

import battlecode.common.*;

import java.util.*;

public class Miner extends Unit {

    static boolean firstMiner = false;
    static int stuckMoves = 0;
    static int designSchoolCount = 0; //only first miner cares about this for now
    static int currentElevation = 0;
    static boolean startAttacking = false;
    static boolean pauseForFlight = false;
    static Set<MapLocation> refineLocations;
    static Queue<MapLocation> soupLocations;
    static MapLocation soupLocation;
    static Set<MapLocation> seenSoupLocs;
    static boolean backupMiner = false;
    static MapLocation rushDesignSchoolLocation;
    static int buildPriority = 0;
    static boolean rushing;
    static boolean broadCastedGiveUpMinerRush = false;
    static int fulfillmentCenterCount = 0;
    static boolean checkGiveUpRush = false;
    static String moveAroundHQDir = "left";
    static MapLocation recentPosition;
    static String discoverDir = "left"; // prioritizes discovering to the right;
    static boolean usedToBeFirstMiner = false;
    static boolean builtVaporator = false;
    static boolean usedToBeBackUp = false;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        mapLocations = new HashMap<>();
        refineLocations = new TreeSet<>();
        soupLocations = new LinkedList<>();
        seenSoupLocs = new HashSet<>();
        NavHelper s = new NavHelper();
        MapLocation temp = s.abc(hqLoc, rc.getMapWidth(), rc.getMapHeight());
        if (temp == null) {
            giveUpTurn = 50;
        }
        hugDirection = s.cba(hqLoc);
    }

    public void run() throws GameActionException {
        super.run();

        if (rc.getRoundNum() > 300 && rc.getLocation().isAdjacentTo(hqLoc) && nearbyTeamRobot(RobotType.LANDSCAPER)) {
            //blocking the turtle and there's probably no other soup locations discovered that's why it deposited
            //at HQ
            rc.disintegrate();
        }

        if (turnCount == 1) {
            refineLocations.add(hqLoc);
        }
        rushing = rc.getRoundNum() < 250 && !giveUpMinerRush;
        setBuildPriority();

        currentElevation = rc.senseElevation(rc.getLocation());

        if (!backupMiner && turnCount == 1 && rc.getRoundNum() >= backupRound) {
            backupMiner = true;
        } else if (turnCount == 1 && rc.getRoundNum() == 2) {
            //Sets the first spawned miner to the first miner (that will be discovring enemy HQ)
            firstMiner = true;
            usedToBeFirstMiner = true;
        }


        if (firstMiner) {
            if (rc.getRoundNum() > giveUpTurn && !startAttacking) {
                System.out.println("TEst " + giveUpTurn);
                giveUpMinerRush = true;
            }

            if (enemyHqLoc != null && !giveUpMinerRush) {
                //if miner is the first miner and enemy HQ is found, keep broadcasting
                //enemy HQ to new units and (build design schools nearby enemy HQ) -> this behavior should be optimized
                if (rushDesignSchoolLocation != null) {
                    if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
                        for (Direction dir : Util.directions) {
                            if (rc.getTeamSoup() > 254 && tryBuild(RobotType.NET_GUN, dir)) {
                                if (!broadcastedCont && broadcastedHalt) {
                                    broadcastContinueProduction();
                                    broadcastedCont = true;
                                    broadcastedHalt = false;
                                }
                            } else {
                                if (!broadcastedHalt) {
                                    broadcastHaltProduction();
                                    broadcastedHalt = true;
                                    broadcastedCont = false;
                                }
                            }
                        }
                    }
//                    MapLocation waitingPosition = enemyHqLoc.add(enemyHqLoc.directionTo(rushDesignSchoolLocation).opposite());
//                    if (!rc.getLocation().equals(waitingPosition)) {
//                        dfsWalk(waitingPosition);
//                    }
                    if (rc.getLocation().isAdjacentTo(enemyHqLoc)) {
                        for (Direction dir : Util.directions) {
                            if (rc.canMove(dir) && !rc.getLocation().add(dir).isAdjacentTo(enemyHqLoc) && tryMove(dir)) {
                                return;
                            }
                        }
                    }
                } else if (rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 8)) {
                    //create design school next to enemy HQ
                    startAttacking = true;
                    //build net gun if there's enemy delievery drones nearby
                    if (designSchoolCount < 1) {
                        for (Direction dir : Util.directions) {
                            if (rc.getLocation().add(dir).isWithinDistanceSquared(enemyHqLoc, 1) &&
                                    tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                                designSchoolCount++;
                                rushDesignSchoolLocation = rc.getLocation().add(dir);
                                return;
                            }
                        }

                        for (Direction dir : Util.directions) {
                            if (rc.getLocation().add(dir).isAdjacentTo(enemyHqLoc) &&
                                    tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                                designSchoolCount++;
                                rushDesignSchoolLocation = rc.getLocation().add(dir);
                                break;
                            }
                        }
                    }
                    if (designSchoolCount == 0) {
                        if (!rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 2)){
                            navigation(enemyHqLoc);
                        }
                    }
                } else {
                    //move towards enemy HQ to make sure your not divided by water
                    if (!rc.getLocation().isWithinDistanceSquared(enemyHqLoc, 2)){
                        navigation(enemyHqLoc);
                    }
                }
            } else if (giveUpMinerRush) {
                if (!broadCastedGiveUpMinerRush) {
                    broadcastGiveUpMinerRush();
                } else {
                    firstMiner = false;
                }
                //Make fulfillment center
            } else {
                //If enemy HQ is not found yet and is within miner's sensor radius, broadcast enemy HQ position
                //Checks if the enemyHQ is within the current robots sensor radius
                if (rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY), rc.getCurrentSensorRadiusSquared())) {
                    if (nearbyEnemyRobot(RobotType.HQ)) {
                        nearbyEnemyHQLocation(); //Special case is map spiral, two potential locations can be sensed together.
                        broadcastRealEnemyHQCoordinates();
                        enemyHqLoc = new MapLocation(targetEnemyX, targetEnemyY);
                    } else {
                        //if potential enemy HQ location is within sensor radius but enemy HQ is not found,
                        //switch to move to next potential location
                        enemyPotentialHQNumber++;
                    }
                }

                if (enemyHqLoc == null) {
                    enemyBaseFindingLogic();
                }
//                //Broadcast soup locations found while discovering enemy HQ
//                MapLocation[] soupToMine = rc.senseNearbySoup();
//                if (soupToMine.length > 3 && (lastNewSoupsLocation == null
//                        || !rc.getLocation().isWithinDistanceSquared(lastNewSoupsLocation, 50))) {
//                    broadcastSoupNewSoupLoc(soupToMine[0].x, soupToMine[0].y);
//                }
                //Hills
                if (rc.getRoundNum() < 16) {
                    minerGoToEnemyHQ();
                } else {
                    navigation(new MapLocation(targetEnemyX, targetEnemyY));
                }
            }
        } else if (backupMiner) {
            if (designSchoolCount > 0 && fulfillmentCenterCount > 0) {
                backupMiner = false;
                usedToBeBackUp = true;
            }
            if (!checkGiveUpRush && !giveUpMinerRush) {
                getGiveUpMinerRush(rc.getRoundNum() - 5);
                checkGiveUpRush = true;
            }

            if (rc.getRoundNum() > 30 && !giveUpMinerRush) {
                getGiveUpMinerRush(5);
            }

            if (refineLocations.isEmpty()) {
                getRefineryLocation();
            }

            if (rc.getLocation().isAdjacentTo(hqLoc)) {
                Direction temp = rc.getLocation().directionTo(hqLoc);
                Direction[] dirs = {temp.opposite().rotateLeft(), temp.opposite().rotateRight(), temp.opposite()};
                for (Direction dir : dirs) {
                    if (tryMove(dir)) {
                        break;
                    }
                }
                if (rc.getCooldownTurns() < 1) {
                    for (Direction dir : Util.directions) {
                        if (tryMove(dir)) {
                            break;
                        }
                    }
                }
            }
            getHaltProductionFromBlockchain();
            getContinueProductionFromBlockchain();

            if (checkHalt()) {
                return;
            }

            if (giveUpTurn == 50 && !builtVaporator) {
                for (Direction dir : Util.directions) {
                    if (!hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir),
                            4) && tryBuild(RobotType.VAPORATOR, dir)) {
                        System.out.println("created a vaporator next to HQ");
                        builtVaporator = true;
                    }
                }
                return;
            }

            if (rc.getTeamSoup() > 150 + buildPriority && !nearbyTeamRobot(RobotType.LANDSCAPER) && !nearbyTeamRobot(RobotType.DESIGN_SCHOOL)) {
                for (Direction dir : Util.directions) {
                    if (tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                        designSchoolCount++;
                    }
                }
            }

            if (designSchoolCount > 0) {
                //Build fulfillment center next to hq
                if (rc.getTeamSoup() > 150 + buildPriority && fulfillmentCenterCount == 0) {
                    for (Direction dir : Util.directions) {
                        if (!hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 4)
                                && tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                            fulfillmentCenterCount++;
                            return;
                        }
                    }
                }

                //if 4 landscapers are built
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
                int landscaperCount = 0;
                for (RobotInfo robot : nearbyRobots) {
                    if (robot.getType() == RobotType.LANDSCAPER && robot.getTeam() == rc.getTeam()) {
                        landscaperCount++;
                        if (landscaperCount >= 4) {
                            break;
                        }
                    }
                }
                if (landscaperCount < 4) {
                    return;
                }
//                if (netGuns == 0) {
//                    MapLocation topRight = new MapLocation(hqLoc.x + 2, hqLoc.y + 2);
//                    MapLocation topLeft = new MapLocation(hqLoc.x - 2, hqLoc.y + 2);
//                    MapLocation bottomLeft = new MapLocation(hqLoc.x - 2, hqLoc.y - 2);
//                    MapLocation bottomRight = new MapLocation(hqLoc.x + 2, hqLoc.y - 2);
//                    MapLocation[] locs = {topLeft, topRight, bottomLeft, bottomRight};
//                    MapLocation closestPosition = null;
//                    int closestDistance = 1000;
//                    for (MapLocation loc : locs) {
//                        if (rc.getLocation().distanceSquaredTo(loc) < closestDistance) {
//                            closestDistance = rc.getLocation().distanceSquaredTo(loc);
//                            closestPosition = loc;
//                        }
//                    }
//                    if (rc.getLocation().isAdjacentTo(closestPosition)) {
//                        for (Direction dir : Util.directions) {
//                            if (!hqLoc.isAdjacentTo(rc.getLocation().add(dir)) && tryBuild(RobotType.NET_GUN, dir)) {
//                                System.out.println("created a net gun next to HQ");
//                                netGuns++;
//                                return;
//                            }
//                        }
//                    } else {
//                        dfsWalk(closestPosition);
//                    }
//                }
            }
            moveAroundHQ();
        } else {
            if (rc.getTeamSoup() > 600 && rc.getRoundNum() > 400) {
                for (Direction dir : Util.directions) {
                    if (!hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 4)
                            && tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                        fulfillmentCenterCount++;
                        return;
                    }
                }
            }
            if (rc.getRoundNum() > 30 && !giveUpMinerRush) {
                getGiveUpMinerRush(5);
            }

            if (refineLocations.contains(hqLoc)) {
                int depositSpots = 0;
                MapLocation hqAdjacents[] = {hqLoc.translate(0, 1), hqLoc.translate(0, -1),
                        hqLoc.translate(1, 1), hqLoc.translate(-1, -1), hqLoc.translate(-1, 1),
                        hqLoc.translate(1, -1), hqLoc.translate(1, 0), hqLoc.translate(-1, 0)};
                for (MapLocation loc : hqAdjacents) {
                    if (rc.canSenseLocation(loc) && rc.senseElevation(loc) < 5) {
                        depositSpots++;
                    }
                }
                if (depositSpots < 3) {
                    refineLocations.remove(hqLoc);
                }
            }
            if (rc.getRoundNum() > 10) {
                getSoupLocation();
                getRefineryLocation();
            }

            for (Direction dir : Util.directions) {
                if (tryRefine(dir)) {
                    clearMovement();
                    return;
                }
            }

            MapLocation[] soupToMine = rc.senseNearbySoup();
            //Build refinery if adjacent to soup
            boolean adjacentSoup = false;
            for (MapLocation soup : soupToMine) {
                if (soup.isAdjacentTo(rc.getLocation())) {
                    adjacentSoup = true;
                }
            }

            if (rc.getRoundNum() > 10 && adjacentSoup && !usedToBeBackUp) {
                getRefineryLocation();
                buildRefineryNearSoupArea();
            }

            int whenToStopMiningSoup = RobotType.MINER.soupLimit; //used to be RobotType.MINER.soupLimit;
            if (rc.getSoupCarrying() >= whenToStopMiningSoup) {
                depositSoupAtNearestRefineLocation();
            } else {
                getHaltProductionFromBlockchain();
                getContinueProductionFromBlockchain();

                // Try to find soup
                MapLocation closestSoup = null;
                int shortestDistance = 100;
                //mine the closest soup to avoid bug that occurs on map like "spiral"
                for (MapLocation soupLoc : soupToMine) {
                    int distanceToSoup = soupLoc.distanceSquaredTo(rc.getLocation());
                    if (distanceToSoup < shortestDistance) {
                        shortestDistance = distanceToSoup;
                        closestSoup = soupLoc;
                    }
                }

                if (soupLocation == null) {
                    soupLocation = closestSoup;
                }

                System.out.println(soupLocation);
                if (soupToMine.length == 0 || closestSoup == null) {
                    // If no soup is nearby, go to previous soup location.
                    // If still no soup nearby, check if there's stored soup locations, if none,
                    // search for soup by moving randomly
                    if (soupLocation == null && !soupLocations.isEmpty()) {
                        soupLocation = soupLocations.remove();
                    }

                    //move to last soup location
                    if (soupLocation != null) {
                        System.out.println("Walt to" + soupLocation);
                        navigation(soupLocation);
                        //if no soup is nearby soup location, set it to null to trigger random movement again
                        if (rc.getLocation().isWithinDistanceSquared(soupLocation, 8)) {
                            soupLocation = null;
                        }
                    } else {
                        if (turnCount < 30) {
                            goTo(Util.randomDirection());
                        } else {
                            if (randomDirection == null) {
                                randomDirection = Util.randomDirection();
                            }
                            if (!(randomDirectionCount-- > 0 && goTo(randomDirection))) {
                                randomDirectionCount = 10;
                                randomDirection = randomDirection.rotateLeft();
                            }
                        }
                    }
                } else {

                    if (Math.abs(closestSoup.x - rc.getLocation().x) < 2 &&
                            Math.abs(closestSoup.y - rc.getLocation().y) < 2) {
                        // Else if soup is adjacent, mine it
                        for (Direction dir : Util.directions) {
                            if (tryMine(dir)) {
                                clearMovement();
                                soupLocation = rc.getLocation();
                                if (checkIfSoupLocIsNew()) {
                                    broadcastSoupNewSoupLoc(rc.getLocation().x, rc.getLocation().y);
                                }
                                break;
                            }
                        }
                    } else {
                        // Otherwise, travel towards the detected soup
                        dfsWalk(closestSoup);
                    }
                }

            }
        }
        System.out.println("Bytecode end" + Clock.getBytecodeNum() + " " + Clock.getBytecodesLeft());
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

    void broadcastGiveUpMinerRush() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = GIVE_UP_MINER_RUSH;
        if (rc.canSubmitTransaction(message, 3)) {
            rc.submitTransaction(message, 3);
            broadCastedGiveUpMinerRush = true;
        }
        System.out.println("GUMR");
    }

    public void getGiveUpMinerRush(int rounds) throws GameActionException {
        System.out.println(rc.getRoundNum() - rounds);
        for (int i = rc.getRoundNum() - rounds; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == GIVE_UP_MINER_RUSH) {
                    rushing = false;
                    giveUpMinerRush = true;
                    System.out.println("GOT GUMR");
                }
            }
        }
    }

    void discoverEnemyHQ(MapLocation enemyHQ) throws GameActionException {
        //hugDirection 0 for left, 1 for right;
        Direction targetDirection = rc.getLocation().directionTo(enemyHQ);
        //return if miner is not suppose to be able to move
        System.out.println(targetDirection + " " + prevDirection);
//        System.out.println(hugDirection);
        if (rc.getCooldownTurns() >= 1) {
            return;
        }
        if (targetDirection != prevDirection && rc.canMove(targetDirection) && tryMove(targetDirection)) {
            //moved in target direction
            //reset hug logic
//            if (hugDirection == 1) {
//                hugDirection = 0;
//                prevDirection = null;
//            }
            prevDirection = targetDirection.opposite();
        } else {
            //hug direction logic
            ArrayList<Direction> dirs = new ArrayList<>();

            //check if couldn't move because it's a unit, if is, try move around it.
            if (rc.senseRobotAtLocation(rc.getLocation().add(targetDirection)) != null) {
                if (tryMove(targetDirection.rotateLeft()) || tryMove(targetDirection.rotateRight())) {
                    return;
                }
            }
            System.out.println(hugDirection);
            if (hugDirection == 0) {
                dirs.add(targetDirection.rotateLeft());
                dirs.add(targetDirection.rotateLeft().rotateLeft());
                dirs.add(targetDirection.rotateLeft().rotateLeft().rotateLeft());
                dirs.add(targetDirection.rotateLeft().rotateLeft().rotateLeft().rotateLeft());
            } else {
                dirs.add(targetDirection.rotateRight());
                dirs.add(targetDirection.rotateRight().rotateRight());
                dirs.add(targetDirection.rotateRight().rotateRight().rotateRight());
                dirs.add(targetDirection.rotateRight().rotateRight().rotateRight().rotateRight());
            }

            //try move following the hug directions
            boolean moved = false;
            for (Direction dir : dirs) {
                if (tryMove(dir)) {
                    moved = true;
                    prevDirection = dir.opposite();
                    break;
                }
            }
            //if moved, just return
            if (moved) {
                return;
            }
            //if couldn't move towards the hug direction
            if (hugDirection == 0) {
                //now try move right
                hugDirection = 1;
            } else {
//                //we'll claim to be stuck, and then give up miner rush
//                System.out.println("-----------------STUCK------------------------------");
//                giveUpMinerRush = true;
                //now try move right
                hugDirection = 0;
            }
        }
    }

    public void getRefineryLocation() throws GameActionException {
        for (int i = rc.getRoundNum() - 5; i < rc.getRoundNum(); i++) {
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
                closestRefineDistance = rc.getLocation().distanceSquaredTo(refineLoc);
            }
        }
        dfsWalk(closestRefineLoc);
    }

    public void buildRefineryNearSoupArea() throws GameActionException {
        if (nearbyTeamRobot(RobotType.REFINERY) || rc.getRoundNum() < giveUpTurn) {
            return;
        }
        System.out.println("BUILD");
        //if finds soup area and no refinery nearby, build refinery
        //check if have refine spots nearby
        boolean hasNearby = false;
        for (MapLocation loc : refineLocations) {
            if (loc != hqLoc && loc.distanceSquaredTo(rc.getLocation()) < 120) {
                hasNearby = true;
            }
        }

        if (!hasNearby && hqLoc.distanceSquaredTo(rc.getLocation()) > 9) {
            if (rc.getTeamSoup() > 206 + buildPriority) {
                for (Direction dir : Util.directions) {
                    if (!rc.getLocation().add(dir).isAdjacentTo(hqLoc) && tryBuild(RobotType.REFINERY, dir)) {
                        MapLocation refineryLoc = rc.getLocation().add(dir);
                        broadcastNewRefinery(refineryLoc.x, refineryLoc.y);
                        refineLocations.add(refineryLoc);
                        System.out.println(haltProduction + " " + broadcastedCont);
                        if (haltProduction || broadcastedHalt) {
                            System.out.println("TEST---------------------------------------");
                            broadcastContinueProduction();
                            broadcastedCont = true;
                            broadcastedHalt = false;
                        }
                        break;
                    }
                }
            } else if (rc.getTeamSoup() > 3) {
                if (!broadcastedHalt) {
                    broadcastHaltProduction();
                    broadcastedHalt = true;
                    broadcastedCont = false;
                }
            }
        }
    }

    public void broadcastNewRefinery(int x, int y) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = REFINERY_LOCATION;
        message[2] = x; // possible x coord of enemy HQ
        message[3] = y; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
        System.out.println("Built New refinery");
    }

    public void broadcastSoupNewSoupLoc(int x, int y) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = SOUP_LOCATION;
        message[2] = x; // possible x coord of enemy HQ
        message[3] = y; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 2))
            rc.submitTransaction(message, 2);
        System.out.println("New Soup");
    }

    boolean checkIfSoupLocIsNew() {
        MapLocation currentLocation = rc.getLocation();
        for (MapLocation loc : soupLocations) {
            if (loc.isWithinDistanceSquared(currentLocation, 20)) {
                return false;
            }
        }
        return true;
    }

    public void getSoupLocation() throws GameActionException {
        for (int i = rc.getRoundNum() - 3; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == SOUP_LOCATION) {
                    soupLocations.add(new MapLocation(mess[2], mess[3]));
                    System.out.println("---------------- GOT SOUP LOC -----------------------");
                }
            }
        }
    }

    public void setBuildPriority() {
        int roundNum = rc.getRoundNum();

        if (!rushing) {
            //reserve soup for refinery if no refineries built.
            buildPriority = refineLocations.isEmpty() && backupMiner ? 200 : 0;
            return;
        }
        if (roundNum < giveUpTurn) {
            buildPriority = 1000; //technically we don't want to waste resources when rushing;
        } else if (roundNum < 250) {
            buildPriority = 150; //prioritize building miners and design schools during rush period.
        } else {
            buildPriority = 0;
        }
    }

    public void moveAroundHQ() throws GameActionException {
//        boolean moved = false;
//        Direction towardsHQ = rc.getLocation().directionTo(hqLoc);
//        ArrayList<Direction> dirs = new ArrayList<>();
//
//        //If see enemy drone, move towards HQ for protection.
//        if (nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
//            dfsWalk(hqLoc);
//        }
//
//        clearMovement();
//        //Set the general direction for the miner to move around HQ
//        if (moveAroundHQDir.equals("right")) {
//            dirs.add(towardsHQ.rotateLeft());
//            dirs.add(towardsHQ.rotateLeft().rotateLeft());
//            dirs.add(towardsHQ.rotateLeft().rotateLeft().rotateLeft());
//            dirs.add(towardsHQ.rotateLeft().rotateLeft().rotateLeft().rotateLeft());
//        } else {
//            dirs.add(towardsHQ.rotateRight());
//            dirs.add(towardsHQ.rotateRight().rotateRight());
//            dirs.add(towardsHQ.rotateRight().rotateRight().rotateRight());
//            dirs.add(towardsHQ.rotateRight().rotateRight().rotateRight().rotateRight());
//        }

        //move around HQ following a general direction - ideally miner would be moving in circles with the HQ as the center
//
//        List<Direction> list = Arrays.asList(Util.directions);
//
//        Collections.shuffle(list);
//
//        //move around HQ in random directions
//        for (Direction dir : list) {
//            if (rc.getLocation().add(dir).isWithinDistanceSquared(hqLoc, 16)
//                    && !rc.getLocation().add(dir).isWithinDistanceSquared(hqLoc, 4)
//                    && !rc.getLocation().add(dir).equals(prevLocation) && tryMove(dir)) {
////                moveAroundHQDir = moveAroundHQDir.equals("left") ? "right" : "left";
//                break;
//            }
//        }
//
//        prevLocation = rc.getLocation();
//
////        if (rc.getCooldownTurns() < 1) {
////            goTo(hqLoc);
////        }
//
//        if (rc.getCooldownTurns() < 1) {
//            //just move in a random direction - to prevent the bug happening in maps like hourgalss
//            for (Direction dir : Util.directions) {
//                tryMove(dir);
//            }
//        }
        if (rc.getCooldownTurns() > 1) {
            return;
        }

        if (randomDirection == null) {
            randomDirection = rc.getLocation().directionTo(hqLoc);
        }

        if (!(randomDirectionCount-- > 0 && rc.getLocation().add(randomDirection).isWithinDistanceSquared(hqLoc, 9) && tryMove(randomDirection))) {
            randomDirectionCount = 1;
            randomDirection = randomDirection.rotateLeft();
        }
    }

    @Override
    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir) && !rc.getLocation().add(dir).isWithinDistanceSquared(hqLoc, 4)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }
}