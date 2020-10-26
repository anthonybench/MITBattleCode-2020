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

        if (turnCount == 1) {
            sendHqLoc(rc.getLocation());
        }

        //I'm assuming there's a min num of turns it takes to find the enemyHQ
        if (turnCount > 10) {
            getRealEnemyHQFromBlockchain();
        }
        if (enemyHqLoc == null) {
            if (mapWidth == 30 && mapHeight == 30) {
                //find current map width
                while (rc.onTheMap(new MapLocation(mapWidth + 1, 0))) {
                    mapWidth++;
                }
                //find current map height
                while (rc.onTheMap(new MapLocation(0, mapHeight + 1))) {
                    mapHeight++;
                }
                System.out.println("BOUNDARIES" + mapWidth + " " + mapHeight);

                //horizontally symmetric
                possibleX = mapWidth - rc.getLocation().x;
                possibleY = mapHeight - rc.getLocation().y;
            }
            System.out.println("Possible points" + possibleX + " " + possibleY);

            broadcastPotentialEnemyHQCoordinates();
        }

        if (numMiners < 10) {
            for (Direction dir : Util.directions)
                if (tryBuild(RobotType.MINER, dir)) {
                    numMiners++;
                }
        }
    }

    public void sendHqLoc(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 0;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }

    public void broadcastPotentialEnemyHQCoordinates() throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 11;
        message[2] = possibleX; // possible x coord of enemy HQ
        message[3] = possibleY; // possible y coord of enemy HQ
        if (rc.canSubmitTransaction(message, 3))
            rc.submitTransaction(message, 3);
    }
}
