package redemptionplayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Landscaper extends Unit {
    static boolean rushType = false;
    static ArrayList<MapLocation> outerCircle;
    static MapLocation[] innerCircle;
    static int turtlePosition = 0;
    static boolean gotBlockchainMess = false;
    static boolean stopWalking = false;

    public Landscaper(RobotController rc) throws GameActionException {
        super(rc);
        outerCircle = new ArrayList<>();
    }

    public void run() throws GameActionException {
        super.run();
        if (turnCount == 1 && nearbyEnemyRobot(RobotType.HQ)) {
            rushType = true;
        }

        if (rc.getRoundNum() > 250 && rushType) {
            rushType = false;
        }
        //Gets the enemy HQ coordinate, if gotten already sends landscrapers to enemy HQ
        if (!gotBlockchainMess) {
            findEnemyHQ();
            gotBlockchainMess = true;
        }

        System.out.println("RUSH " + rushType + " " + enemyHqLoc);

        if (!rushType) {
            // otherwise try to get to the hq
            if (hqLoc != null) {
                turtle();
            } else {
                goTo(Util.randomDirection());
            }
        } else {
            RobotInfo[] robotInfos = rc.senseNearbyRobots();

            MapLocation target = enemyHqLoc;
            for (RobotInfo robot : robotInfos) {
                if (robot.getType() == RobotType.DESIGN_SCHOOL && robot.getTeam() != rc.getTeam()) {
                    target = robot.getLocation();
                    break;
                }
            }

            if (rc.getDirtCarrying() == 0) {
                tryDig(true);
                System.out.println("Try dig");
            } else if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 4
                    && rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
                //If nearby enemy HQ, bury it
                rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
                System.out.println("Buried Enemy HQ");
            } else if (target != null) {
                if (rc.getLocation().distanceSquaredTo(target) < 4
                        && rc.canDepositDirt(rc.getLocation().directionTo(target))) {
                    rc.depositDirt(rc.getLocation().directionTo(target));
                } else {
                    if (!rc.getLocation().isAdjacentTo(target)) {
                        for (Direction dir : Util.directions) {
                            dfsWalk(target.add(dir));
                        }
                    }
                }
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
            for (Direction dirs : Util.directions) {
                if (rc.canDigDirt(dirs) && !rc.getLocation().add(dirs).equals(enemyHqLoc)) {
                    rc.digDirt(dirs);
                    System.out.println("Dug");
                    return true;
                }
            }
        } else {
            //always dig away from HQ
            for (Direction dirs : Util.directions) {
                if (!rc.getLocation().add(dirs).isAdjacentTo(hqLoc) && rc.canDigDirt(dirs)
                && !rc.isLocationOccupied(rc.getLocation().add(dirs))) {
                    rc.digDirt(dirs);
                    System.out.println("Dug");
                    return true;
                }
            }
        }
        return false;
    }

    void turtle() throws GameActionException {
        MapLocation topLeft = hqLoc.add(Util.directions[7]);
        MapLocation topRight = hqLoc.add(Util.directions[1]);
        MapLocation bottomLeft = hqLoc.add(Util.directions[5]);
        MapLocation bottomRight = hqLoc.add(Util.directions[3]);

        // first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
            if (rc.canDigDirt(dirtohq)) {
                rc.digDirt(dirtohq);
            }
        }

        if (rc.getDirtCarrying() == 0) {
            System.out.println("Try dig");
            tryDig(false);
        }

        if (!rc.getLocation().isWithinDistanceSquared(hqLoc, 4)) {
            dfsWalk(hqLoc);
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

                for (Direction dir : Util.directions) {
                    if (rc.canMove(dir)) {
                        if (Math.random() < 0.8) {
                            if (bestPlaceToBuildWall != null) {
                                rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                                System.out.println("building a wall");
                            }
                        } else {
                            goTo(Util.randomDirection());
                        }
                        break;
                    } else {
                        if (bestPlaceToBuildWall != null) {
                            rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                            rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                            System.out.println("building a wall");
                        }
                    }
                }
            }
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