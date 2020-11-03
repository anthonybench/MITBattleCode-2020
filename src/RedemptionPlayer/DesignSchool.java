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
        }

        if (landscaperCount <= 4) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscaperCount++;
                    System.out.println("Made a landscaper");
                }
            }
        }
    }
}
