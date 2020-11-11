package redemptionplayer;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int firstMiner = -1;
    static int secondMiner = -1;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {
        Robot thisRobot = null;

        switch (rc.getType()) {
            case HQ:                 thisRobot = new HQ(rc);                    break;
            case MINER:              thisRobot = new Miner(rc);                 break;
            case REFINERY:           thisRobot = new Refinery(rc);              break;
            case VAPORATOR:          thisRobot = new Vaporator(rc);             break;
            case DESIGN_SCHOOL:      thisRobot = new DesignSchool(rc);          break;
            case FULFILLMENT_CENTER: thisRobot = new FulfillmentCenter(rc);     break;
            case LANDSCAPER:         thisRobot = new Landscaper(rc);            break;
            case DELIVERY_DRONE:     thisRobot = new Drone(rc);                  break;
            case NET_GUN:            thisRobot = new NetGun(rc);                break;
        }

        while(true) {
            try {
                thisRobot.run();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception"); // darn
                e.printStackTrace();
            }
        }
    }

//    static void findEnemyHQ() throws GameActionException {
//        if (enemyHqLoc == null) {
//            // search surroundings for enemy HQ
//            RobotInfo[] robots = rc.senseNearbyRobots();
//            for (RobotInfo robot : robots) {
//                if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
//                    enemyHqLoc = robot.location;
//                }
//            }
//        }
//    }


    static void runVaporator() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
//        for (Direction dir : directions)
//            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }


    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

}
