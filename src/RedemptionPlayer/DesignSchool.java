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
                if (!broadcastedHalt && !haltProduction) {
                    broadcastHaltProduction();
                    broadcastedHalt = true;
                }
            } else {
                if (!broadcastedCont && haltProduction) {
                    broadcastContinueProduction();
                    broadcastedCont = true;
                }
            }
        } else if (landscaperCount <= 4) {
            getHaltProductionFromBlockchain();
            getContinueProductionFromBlockchain();
            if (checkHalt()) {
                return;
            }
        }

        for (Direction dir : Util.directions) {
            if (tryBuild(RobotType.LANDSCAPER, dir)) {
                landscaperCount++;
                System.out.println("Made a landscaper");
            }

        }
    }
}
