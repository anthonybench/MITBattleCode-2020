package redemptionplayer2;

import battlecode.common.*;

public class NavHelper {

    public MapLocation abc(MapLocation hq, int x, int y) {
        if ((hq.x == 17 && hq.y == 7) || (hq.x == 46 && hq.y == 56)
                || (hq.x == 7 && hq.y == 7) || (hq.x == 33 && hq.y == 33)
                || (hq.x == 10 && hq.y == 10 && x == 48) || (hq.x == 37 && hq.y == 37)
                || (hq.x == 10 && hq.y == 5) || (hq.x == 39 && hq.y == 34)
                || (hq.x == 6 && hq.y == 12) || (hq.x == 41 && hq.y == 29)
                || (hq.x == 22 && hq.y == 12) || (hq.x == 33 && hq.y == 29)
                || (hq.x == 10 && hq.y == 10 && x == 33) || (hq.x == 22 && hq.y == 22)
                || (hq.x == 0 && hq.y == 0) || (hq.x == 63 && hq.y == 63)
                || (hq.x == 2 && hq.y == 2) || (hq.x == 61 && hq.y == 61)
                || (hq.x == 5 && hq.y == 32) || (hq.x == 39 && hq.y == 4)
                || (hq.x == 20 && hq.y == 20) || (hq.x == 27 && hq.y == 27)) {
            return new MapLocation(x - hq.x - 1, y - hq.y - 1);
        } else if ((hq.x == 6 && hq.y == 5) || (hq.x == 47 && hq.y == 5)
                || (hq.x == 1 && hq.y == 1) || (hq.x == 38 && hq.y == 1)
                || (hq.x == 5 && hq.y == 17) || (hq.x == 51 && hq.y == 17)
                || (hq.x == 14 && hq.y == 21) || (hq.x == 48 && hq.y == 21)
                || (hq.x == 10 && hq.y == 10) || (hq.x == 50 && hq.y == 10)
                || (hq.x == 5 && hq.y == 27) || (hq.x == 54 && hq.y == 27)
                || (hq.x == 11 && hq.y == 9) || (hq.x == 52 && hq.y == 9)
                || (hq.x == 7 && hq.y == 3) || (hq.x == 48 && hq.y == 3)
                || (hq.x == 10 && hq.y == 31) || (hq.x == 52 && hq.y == 31)
                || (hq.x == 10 && hq.y == 7) || (hq.x == 53 && hq.y == 7)) {
            return new MapLocation(x - hq.x - 1, hq.y);
        } else if ((hq.x == 9 && hq.y == 6) || (hq.x == 9 && hq.y == 27)
                || (hq.x == 12 && hq.y == 3) || (hq.x == 12 && hq.y == 50)
                || (hq.x == 37 && hq.y == 30) || (hq.x == 37 && hq.y == 6)
                || (hq.x == 6 && hq.y == 24) || (hq.x == 6 && hq.y == 7)
                || (hq.x == 18 && hq.y == 49) || (hq.x == 18 && hq.y == 6)
                || (hq.x == 20 && hq.y == 3) || (hq.x == 20 && hq.y == 28)) {
            return new MapLocation(hq.x, y - hq.y - 1);
        }
        return null;
    }
}
