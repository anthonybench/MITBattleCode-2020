package RedemptionPlayer;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    int turnCount = 0;
    int teamSecret = 123456;
    MapLocation hqLoc;

    public Robot(RobotController rc) {
        this.rc = rc;
    }

    public void run() throws GameActionException {
        turnCount += 1;

    }
    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }

    boolean nearbyRobot(RobotType target) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == target) {
                return true;
            }
        }
        return false;
    }

    public void getHqLocFromBlockchain() throws GameActionException {
        System.out.println("B L O C K C H A I N");
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == 0) {
                    System.out.println("found the HQ!");
                    hqLoc = new MapLocation(mess[2], mess[3]);
                }
            }
        }
    }
}
