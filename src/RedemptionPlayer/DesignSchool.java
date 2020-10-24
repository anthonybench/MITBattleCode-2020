package RedemptionPlayer;

import battlecode.common.*;

public class DesignSchool extends Building {

    public DesignSchool (RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (tryBuild(RobotType.LANDSCAPER, dir)) {
                System.out.println("Made a landscaper");
            }
        }
    }
}
