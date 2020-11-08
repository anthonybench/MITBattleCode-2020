package redemptionplayer;

import battlecode.common.*;

import java.util.Map;
import java.util.Stack;

public class Unit extends Robot {
    static int potentialEnemyHQX = -1;
    static int potentialEnemyHQY = -1;
    static int enemyPotentialHQNumber = 1;
    static int targetEnemyX = -100;
    static int targetEnemyY = -100;
    //might need to move variables below to miner, not sure yet.
    static Stack<Pair> prevSplitLocations;
    static String discoverDir = "right"; // prioritizes discovering to the right;
    static Stack<MapLocation> prevLocations;
    static boolean headBackToPrevSplitLocation = false;
    static Pair prevSplitLocation;
    static boolean split = false;
    Map<MapLocation, Integer> mapLocations;

    public class Pair {
        private MapLocation key;
        private String value;

        Pair(MapLocation mapLoc, String dir) {
            this.key = mapLoc;
            this.value = dir;
        }

        public MapLocation getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public Unit(RobotController rc) throws GameActionException {
        super(rc);
        findHQ();
        prevSplitLocations = new Stack<>();
        prevLocations = new Stack<>();
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir)) && !tileGoingToFlood(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }


    boolean tryMove() throws GameActionException {
        for (Direction dir : Util.directions)
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

    // tries to move in the general direction of dir
    boolean goTo(Direction dir) throws GameActionException {
        System.out.println("Goto");
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(), dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft()};
        for (Direction d : toTry) {
            if (tryMove(d))
                return true;
        }
        return false;
    }


    // navigate towards a particular location
    boolean goTo(MapLocation destination) throws GameActionException {
        return goTo(rc.getLocation().directionTo(destination));
    }

    void dfsWalk(MapLocation destination) throws GameActionException {
        Direction targetDirection = rc.getLocation().directionTo(destination);
        System.out.println("============================================");
        System.out.println("Bytecode 3 " + Clock.getBytecodeNum());
        System.out.println("!!!Moving towards " + targetDirection + " " + discoverDir + " " + prevSplitLocations.size());
        System.out.println("Prev split " + prevSplitLocation + " " + rc.getLocation());

        if (prevSplitLocation != null && rc.getLocation().equals(prevSplitLocation.getKey())) {
            System.out.println("At previous split loc " + prevSplitLocation.getKey() + " " + discoverDir);
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
                //This condition is met if couldn't get to destination after one entire cycle, and should be called
                //at the first split location (instead of all the way to the start).
                if (prevSplitLocations.empty()) {
                    //start all over again
                    prevLocations.clear();
                    discoverDir = "right";
                    split = false;
                    headBackToPrevSplitLocation = false;
                }
            }
        } else if (prevSplitLocation != null && rc.getLocation() != prevSplitLocation.getKey() && headBackToPrevSplitLocation) {
            if (!prevLocations.empty()) {
                MapLocation prevLocation = prevLocations.peek();
//                if (prevLocation.equals(rc.getLocation()) && !prevLocations.empty()) {
//                    prevLocation = prevLocations.pop();
//                }
                System.out.println("Backtracking to " + prevLocation);
                if (tryMove(rc.getLocation().directionTo(prevLocation))){
                    prevLocations.pop();
                }
            }
        }
        MapLocation temp = rc.getLocation(); //add the loc before moving

        //to make sure you don't walk back to previous location when discovering, that
        //sometimes causes unit to just move back and forth.
        boolean walkingPrevDirection = !prevLocations.empty() && targetDirection.equals(rc.getLocation().directionTo(prevLocations.peek()));
        System.out.println("TEST!!! " + walkingPrevDirection + " " + tileGoingToFlood(targetDirection));
        if (!walkingPrevDirection && tryMove(targetDirection)) {
            System.out.println("Walked in desired dir");
            prevLocations.push(temp);
            if (split) {
                split = false;
                discoverDir = "right";
            }
        } else {
            System.out.println("Couldn't move towards target direction " + split);
            if (!split) {
                System.out.println("SPLIT - push" + rc.getLocation());
                prevSplitLocations.push(new Pair(rc.getLocation(), discoverDir));
                prevSplitLocation = new Pair(rc.getLocation(), discoverDir);
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
                for (Direction dir : dirs) {
                    if (tryMove(dir)) {
                        moved = true;
                        prevLocations.push(temp);
                        System.out.println("Pushed prev location " + temp);
                    }
                }
                if (!moved) {
                    System.out.println("Couldn't move " + discoverDir + " " + prevSplitLocations.size());
                    if (!prevSplitLocations.empty()) {
                        prevSplitLocation = prevSplitLocations.peek();
                        System.out.println("Prev split " + prevSplitLocation);
                        if (discoverDir.equals("left") && headBackToPrevSplitLocation) {
                            System.out.println("pop " + prevSplitLocations.size());
                            prevSplitLocations.pop();
                        }
                    }
                    headBackToPrevSplitLocation = true;

                }
            }
        }
        System.out.println("Bytecode 4 " + Clock.getBytecodeNum());
        System.out.println("============================================");
    }

    boolean tileGoingToFlood(Direction currentDir) throws GameActionException {
        //Fixes issue for maps like hills
        MapLocation nextMapLoc = rc.adjacentLocation(currentDir);
        int currentDirElevation = rc.senseElevation(nextMapLoc);
        for (Direction dir : Util.directions) {
            if (rc.canSenseLocation(nextMapLoc.add(dir)) && rc.senseFlooding(nextMapLoc.add(dir))
                    && rc.senseElevation(nextMapLoc.add(dir)) >= currentDirElevation) {
                System.out.println("Has flooding adjacent");
                return true;
            }
        }
        return false;
    }

    public void broadcastRealEnemyHQCoordinates() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = ENEMY_HQ_LOC;
        message[2] = targetEnemyX; // possible x coord of enemy HQ
        message[3] = targetEnemyY; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
        System.out.println("Broadcasting real enemy HQ coordinates " + targetEnemyX + " " + targetEnemyY);
    }

    public void clearMovement () {
        prevSplitLocations.clear();
        discoverDir = "right";
        prevLocations.clear();
        headBackToPrevSplitLocation = false;
        prevSplitLocation = null;
        split = false;
    }

    void findEnemyHQ() throws GameActionException {
        if (enemyHqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                    enemyHqLoc = robot.location;
                }
            }
            if (enemyHqLoc == null) {
                // if still null, search the blockchain
                getRealEnemyHQFromBlockchain();
            }
        }
    }
}
