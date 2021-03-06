package redemptionplayer;

import battlecode.common.*;

public class FulfillmentCenter extends Building {
    static boolean madeDrone = false;
    static int droneCount = 0;

    public FulfillmentCenter(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        findHQ();
        System.out.println(haltProduction + "--------------");
        getHaltProductionFromBlockchain();
        getContinueProductionFromBlockchain();
        //fulfillment center near our HQ can build more than one drone
        if (checkHalt()) {
            return;
        }
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

    }
}
