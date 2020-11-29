package redemptionplayer;

import battlecode.common.*;

public class DesignSchool extends Building {
    static int landscaperCount = 0;
    static int additionalCost = 0;
    public DesignSchool(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        //halt building landscapers, to wait for miner to build net guns.
        getHaltProductionFromBlockchain();
        getContinueProductionFromBlockchain();
        if (checkHalt()) {
            return;
        }
        if (!nearbyTeamRobot(RobotType.NET_GUN) && nearbyEnemyRobot(RobotType.DELIVERY_DRONE)) {
            return;
        } else if (nearbyEnemyRobot(RobotType.HQ)) {
            //leave some soup for communication
            if (rc.getRoundNum() < 250 && landscaperCount <= 5) {
                System.out.println(landscaperCount + " 1");
                tryBuildLandscaper();
            }
        } else if (landscaperCount <= 3) {
//            getHaltProductionFromBlockchain();
//            getContinueProductionFromBlockchain();
//
//            if (checkHalt()) {
//                return;
//            }
//            System.out.println(landscaperCount + " 2");
            tryBuildLandscaper();
        } else {
            if (nearbyTeamRobot(RobotType.DELIVERY_DRONE) && landscaperCount <= 7) {
               tryBuildLandscaper();
            }
        }

        System.out.println("BC " + Clock.getBytecodeNum());
    }

    public void tryBuildLandscaper() throws GameActionException {
        for (Direction dir : Util.directions) {
            if ((rc.getTeamSoup() > 150 + additionalCost) && tryBuild(RobotType.LANDSCAPER, dir)) {
                landscaperCount++;
                System.out.println(landscaperCount + " Made a landscaper");
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