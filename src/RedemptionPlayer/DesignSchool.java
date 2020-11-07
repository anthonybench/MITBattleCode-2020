package RedemptionPlayer;

import battlecode.common.*;

public class DesignSchool extends Building {
    static int landscaperCount = 0;

    public DesignSchool(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        //halt building landscapers, to wait for miner to build net guns.
        if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
            return;
        } else if (nearbyEnemyRobot(RobotType.HQ)) {
            if (landscaperCount <= 2) {
                if (!broadcastedHalt && !haltProduction && rc.getTeamSoup() < 150) {
                    broadcastHaltProduction();
                    broadcastedHalt = true;
                }
            } else {
                if (!broadcastedCont && haltProduction) {
                    broadcastContinueProduction();
                    broadcastedCont = true;
                }
            }
            //leave some soup for communication
            if (landscaperCount <= 4 && rc.getTeamSoup() > 160) {
                System.out.println(landscaperCount + " 1");
                tryBuildLandscaper();
            }
        } else if (landscaperCount <= 2 || rc.getTeamSoup() > 400) {
            if (enemyHqLoc != null) {
                getHaltProductionFromBlockchain();
                getContinueProductionFromBlockchain();
            }
            if (checkHalt()) {
                return;
            }
            System.out.println(landscaperCount + " 2");
            tryBuildLandscaper();
        }

        System.out.println("BC " + Clock.getBytecodeNum());
    }

    public void tryBuildLandscaper() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (tryBuild(RobotType.LANDSCAPER, dir)) {
                landscaperCount++;
                System.out.println(landscaperCount + " Made a landscaper");
            }
        }
    }
}