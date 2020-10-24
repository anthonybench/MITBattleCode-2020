package RedemptionPlayer;
import battlecode.common.*;

public class Miner extends Unit {

    public Miner(RobotController rc) {
        super(rc);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        //        if (enemyHqX == -1 && enemyHqY == -1) {
//            getEnemyHQCoordinates();
//            enemyHqLoc = new MapLocation(enemyHqX, enemyHqY);
//        } else if (enemyHqY != -1 && enemyHqX != -1) {
//            System.out.println("Enemy HQ" + enemyHqLoc);
//            if (goTo(enemyHqLoc)) {
//                System.out.println("Went to possible enemy HQ coordinate" + enemyHqX + ", " + enemyHqY);
//            } else {
//                System.out.println("Couldn't move to enemy HQ");
//            }
//        }
        if (!nearbyRobot(RobotType.DESIGN_SCHOOL)) {
            if (tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection()))
                System.out.println("created a design school");
        }

        for (Direction dir : Util.directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
//        for (Direction dir : directions)
//            if (tryMine(dir))
//                System.out.println("I mined soup! " + rc.getSoupCarrying());

        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
            // time to go back to the HQ
            if (goTo(hqLoc))
                System.out.println("moved towards HQ");
        } else {
            // Try to find soup
            MapLocation[] soupToMine = rc.senseNearbySoup();
            if (soupToMine.length == 0) {
                // If no soup is nearby, search for soup by moving randomly
                if (goTo(Util.randomDirection())) {
                    System.out.println("I moved randomly!");
                }
            } else if (Math.abs(soupToMine[0].x - rc.getLocation().x) < 2 &&
                    Math.abs(soupToMine[0].y - rc.getLocation().y) < 2) {
                // Else if soup is adjacent, mine it
                for (Direction dir : Util.directions) {
                    if (tryMine(dir)) {
                        System.out.println("I mined soup! " + rc.getSoupCarrying());
                    }
                }
            } else {
                // Otherwise, travel towards the detected soup
                if (soupToMine[0].x > rc.getLocation().x) {
                    if (rc.canMove(Direction.EAST))
                        rc.move(Direction.EAST);
                } else if (soupToMine[0].x < rc.getLocation().x) {
                    if (rc.canMove(Direction.WEST))
                        rc.move(Direction.WEST);
                }
                if (soupToMine[0].y > rc.getLocation().y) {
                    if (rc.canMove(Direction.NORTH))
                        rc.move(Direction.NORTH);
                } else if (soupToMine[0].y < rc.getLocation().y) {
                    if (rc.canMove(Direction.SOUTH))
                        rc.move(Direction.SOUTH);
                }
            }
        }
    }
    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }
}
