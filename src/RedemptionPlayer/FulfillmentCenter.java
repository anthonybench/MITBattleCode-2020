package RedemptionPlayer;

import battlecode.common.*;

public class FulfillmentCenter extends Building {
    static boolean madeDrone = false;

    public FulfillmentCenter (RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (!madeDrone && tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                System.out.println("Made a Drone!");
                madeDrone = true;
            }
        }
    }
}
