package redemptionplayer;

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
    static final int HALT_PRODUCTION = 4;
    static final int CONTINUE_PRODUCTION = 5;
    static final int REFINERY_LOCATION = 6;
    static final int SOUP_LOCATION = 7;
    static final int GIVE_UP_MINER_RUSH = 8;
    static boolean haltProduction = false;
    static int haltTurn = 0;
    static int continueTurn = 0;
    static boolean broadcastedHalt = false;
    static boolean broadcastedCont = false;
    static final int backupRound = 90;

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
     * @param dir  The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        System.out.println(rc.isReady() + " " + rc.canBuildRobot(type, dir));
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
                    return;
                }
            }
        }
    }

    public void getRealEnemyHQFromBlockchain() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                System.out.println(i + " " + mess[1]);
                if (mess[0] == teamSecret && mess[1] == ENEMY_HQ_LOC) {
                    System.out.println("got the real enemy HQ coord!");
                    enemyHqLoc = new MapLocation(mess[2], mess[3]);
                    return;
                }
            }
        }
    }

    public void broadcastHaltProduction() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = HALT_PRODUCTION;
        message[2] = rc.getRoundNum();
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
        System.out.println("Broadcast halt!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        broadcastedHalt = true;
    }

    public void broadcastContinueProduction() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = CONTINUE_PRODUCTION;
        message[2] = rc.getRoundNum();
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
        System.out.println("Broadcast continue!!!!!!!!!!!!!!!!!!!!!!!");
        broadcastedCont = true;
    }

    public void getHaltProductionFromBlockchain() throws GameActionException {
        for (int i = rc.getRoundNum() - 3; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == HALT_PRODUCTION) {
                    System.out.println("Halt!");
                    haltProduction = true;
                    haltTurn = mess[2];
                    return;
                }
            }
        }
    }

    public void getContinueProductionFromBlockchain() throws GameActionException {
        for (int i = rc.getRoundNum() - 3; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == CONTINUE_PRODUCTION) {
                    System.out.println("Cont!");
                    haltProduction = false;
                    continueTurn = mess[2];
                    return;
                }
            }
        }
    }

    public boolean checkHalt() {
        return haltProduction && haltTurn > continueTurn;
    }

    void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                }
            }
            if (hqLoc == null) {
                // if still null, search the blockchain
                getHqLocFromBlockchain();
            }
        }
    }
}
