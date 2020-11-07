package redemptionplayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Landscaper extends Unit {
    static boolean rushType = false;
    static ArrayList<MapLocation> outerCircle;
    static MapLocation[] innerCircle;
    static int turtlePosition = 0;

    public Landscaper(RobotController rc) throws GameActionException {
        super(rc);
        outerCircle = new ArrayList<>();
    }

    public void run() throws GameActionException {
        super.run();
        if (turnCount == 1 && nearbyEnemyRobot(RobotType.HQ)) {
            rushType = true;
        }

        //Gets the enemy HQ coordinate, if gotten already sends landscrapers to enemy HQ
        if (rushType && enemyHqLoc == null) {
            getRealEnemyHQFromBlockchain();
        }

        if (!rushType) {
//            if (turnCount == 1) {
//                //getOuterCircle
//                int top = hqLoc.y + 3;
//                int left = hqLoc.x - 3;
//                int right = hqLoc.x + 3;
//                int bottom = hqLoc.y - 3;
//                for (int i = 0; i < 4; i++) {
//                    for (int j = 0; j < 6; j++) {
//                        MapLocation mapLoc = null;
//                        switch (i) {
//                            case 0:
//                                mapLoc = new MapLocation(left, top - j);
//                                break;
//                            case 1:
//                                mapLoc = new MapLocation(left + j, bottom);
//                                break;
//                            case 2:
//                                mapLoc = new MapLocation(right, bottom + j);
//                                break;
//                            case 3:
//                                mapLoc = new MapLocation(right - j, top);
//                        }
//                        if (mapLoc != null && rc.onTheMap(mapLoc)) outerCircle.add(mapLoc);
//                    }
//                }
//            }
            // first, save HQ by trying to remove dirt from it
            if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
                Direction dirtohq = rc.getLocation().directionTo(hqLoc);
                if (rc.canDigDirt(dirtohq)) {
                    rc.digDirt(dirtohq);
                }
            }

            if (rc.getDirtCarrying() == 0) {
                tryDig(false);
            }

            // otherwise try to get to the hq
            if (hqLoc != null) {
                turtle();
            } else {
                goTo(Util.randomDirection());
            }
        } else {
            if (rc.getDirtCarrying() == 0) {
                tryDig(true);
                System.out.println("Try dig");
            } else if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 4
                    && rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
                //If nearby enemy HQ, bury it
                rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
                System.out.println("Buried Enemy HQ");
            } else {
                //If not nearby enemy HQ, continue moving towards it
                surroundEnemyHQ();
            }
        }

        System.out.println("BCL " + Clock.getBytecodesLeft());
    }

    boolean tryDig(boolean enemyHQ) throws GameActionException {
        Direction dir;
        if (enemyHQ && enemyHqLoc != null) {
            //set to dig the spot behind the landscaper
            dir = rc.getLocation().directionTo(enemyHqLoc).opposite();
            if (rc.canDigDirt(dir)) {
                rc.digDirt(dir);
                return true;
            }
        } else {
            if (hqLoc == null) {
                dir = Util.randomDirection();
            } else {
                //always dig away from HQ
                dir = hqLoc.directionTo(rc.getLocation()).rotateRight().rotateRight();
            }
            if (rc.canDigDirt(dir)) {
                rc.digDirt(dir);
                rc.setIndicatorDot(rc.getLocation().add(dir), 255, 0, 0);
                return true;
            }
        }
        return false;
    }

    void turtle() throws GameActionException {
        MapLocation topLeft = hqLoc.add(Util.directions[7]);
        MapLocation topRight = hqLoc.add(Util.directions[1]);
        MapLocation bottomLeft = hqLoc.add(Util.directions[5]);
        MapLocation bottomRight = hqLoc.add(Util.directions[3]);

        if (!rc.getLocation().equals(topLeft) && !rc.getLocation().equals(topRight) &&
                !rc.getLocation().equals(bottomLeft) && !rc.getLocation().equals(bottomRight)) {
            if (rc.canSenseLocation(topLeft)
                    && !rc.isLocationOccupied(topLeft)) {
                dfsWalk(topLeft);
            } else if (rc.canSenseLocation(topRight)
                    && !rc.isLocationOccupied(topRight)) {
                dfsWalk(topRight);
            } else if (rc.canSenseLocation(bottomLeft)
                    && !rc.isLocationOccupied(bottomLeft)) {
                dfsWalk(bottomLeft);
            } else if (rc.canSenseLocation(bottomRight)
                    && !rc.isLocationOccupied(bottomRight)) {
                dfsWalk(bottomRight);
            } else {
                dfsWalk(hqLoc);
            }
        } else {
            MapLocation bestPlaceToBuildWall = null;
            // find best place to build
            if (hqLoc != null) {
                int lowestElevation = 9999999;
                for (Direction dir : Util.directions) {
                    MapLocation tileToCheck = hqLoc.add(dir);
                    if (tileToCheck.isAdjacentTo(hqLoc) && rc.getLocation().distanceSquaredTo(tileToCheck) < 4
                            && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                        if (rc.senseElevation(tileToCheck) < lowestElevation) {
                            lowestElevation = rc.senseElevation(tileToCheck);
                            bestPlaceToBuildWall = tileToCheck;
                        }
                    }
                }
            }

//            if (Math.random() < 0.8){
            // build the wall
            if (bestPlaceToBuildWall != null) {
                rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                System.out.println("building a wall");
            }
//            }
        }
    }

    void surroundEnemyHQ() throws GameActionException {
        if (!rc.getLocation().isAdjacentTo(enemyHqLoc)) {
            for (Direction dir : Util.directions) {
                dfsWalk(enemyHqLoc.add(dir));
            }
        }
    }
}