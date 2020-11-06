package RedemptionPlayer;

import battlecode.common.*;

public class HQ extends Building {
    int mapWidth = 30;
    int mapHeight = 30;
    int numMiners = 0;
    int possibleY = 0;
    int possibleX = 0;

    public HQ (RobotController rc) throws GameActionException{
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        System.out.println("BOUNDARIES" + mapWidth + " " + mapHeight);
        System.out.println("Bytecode HQ" + Clock.getBytecodeNum());

        if (turnCount == 1) {
            sendHqLoc(rc.getLocation());
        }

        if (numMiners < 5) {
            for (Direction dir : Util.directions)
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                }
        }

        int targetID = nearbyEnemyDrone();
        if (targetID != -1 && rc.canShootUnit(targetID)) {
            rc.shootUnit(targetID);
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
