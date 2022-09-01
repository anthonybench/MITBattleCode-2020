# **MIT BattleCode 2020**
*My team's submission to the Portland State University mock BattleCode competition.*

<br />

## **Mechanical Overview**
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.

<br /><hr>

## **Robot Logic**
The path finding and game mechanic logic for our robot is found in `src/FunkBot/RobotPlayer.java`.

In this game, the objective is to acquire **soup** (the fundamental resource of the game) in one of several game maps, and spend soup to build robots from the selection found in `src/redemptionplayer/`.

The format is "team versus team", with the winner being whoever destroys the opposing HQ first!