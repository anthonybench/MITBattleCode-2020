package redemptionplayer;

import battlecode.common.*;

public class DesignSchool extends Building {
    static int landscaperCount = 0;
    static int additionalCost = 0;
    static int hqAdjacentSpots = 0;

    public DesignSchool(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        //halt building landscapers, to wait for miner to build net guns.
        findHQ();
        getHaltProductionFromBlockchain();
        getContinueProductionFromBlockchain();

        //check how much adjacent spots to determine how much miners needed.
        if (hqLoc != null && hqAdjacentSpots == 0) {
            if (rc.onTheMap(new MapLocation(hqLoc.x - 1, hqLoc.y))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x + 1, hqLoc.y))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x, hqLoc.y - 1))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x, hqLoc.y + 1))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x - 1, hqLoc.y - 1))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x - 1, hqLoc.y + 1))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x + 1, hqLoc.y - 1))) {
                hqAdjacentSpots++;
            }
            if (rc.onTheMap(new MapLocation(hqLoc.x + 1, hqLoc.y + 1))) {
                hqAdjacentSpots++;
            }
        }

        if (enemyHqLoc == null) {
            findEnemyHQ();
        }
        if (checkHalt()) {
            return;
        }

        if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
            return;
        } else if (nearbyEnemyRobot(RobotType.HQ)) {
            if (rc.getRoundNum() < 250 && landscaperCount <= 5) {
                tryBuildLandscaper(true);
            }
        } else if (landscaperCount < hqAdjacentSpots) {
//            getHaltProductionFromBlockchain();
//            getContinueProductionFromBlockchain();
//
//            if (checkHalt()) {
//                return;
//            }
//            System.out.println(landscaperCount + " 2");
            tryBuildLandscaper(false);
        } else {
            if (nearbyTeamRobot(RobotType.DELIVERY_DRONE) && landscaperCount < hqAdjacentSpots) {
                tryBuildLandscaper(false);
            }
        }

    }

    public void tryBuildLandscaper(boolean rush) throws GameActionException {
        if (rush) {
            Direction targetDir = rc.getLocation().directionTo(enemyHqLoc);
            Direction[] dirs = {targetDir, targetDir.rotateLeft(), targetDir.rotateRight(), targetDir.rotateLeft().rotateLeft(),
                    targetDir.rotateRight().rotateRight()};
            int prevCount = landscaperCount;
            for (Direction dir : dirs) {
                if ((rc.getTeamSoup() > 150 + additionalCost) && tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperCount++;
                    System.out.println(landscaperCount + " Made a landscaper");
                }
            }
            if (landscaperCount == prevCount) {
                for (Direction dir : Util.directions) {
                    if ((rc.getTeamSoup() > 150 + additionalCost) && tryBuild(RobotType.LANDSCAPER, dir)) {
                        landscaperCount++;
                        System.out.println(landscaperCount + " Made a landscaper");
                    }
                }
            }
        } else {
            for (Direction dir : Util.directions) {
                if ((rc.getTeamSoup() > 150 + additionalCost) && tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperCount++;
                    System.out.println(landscaperCount + " Made a landscaper");
                }
            }
        }
    }

    public void broadcastFirstLandscaper() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = FIRST_LANDSCAPER;
        if (rc.canSubmitTransaction(message, 2))
            rc.submitTransaction(message, 2);
    }
}