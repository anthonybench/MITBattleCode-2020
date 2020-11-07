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
        if (rc.getLocation().isWithinDistanceSquared(hqLoc, 5)) {
            //fulfillment center near our HQ can build more than one drone
            if (rc.getLocation().isAdjacentTo(hqLoc) && droneCount >= 1) {
                rc.disintegrate(); //Map - in a ditch
            }
            if (droneCount < 20) {
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
