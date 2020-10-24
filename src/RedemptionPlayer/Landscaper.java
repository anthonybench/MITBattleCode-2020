package RedemptionPlayer;
import battlecode.common.*;

public class Landscaper extends Unit{

    public Landscaper(RobotController rc) {
        super(rc);
    }

    boolean tryDig() throws GameActionException {
        Direction dir = Util.randomDirection();
        if (rc.canDigDirt(dir)) {
            rc.digDirt(dir);
            return true;
        }
        return false;
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        if (rc.getDirtCarrying() == 0) {
            tryDig();
        }

        //Gets the enemy HQ coordinate, if gotten already sends landscrapers to enemy HQ
        if (enemyHqX == -1 && enemyHqY == -1) {
            getEnemyHQCoordinates();
            enemyHqLoc = new MapLocation(enemyHqX, enemyHqY);
        } else if (enemyHqY != -1 && enemyHqX != -1) {
            System.out.println("Enemy HQ" + enemyHqLoc);
            //If nearby enemy HQ, bury it
            if (rc.getLocation().distanceSquaredTo(enemyHqLoc) < 4
                    && rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
                rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
            }
            //If not nearby enemy HQ, continue moving towards it
            if (goTo(enemyHqLoc)) {
                System.out.println("Went to possible enemy HQ coordinate" + enemyHqX + ", " + enemyHqY);
            } else {
                System.out.println("Couldn't move to enemy HQ");
            }
        }
    }
}
