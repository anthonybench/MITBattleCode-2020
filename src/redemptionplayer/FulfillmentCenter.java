package redemptionplayer;

import battlecode.common.*;

public class FulfillmentCenter extends Building {
    static boolean madeDrone = false;
    static int droneCount = 0;
    public FulfillmentCenter (RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        findHQ();
        if (rc.getLocation().isWithinDistanceSquared(hqLoc, 20)) {
            //fulfillment center near our HQ can build more than one drone
            if (droneCount < 2) {
                for (Direction dir : Util.directions) {
                    if (!madeDrone && tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                        System.out.println("Made a Drone!");
                        droneCount++;
                        break;
                    }
                }
            } else if (rc.getTeamSoup() > 152) {
                for (Direction dir : Util.directions) {
                    if (!madeDrone && tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                        System.out.println("Made a Drone!");
                        droneCount++;
                        break;
                    }
                }
            }
        } else {
            for (Direction dir : Util.directions) {
                if (!madeDrone && tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                    System.out.println("Made a Drone!");
                    madeDrone = true;
                    break;
                }
            }
        }
    }
}
