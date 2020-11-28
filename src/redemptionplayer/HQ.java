package redemptionplayer;

import battlecode.common.*;

public class HQ extends Building {
    int numMiners = 0;
    static boolean builtMinerAfter100 = false;
    MapLocation temp;
    public HQ (RobotController rc) throws GameActionException{
        super(rc);
        NavHelper s = new NavHelper();
        temp = s.abc(rc.getLocation(), rc.getMapWidth(), rc.getMapHeight());
    }

    public void run() throws GameActionException {
        super.run();

        if (turnCount == 1) {
            sendHqLoc(rc.getLocation());
        }

        if (temp != null && numMiners == 0) {
            if (tryBuild(RobotType.MINER, rc.getLocation().directionTo(temp))){
                numMiners++;
                return;
            }
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                    break;
                }
            }
        }

        if (temp != null && numMiners < 4) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                    break;
                }
            }
        } else if (temp == null && numMiners < 5) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                    break;
                }
            }
        }

        if ((rc.getRoundNum() > 185 || temp == null) && !builtMinerAfter100 && rc.getRoundNum() >= backupRound) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                    builtMinerAfter100 = true;
                    break;
                }
            }
        }

        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.DELIVERY_DRONE && r.team != rc.getTeam()
                    && rc.canShootUnit(r.getID())) {
                rc.shootUnit(r.getID());
                break;
            }
        }
    }

    public void sendHqLoc(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = HQ_LOC;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }
}
