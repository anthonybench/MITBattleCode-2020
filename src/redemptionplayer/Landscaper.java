package redemptionplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Map;

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

        //Gets the enemy HQ coordinate, if gotten already sends landscrapers to enemy HQ
//        if (!gotBlockchainMess && !rushType) {
//            findEnemyHQ();
//            gotBlockchainMess = true;
//        }
        findEnemyHQ();
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
//            for (RobotInfo robot : robotInfos) {
//                if (robot.getType() == RobotType.DESIGN_SCHOOL && robot.getTeam() != rc.getTeam()) {
//                    target = robot.getLocation();
//                    break;
//                }
//            }

            if (rc.getDirtCarrying() == 0) {
                tryDig(true);
                System.out.println("Try dig");
            } else if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 4
                    && rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
                //If nearby enemy HQ, bury it
                rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
                System.out.println("Buried Enemy HQ " + rc.getLocation().directionTo(enemyHqLoc));
            } else if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 8) {
                //don't deposit dirt to help enemy build wall.
                for (Direction dir : Util.directions) {
                    if (!rc.getLocation().add(dir).isAdjacentTo(enemyHqLoc) && rc.canDepositDirt(dir)) {
                        rc.depositDirt(dir);
                        break;
                    }
                }
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
            }

            if (rc.getCooldownTurns() < 1) {
                //If not nearby enemy HQ, continue moving towards it
                surroundEnemyHQ();
            }
        }

        System.out.println("BCL " + Clock.getBytecodesLeft());
    }

    boolean tryDig(boolean enemyHQ) throws GameActionException {
        if (enemyHQ && enemyHqLoc != null) {
            //try dig the enemy wall first
            for (Direction dir : Util.directions) {
                if (rc.canDigDirt(dir) && !rc.getLocation().add(dir).equals(enemyHqLoc)
                        && rc.getLocation().add(dir).isAdjacentTo(enemyHqLoc) && outerRushLandscaperCanDig(rc.getLocation().add(dir))) {
                    rc.digDirt(dir);
                    System.out.println("Dug " + dir);
                    return true;
                }
            }

            for (Direction dir : Util.directions) {
                if (rc.canDigDirt(dir) && !rc.getLocation().add(dir).equals(enemyHqLoc)
                        && outerRushLandscaperCanDig(rc.getLocation().add(dir))) {
                    rc.digDirt(dir);
                    System.out.println("Dug " + dir);
                    return true;
                }
            }
        } else {
            //dig at predefined locations first, if can't then dig at random loc away from HQ and not part of the wall
            MapLocation left = new MapLocation(hqLoc.x - 2, hqLoc.y);
            if (rc.getLocation().x < hqLoc.x && !locationOccupiedWithSameTeamRobot(left)
                    && rc.canDigDirt(rc.getLocation().directionTo(left))) {
                rc.digDirt(rc.getLocation().directionTo(left));
            }
            MapLocation right = new MapLocation(hqLoc.x + 2, hqLoc.y);
            if (rc.getLocation().x > hqLoc.x && !locationOccupiedWithSameTeamRobot(right)
                    && rc.canDigDirt(rc.getLocation().directionTo(right))) {
                rc.digDirt(rc.getLocation().directionTo(right));
            }
            MapLocation top = new MapLocation(hqLoc.x, hqLoc.y + 2);
            if (rc.getLocation().y > hqLoc.y && !locationOccupiedWithSameTeamRobot(top)
                    && rc.canDigDirt(rc.getLocation().directionTo(top))) {
                rc.digDirt(rc.getLocation().directionTo(top));
            }
            MapLocation bottom = new MapLocation(hqLoc.x, hqLoc.y - 2);
            if (rc.getLocation().y < hqLoc.y && !locationOccupiedWithSameTeamRobot(bottom)
                    && rc.canDigDirt(rc.getLocation().directionTo(bottom))) {
                rc.digDirt(rc.getLocation().directionTo(bottom));
            }

            for (Direction dirs : Util.directions) {
                if (!rc.getLocation().add(dirs).isAdjacentTo(hqLoc) && rc.canDigDirt(dirs)
                        && !locationOccupiedWithSameTeamRobot(rc.getLocation().add(dirs))) {
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

        if (!rc.getLocation().isWithinDistanceSquared(hqLoc, 4)) {
            dfsWalk(hqLoc);
        } else {
            MapLocation bestPlaceToBuildWall = null;

            // find best place to build
            if (hqLoc != null) {
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

                boolean stillCanMove = false;
                for (Direction dir : Util.directions) {
                    if (rc.canMove(dir) && rc.getLocation().add(dir).isAdjacentTo(hqLoc)) {
                        stillCanMove = true;
                        System.out.println("Can Move");
                        break;
                    }
                }

                if (stillCanMove && Math.random() < 0.2) {
                    goTo(Util.randomDirection());
                }

                if (bestPlaceToBuildWall != null && rc.canDepositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall))) {
                    rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                    rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                    System.out.println("building a wall");
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

    public boolean outerRushLandscaperCanDig(MapLocation mapLoc) throws GameActionException {
        return !locationOccupiedWithSameTeamRobot(mapLoc) || (rc.senseElevation(mapLoc) > 5);
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