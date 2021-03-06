package redemptionplayer;

import battlecode.common.*;

import java.util.*;

public class Miner extends Unit {

    static boolean firstMiner = false;
    static int stuckMoves = 0;
    static int designSchoolCount = 0; //only first miner cares about this for now
    static int currentElevation = 0;
    static boolean startAttacking = false;
    static boolean pauseForFlight = false;
    static boolean giveUpMinerRush = false;
    static ArrayList<MapLocation> refineLocations;
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

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        mapLocations = new HashMap<>();
        refineLocations = new ArrayList<>();
        soupLocations = new LinkedList<>();
        seenSoupLocs = new HashSet<>();
    }

    public void run() throws GameActionException {
        super.run();

        if (rc.getRoundNum() > 300 && rc.getLocation().isAdjacentTo(hqLoc) && nearbyTeamRobot(RobotType.LANDSCAPER)) {
            //blocking the turtle and there's probably no other soup locations discovered that's why it deposited
            //at HQ
            rc.disintegrate();
        }

        rushing = rc.getRoundNum() < 250 && !giveUpMinerRush;
        setBuildPriority();

        currentElevation = rc.senseElevation(rc.getLocation());

        if (!backupMiner && turnCount == 1 && rc.getRoundNum() >= backupRound) {
            backupMiner = true;
        } else if (turnCount == 1 && rc.getRoundNum() == 2) {
            System.out.println("IM FIRST MINER");
            //Sets the first spawned miner to the first miner (that will be discovring enemy HQ)
            firstMiner = true;
            usedToBeFirstMiner = true;
//            refineLocations.add(hqLoc);
        }

        System.out.println("Bytecode 1" + Clock.getBytecodeNum());
        System.out.println("Bytecode 2" + Clock.getBytecodeNum());


        if (firstMiner) {
            if (rc.getRoundNum() > 185 && !startAttacking) {
                giveUpMinerRush = true;
            }

//            //If 10 turns later still around last recent position, give up miner rush
//            if (rc.getRoundNum() > 20 && turnCount % 20 == 0) {
//                if (recentPosition != null && rc.getLocation().isWithinDistanceSquared(recentPosition, 20)) {
//                    giveUpMinerRush = true;
//                    System.out.println("Stuck");
//                }
//                recentPosition = rc.getLocation();
//            }
            if (enemyHqLoc != null && !giveUpMinerRush) {
                System.out.println("rushing");
                //if miner is the first miner and enemy HQ is found, keep broadcasting
                //enemy HQ to new units and (build design schools nearby enemy HQ) -> this behavior should be optimized
//                if (turnCount % 10 == 0) {
//                    //Temporary way to stop broadcasting every turn when miner is around enemy HQ, because it uses too much soup.
//                    broadcastRealEnemyHQCoordinates();
//                }
                if (rushDesignSchoolLocation != null) {
                    if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
                        for (Direction dir : Util.directions) {
                            if (rc.getTeamSoup() > 254 && tryBuild(RobotType.NET_GUN, dir)) {
                                if (!broadcastedCont && broadcastedHalt) {
                                    broadcastContinueProduction();
                                    broadcastedCont = true;
                                }
                            } else {
                                if (!broadcastedHalt) {
                                    broadcastHaltProduction();
                                    broadcastedHalt = true;
                                }
                            }
                        }
                    }
                    MapLocation waitingPosition = enemyHqLoc.add(enemyHqLoc.directionTo(rushDesignSchoolLocation).opposite());
                    if (!rc.getLocation().equals(waitingPosition)) {
                        dfsWalk(waitingPosition);
                    }
                } else if (rc.getLocation().isAdjacentTo(new MapLocation(enemyHqLoc.x - 1, enemyHqLoc.y))) {
                    System.out.println("Start Attack");
                    //create design school next to enemy HQ
                    startAttacking = true;
                    //build net gun if there's enemy delievery drones nearby
                    if (designSchoolCount < 1) {
                        if (rc.getTeamSoup() > 154 &&
                                tryBuild(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(new MapLocation(enemyHqLoc.x - 1, enemyHqLoc.y)))) {
                            designSchoolCount++;
                            rushDesignSchoolLocation = rc.getLocation().add(rc.getLocation().directionTo(new MapLocation(enemyHqLoc.x - 1, enemyHqLoc.y)));
                        } else {
                            for (Direction dir : Util.directions) {
                                if (rc.getLocation().add(dir).isAdjacentTo(enemyHqLoc) &&
                                        rc.getTeamSoup() > 154 && tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                                    designSchoolCount++;
                                    rushDesignSchoolLocation = rc.getLocation().add(dir);
                                    break;
                                }
                            }
                        }
                    }
                    if (designSchoolCount == 0) {
                        dfsWalk(new MapLocation(targetEnemyX - 1, targetEnemyY));
                    }
                } else {
                    //move towards enemy HQ to make sure your not divided by water
                    dfsWalk(new MapLocation(targetEnemyX - 1, targetEnemyY));
                }
            } else if (giveUpMinerRush) {
                System.out.println("GUMR------------------!");
                if (!broadCastedGiveUpMinerRush) {
                    broadcastGiveUpMinerRush();
                } else {
                    firstMiner = false;
                }
                //Make fulfillment center
            } else {
                //If enemy HQ is not found yet and is within miner's sensor radius, broadcast enemy HQ position
                System.out.println("targeting coordinates " + targetEnemyX + " " + targetEnemyY);
                //Checks if the enemyHQ is within the current robots sensor radius
                if (rc.getLocation().isWithinDistanceSquared(new MapLocation(targetEnemyX, targetEnemyY), rc.getCurrentSensorRadiusSquared())) {
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
                    System.out.println("Moving to " + targetEnemyX + " " + targetEnemyY);
                    discoverEnemyHQ(new MapLocation(targetEnemyX, targetEnemyY));
                }
            }
        } else if (backupMiner) {
            System.out.println("Backup miner " + (!checkGiveUpRush && !giveUpMinerRush));
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
            }
            getHaltProductionFromBlockchain();
            getContinueProductionFromBlockchain();

            if (checkHalt()) {
                return;
            }
            System.out.println("Build! " + buildPriority);

            if (!nearbyTeamRobot(RobotType.VAPORATOR)) {
                for (Direction dir : Util.directions) {
                    if (!hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir),
                            4) && tryBuild(RobotType.VAPORATOR, dir)) {
                        System.out.println("created a vaporator next to HQ");
                    }
                }
            }

            if (rc.getTeamSoup() > 150 + buildPriority && !nearbyTeamRobot(RobotType.LANDSCAPER) && !nearbyTeamRobot(RobotType.DESIGN_SCHOOL)) {
                System.out.println("Try build design");
                for (Direction dir : Util.directions) {
                    if (tryBuild(RobotType.DESIGN_SCHOOL, dir)) {
                        System.out.println("created a design school next to HQ");
                        designSchoolCount++;
                    }
                }
            }

            if (designSchoolCount > 0) {
                //Build fulfillment center next to hq
                if (rc.getTeamSoup() > 150 + buildPriority && fulfillmentCenterCount == 0) {
                    System.out.println("Try build fulfillment center");
                    for (Direction dir : Util.directions) {
                        System.out.println(rc.getLocation().add(dir) + " " + !hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 4));
                        if (!hqLoc.isWithinDistanceSquared(rc.getLocation().add(dir), 4)
                                && tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                            System.out.println("created a fulfillment next to HQ");
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
                System.out.println(landscaperCount);
                if (landscaperCount < 4) {
                    return;
                }
            }
            System.out.println("TESt");
            moveAroundHQ();
        } else {
            System.out.println("Not first miner or backup miner");
            if (rc.getRoundNum() > 30 && !giveUpMinerRush) {
                getGiveUpMinerRush(5);
            }

            getSoupLocation();
            getRefineryLocation();

            for (Direction dir : Util.directions) {
                if (tryRefine(dir)) {
                    System.out.println("I refined soup! " + rc.getTeamSoup());
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

            if (rc.getRoundNum() > 10 && adjacentSoup) {
                getRefineryLocation();
                buildRefineryNearSoupArea();
            }

            int whenToStopMiningSoup = RobotType.MINER.soupLimit; //used to be RobotType.MINER.soupLimit;
            if (rc.getSoupCarrying() >= whenToStopMiningSoup) {
                depositSoupAtNearestRefineLocation();
            } else {
                System.out.println("Before trying to find soup");
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

                if (checkIfSoupLocIsNew()) {
                    broadcastSoupNewSoupLoc(rc.getLocation().x, rc.getLocation().y);
                }

                if (soupLocation == null) {
                    soupLocation = closestSoup;
                }

                if (soupToMine.length == 0 || closestSoup == null) {
                    //move to last soup location
                    if (soupLocation != null) {
                        dfsWalk(soupLocation);
                        //if no soup is nearby soup location, set it to null to trigger random movement again
                        if (rc.getLocation().isWithinDistanceSquared(soupLocation, 8)) {
                            soupLocation = null;
                        }
                    } else {
                        // If no soup is nearby, search for soup by moving randomly
                        System.out.println("No soup nearby");
                        System.out.println(randomDirection + " " + randomDirectionCount);

                        if (randomDirection == null) {
                            randomDirection = Util.randomDirection();
                        }
                        if (randomDirectionCount-- > 0 && goTo(randomDirection)) {
                                System.out.println("I moved randomly!");
                        } else {
                            randomDirectionCount = 10;
                            randomDirection = randomDirection.rotateLeft();
                        }
                    }
                } else {
                    if (Math.abs(closestSoup.x - rc.getLocation().x) < 2 &&
                            Math.abs(closestSoup.y - rc.getLocation().y) < 2) {
                        System.out.println("Soup nearby and within mine distance");
                        // Else if soup is adjacent, mine it
                        for (Direction dir : Util.directions) {
                            if (tryMine(dir)) {
                                System.out.println("I mined soup! " + rc.getSoupCarrying());
                                clearMovement();
                                soupLocation = rc.getLocation();
                                break;
                            }
                        }
                    } else {
                        System.out.println("Moving towards soup to mine " + closestSoup);
                        // Otherwise, travel towards the detected soup
                        dfsWalk(closestSoup);
                    }
                }

            }
        }
        System.out.println("Bytecode end" + Clock.getBytecodeNum());
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
        Direction targetDirection = rc.getLocation().directionTo(enemyHQ);
        System.out.println("============================================");
        if (rc.getCooldownTurns() < 1) {
            if (rc.getCooldownTurns() < 1 && prevSplitLocation != null && rc.getLocation().equals(prevSplitLocation.getKey())) {
                System.out.println("At previous split loc " + prevSplitLocation.getKey() + " " + discoverDir);
                if (discoverDir.equals("left")) {
                    System.out.println("Switched to right");
                    discoverDir = "right";
                    headBackToPrevSplitLocation = false;
                } else if (discoverDir.equals("right")) {
                    headBackToPrevSplitLocation = true;
                    if (!prevSplitLocations.empty()) {
                        prevSplitLocation = prevSplitLocations.pop();
                        discoverDir = "left";
                    }
                    if (prevSplitLocations.empty()) {
                        //broadcast
                        discoverDir = "right"; //to get back into this condition.
                        System.out.println("Stuck, switching to drones");
                        giveUpMinerRush = true;
                        return;
                    }
                }
            } else if (prevSplitLocation != null && rc.getLocation() != prevSplitLocation.getKey() && headBackToPrevSplitLocation) {
                goTo(prevSplitLocation.getKey());
            }

            //to make sure you don't walk back to previous location when discovering, that
            //sometimes causes unit to just move back and forth.
            boolean walkingPrevDirection = targetDirection == rc.getLocation().directionTo(prevLocation);
            prevLocation = rc.getLocation();
            if (!walkingPrevDirection && tryMove(targetDirection)) {
                System.out.println("Moved in target Direction " + targetDirection);
                if (split) {
                    split = false;
                    discoverDir = "left";
                }
            } else if (rc.getCooldownTurns() < 1) {
                System.out.println("Couldn't move towards target direction " + split + " " + discoverDir);
                if (!split) {
                    System.out.println("SPLIT - push" + rc.getLocation());
                    prevSplitLocations.push(new Pair(rc.getLocation(), discoverDir));
                    split = true;
                }
                Direction[] dirs = null;
                if (discoverDir.equals("right")) {
                    dirs = new Direction[]{targetDirection.rotateRight(), targetDirection.rotateRight().rotateRight(),
                            targetDirection.rotateRight().rotateRight().rotateRight(), targetDirection.opposite()};
                } else if (discoverDir.equals("left")) {
                    dirs = new Direction[]{targetDirection.rotateLeft(), targetDirection.rotateLeft().rotateLeft(),
                            targetDirection.rotateLeft().rotateLeft().rotateLeft(), targetDirection.opposite()};
                }
                //make sure miner couldn't move when actually trying to move
                if (rc.getCooldownTurns() < 1) {
                    boolean moved = false;
                    for (Direction dir : dirs) {
                        System.out.println(dir);
                        if (tryMove(dir)) {
                            moved = true;
                        }
                    }
                    if (!moved) {
                        System.out.println("Couldn't move " + discoverDir + " " + prevSplitLocations.size());
                        if (!prevSplitLocations.empty()) {
                            prevSplitLocation = prevSplitLocations.peek();
                            if (discoverDir.equals("right") && headBackToPrevSplitLocation) {
                                System.out.println("pop " + prevSplitLocations.size());
                                prevSplitLocations.pop();
//                            clearMovement();
                            }
                        }
                        headBackToPrevSplitLocation = true;
                    }
                }
            }
        }
        System.out.println("Bytecode 4" + Clock.getBytecodeNum());
        System.out.println("============================================");
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
            }
        }
        System.out.println("Deposit at " + closestRefineLoc);
        dfsWalk(closestRefineLoc);
    }

    public void buildRefineryNearSoupArea() throws GameActionException {
        System.out.println("Build refinery " + buildPriority);

        if (checkHalt()) {
            return;
        }

        if (nearbyTeamRobot(RobotType.REFINERY)) {
            return;
        }
        System.out.println("REFINERY " + buildPriority);
        //if finds soup area and no refinery nearby, build refinery
        if (rc.getTeamSoup() > 206) {
            //check if have refine spots nearby
            boolean hasNearby = false;
            for (MapLocation loc : refineLocations) {
                System.out.println(loc);
                if (loc.distanceSquaredTo(rc.getLocation()) < 120) {
                    hasNearby = true;
                }
            }
            if (!hasNearby && hqLoc.distanceSquaredTo(rc.getLocation()) > 9) {
                for (Direction dir : Util.directions) {
                    if (!rc.getLocation().add(dir).isAdjacentTo(hqLoc) && tryBuild(RobotType.REFINERY, dir)) {
                        MapLocation refineryLoc = rc.getLocation().add(dir);
                        broadcastNewRefinery(refineryLoc.x, refineryLoc.y);
                        refineLocations.add(refineryLoc);
                        if (!broadcastedCont && broadcastedHalt) {
                            broadcastContinueProduction();
                            broadcastedCont = true;
                        }
                        break;
                    }
                }
            }
        } else if (rc.getTeamSoup() > 3){
            if (!broadcastedHalt) {
                broadcastHaltProduction();
                broadcastedHalt = true;
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
    }

    public void broadcastSoupNewSoupLoc(int x, int y) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = SOUP_LOCATION;
        message[2] = x; // possible x coord of enemy HQ
        message[3] = y; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 2))
            rc.submitTransaction(message, 2);
    }

    boolean checkIfSoupLocIsNew() {
        MapLocation currentLocation = rc.getLocation();
        for (MapLocation loc : soupLocations) {
            if (loc.isWithinDistanceSquared(currentLocation, 50)) {
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
                    System.out.println("GOT SOUP LOC");
                }
            }
        }
    }

    public void setBuildPriority() {
        int roundNum = rc.getRoundNum();
        System.out.println("RUSHING " + rushing);
        if (!rushing) {
            //reserve soup for refinery if no refineries built.
            buildPriority = refineLocations.isEmpty() && backupMiner ? 200 : 0;
            return;
        }
        if (roundNum < 185) {
            buildPriority = 1000; //technically we don't want to waste resources when rushing;
        } else if (roundNum < 250) {
            buildPriority = 150; //prioritize building miners and design schools during rush period.
        } else {
            buildPriority = 0;
        }
    }

    public void moveAroundHQ() throws GameActionException {

        List<Direction> list = Arrays.asList(Util.directions);

        Collections.shuffle(list);

        //move around HQ in random directions
        for (Direction dir : list) {
            if (rc.getLocation().add(dir).isWithinDistanceSquared(hqLoc, 16)
                    && !rc.getLocation().add(dir).isWithinDistanceSquared(hqLoc, 4)
                    && !rc.getLocation().add(dir).equals(prevLocation) && tryMove(dir)) {
                break;
            }
        }

        prevLocation = rc.getLocation();

        if (rc.getCooldownTurns() < 1) {
            //just move in a random direction - to prevent the bug happening in maps like hourgalss
            for (Direction dir : Util.directions) {
                tryMove(dir);
            }
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