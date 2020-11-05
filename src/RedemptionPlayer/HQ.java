package RedemptionPlayer;

import battlecode.common.*;

public class HQ extends Building {
    int mapWidth = 30;
    int mapHeight = 30;
    int numMiners = 0;
    int possibleY = 0;
    int possibleX = 0;

    public HQ (RobotController rc) {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        System.out.println("BOUNDARIES" + mapWidth + " " + mapHeight);

        if (turnCount == 1) {
            sendHqLoc(rc.getLocation());
        }

        //Broadcast once to save soup, first miner would definitely get it on turn 5 (maybe add broadcast cost to ensure)
//        if (turnCount == 5) {
//            if (mapWidth == 30 && mapHeight == 30) {
//                //find current map width
//                while (rc.onTheMap(new MapLocation(mapWidth + 1, 0))) {
//                    mapWidth++;
//                }
//                //find current map height
//                while (rc.onTheMap(new MapLocation(0, mapHeight + 1))) {
//                    mapHeight++;
//                }
//                System.out.println("BOUNDARIES" + mapWidth + " " + mapHeight);
//
//                //horizontally symmetric
//                possibleX = mapWidth - rc.getLocation().x;
//                possibleY = mapHeight - rc.getLocation().y;
//            }
//            System.out.println("Possible points" + possibleX + " " + possibleY);
//            broadcastPotentialEnemyHQCoordinates();
//        }

//        if (turnCount % 10 == 0) {
//            getRealEnemyHQFromBlockchain();
//        }

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

//    public void broadcastPotentialEnemyHQCoordinates() throws GameActionException {
//        int[] message = new int[7];
//        message[0] = teamSecret;
//        message[1] = 11;
//        message[2] = possibleX; // possible x coord of enemy HQ
//        message[3] = possibleY; // possible y coord of enemy HQ
//        message[4] = mapWidth;
//        message[5] = mapHeight;
//        if (rc.canSubmitTransaction(message, 3))
//            rc.submitTransaction(message, 3);
//        System.out.println("broadcasting potential enemy HQ coordinates");
//    }
}
