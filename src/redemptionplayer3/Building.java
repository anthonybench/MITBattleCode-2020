package redemptionplayer3;
import battlecode.common.*;

public class Building extends Robot {

    public Building(RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
    }

    int nearbyEnemyDrone() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.DELIVERY_DRONE && r.team != rc.getTeam()) {
                return r.getID();
            }
        }
        return -1;
    }
}