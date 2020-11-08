package redemptionplayer;

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
            //leave some soup for communication
            if (rc.getRoundNum() < 250 && landscaperCount <= 5) {
                System.out.println(landscaperCount + " 1");
                tryBuildLandscaper();
            }
        } else if (landscaperCount <= 3) {
            getHaltProductionFromBlockchain();
            getContinueProductionFromBlockchain();

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