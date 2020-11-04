package RedemptionPlayer;

import battlecode.common.*;

public class NetGun extends Building {
    public NetGun(RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        int targetID = nearbyEnemyDrone();
        if (targetID != -1 && rc.canShootUnit(targetID)) {
            rc.shootUnit(targetID);
        }
    }

}