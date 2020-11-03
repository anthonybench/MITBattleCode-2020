package RedemptionPlayer;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    int turnCount = 0;
    int teamSecret = 384392;
    static MapLocation hqLoc;
    static MapLocation enemyHqLoc;
    static final int HQ_LOC = 0;
    static final int ENEMY_HQ_LOC = 1;
    static final int PICKED_UP_MINER = 2;
    static final int UBER_REQUEST = 3;
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

    boolean nearbyTeamRobot(RobotType target) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == target && r.team == rc.getTeam()) {
                return true;
            }
        }
        return false;
    }

    boolean nearbyEnemyRobot(RobotType target) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == target && r.team != rc.getTeam()) {
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
                if (mess[0] == teamSecret && mess[1] == HQ_LOC) {
                    System.out.println("found the HQ!");
                    hqLoc = new MapLocation(mess[2], mess[3]);
                }
            }
        }
    }

    public void getRealEnemyHQFromBlockchain() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == ENEMY_HQ_LOC) {
                    System.out.println("got the real enemy HQ coord!");
                    enemyHqLoc = new MapLocation(mess[2], mess[3]);
                }
            }
        }
    }
}
