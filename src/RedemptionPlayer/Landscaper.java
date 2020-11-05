package RedemptionPlayer;

import battlecode.common.*;

public class Landscaper extends Unit {
    static boolean rushType = false;

    public Landscaper(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        if (nearbyEnemyRobot(RobotType.HQ)) {
            rushType = true;
        }

        //Gets the enemy HQ coordinate, if gotten already sends landscrapers to enemy HQ
        if (rushType && enemyHqLoc == null) {
            getRealEnemyHQFromBlockchain();
        }

        if (enemyHqLoc == null && rc.getRoundNum() > 200) {
            //if rush is cancelled switch strategy to defense

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

            MapLocation bestPlaceToBuildWall = null;
            // find best place to build
            if (hqLoc != null) {
                int lowestElevation = 9999999;
                for (Direction dir : Util.directions) {
                    MapLocation tileToCheck = hqLoc.add(dir);
                    if (rc.getLocation().distanceSquaredTo(tileToCheck) < 4
                            && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                        if (rc.senseElevation(tileToCheck) < lowestElevation) {
                            lowestElevation = rc.senseElevation(tileToCheck);
                            bestPlaceToBuildWall = tileToCheck;
                        }
                    }
                }
            }

            if (Math.random() < 0.8) {
                // build the wall
                if (bestPlaceToBuildWall != null) {
                    rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                    rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                    System.out.println("building a wall");
                }
            }

            // otherwise try to get to the hq
            if (hqLoc != null) {
                goTo(hqLoc);
            } else {
                goTo(Util.randomDirection());
            }
        } else {
            if (rc.getDirtCarrying() == 0) {
                tryDig(true);
                System.out.println("Try dig");
            }
            System.out.println("Enemy HQ" + enemyHqLoc);
            //If nearby enemy HQ, bury it
            if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 4
                    && rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
                rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
                System.out.println("Buried Enemy HQ");
            } else
            //If not nearby enemy HQ, continue moving towards it
            if (goTo(enemyHqLoc)) {
                System.out.println("Moving towards enemy HQ");
            } else {
                System.out.println("Couldn't move to enemy HQ");
            }
        }
    }

    boolean tryDig(boolean enemyHQ) throws GameActionException {
        Direction dir;
        if (enemyHQ) {
            //set to dig the spot behind the landscaper
            dir = rc.getLocation().directionTo(enemyHqLoc).rotateRight().rotateRight().rotateRight().rotateRight();
            if (rc.canDigDirt(dir)) {
                rc.digDirt(dir);
                return true;
            }
        } else {
            if (hqLoc == null) {
                dir = Util.randomDirection();
            } else {
                //always dig away from HQ
                dir = hqLoc.directionTo(rc.getLocation());
            }
            if (rc.canDigDirt(dir)) {
                rc.digDirt(dir);
                rc.setIndicatorDot(rc.getLocation().add(dir), 255, 0, 0);
                return true;
            }
        }
        return false;
    }
}
