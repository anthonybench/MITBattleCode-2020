package redemptionplayer3;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import static java.lang.Math.abs;

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
    static int stuckCount = 3;
    static int hugDirection = 0; // 0 for left, 1 for right;
    Map<MapLocation, Integer> mapLocations;
    static MapLocation prevLocation;
    static int randomDirectionCount = 10;
    static Direction randomDirection;
    static Direction prevDirection;
    static boolean giveUpMinerRush = false;
    static int giveUpTurn = 200;

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
        potentialEnemyHQY = rc.getMapHeight() - hqLoc.y - 1;
        potentialEnemyHQX = rc.getMapWidth() - hqLoc.x - 1;
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
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
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

        if (prevSplitLocation != null && rc.getLocation().equals(prevSplitLocation.getKey())) {
            if (discoverDir.equals("right")) {
                discoverDir = "left";
                headBackToPrevSplitLocation = false;
            } else if (discoverDir.equals("left")) {
                headBackToPrevSplitLocation = true;
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
                if (tryMove(rc.getLocation().directionTo(prevLocation))) {
                    prevLocations.pop();
                }
            }
        }
        MapLocation temp = rc.getLocation(); //add the loc before moving

        //to make sure you don't walk back to previous location when discovering, that
        //sometimes causes unit to just move back and forth.
        boolean walkingPrevDirection = !prevLocations.empty() && targetDirection.equals(rc.getLocation().directionTo(prevLocations.peek()));
        if (!walkingPrevDirection && tryMove(targetDirection)) {
            prevLocations.push(temp);
            if (split) {
                split = false;
                discoverDir = "right";
            }
        } else {
            if (!split) {
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
                    }
                }
                if (!moved) {
                    if (!prevSplitLocations.empty()) {
                        prevSplitLocation = prevSplitLocations.peek();
                        if (discoverDir.equals("left") && headBackToPrevSplitLocation) {
                            prevSplitLocations.pop();
                        }
                    }
                    headBackToPrevSplitLocation = true;

                }
            }
        }
    }

    boolean tileGoingToFlood(Direction currentDir) throws GameActionException {
        //Fixes issue for maps like hills
        MapLocation nextMapLoc = rc.adjacentLocation(currentDir);
        int currentDirElevation = rc.senseElevation(nextMapLoc);
        for (Direction dir : Util.directions) {
            if (rc.canSenseLocation(nextMapLoc.add(dir)) && rc.senseFlooding(nextMapLoc.add(dir))
                    && rc.senseElevation(nextMapLoc.add(dir)) >= currentDirElevation) {
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
        System.out.println(" real enemy HQ coordinates " + targetEnemyX + " " + targetEnemyY);
    }

    public void clearMovement() {
        prevSplitLocations.clear();
        discoverDir = "right";
        prevLocations.clear();
        headBackToPrevSplitLocation = false;
        prevSplitLocation = null;
        split = false;
    }

    void enemyBaseFindingLogic() {
        //Sets the first miner's targeted locations

        switch (enemyPotentialHQNumber) {
            case 1:
                targetEnemyX = potentialEnemyHQX;
                targetEnemyY = hqLoc.y;
                break;
            case 2:
                targetEnemyX = potentialEnemyHQX;
                targetEnemyY = potentialEnemyHQY;
                break;
            case 3:
                targetEnemyX = hqLoc.x;
                targetEnemyY = potentialEnemyHQY;
                break;
            default:
                break;
        }
        NavHelper s = new NavHelper();
        MapLocation temp = s.abc(hqLoc, rc.getMapWidth(), rc.getMapHeight());
        if (temp == null) {
            giveUpTurn = 50;
        } else {
            targetEnemyX = temp.x;
            targetEnemyY = temp.y;
        }
    }

    public boolean locationOccupiedWithSameTeamRobot(MapLocation mapLoc) throws GameActionException {
        if (rc.canSenseLocation(mapLoc) && rc.isLocationOccupied(mapLoc)) {
            RobotInfo robot = rc.senseRobotAtLocation(mapLoc);
            if (robot.getTeam() == rc.getTeam() && robot.getType() != RobotType.DELIVERY_DRONE) {
                return true;
            }
        }
        return false;
    }

    public void navigation(MapLocation destination) throws GameActionException {
        //Navigate towards the destination
        Direction intentedDir = rc.getLocation().directionTo(destination);
        System.out.println(intentedDir + " " + prevDirection);

        if (rc.getCooldownTurns() >= 1) {
            return;
        }
        MapLocation curLoc = rc.getLocation();
        if (prevLocation != null || (prevDirection) == intentedDir || !tryMove(intentedDir)) {
            //if the unit couldn't move the intended direction, hug to the right, then to the left
            ArrayList<Direction> dirs = new ArrayList<>();
            System.out.println("Couldn't move " + hugDirection);
//            if (rc.senseRobotAtLocation(rc.getLocation().add(intentedDir)) != null) {
//                if (hugDirection == 0) {
//                    if (tryMove(intentedDir.rotateLeft()) || tryMove(intentedDir.rotateRight())) {
//                        return;
//                    }
//                }
//            }
//
//            if (rc.getCooldownTurns() > 1) {
//                return;
//            }
            System.out.println("Hug direction " + rc.getCooldownTurns());
            if (hugDirection == 0) {
                dirs.add(intentedDir.rotateLeft());
                dirs.add(intentedDir.rotateLeft().rotateLeft());
                dirs.add(intentedDir.rotateLeft().rotateLeft().rotateLeft());
                dirs.add(intentedDir.rotateLeft().rotateLeft().rotateLeft().rotateLeft());
            } else {
                dirs.add(intentedDir.rotateRight());
                dirs.add(intentedDir.rotateRight().rotateRight());
                dirs.add(intentedDir.rotateRight().rotateRight().rotateRight());
                dirs.add(intentedDir.rotateRight().rotateRight().rotateRight().rotateRight());
            }

            boolean couldMove = false;
            for (Direction dir : dirs) {
                System.out.println(dir + " " + rc.canMove(dir));
                if (tryMove(dir)) {
                    couldMove = true;
                }
            }

            if (!couldMove) {
                System.out.println("COuldn't move!");
                //if couldn't move after hugging a certain direction
                if (hugDirection == 0) {
                    //if was hugging to the left, now hug to the right;
                    hugDirection = 1;
                } else {
                    //if was hugging to the right, then it could mean the unit is stuck
                    hugDirection = hugDirection == 0 ? 1 : 0;
                }
                prevDirection = null;
            }
        }
        prevDirection = rc.getLocation().directionTo(curLoc);
    }
}