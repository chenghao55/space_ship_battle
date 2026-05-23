package com.binge.GameProject.gameplay;

public class ScoreResult {
    private final int totalRescueTargetCount;
    private final int rescuedCount;
    private final int lostRescueCount;
    private final int aliveRescuedCount;
    private final int enemyKillCount;
    private final int playerHp;
    private final double rescueRate;
    private final double totalScore;
    private final String rating;

    public ScoreResult(int totalRescueTargetCount, int rescuedCount, int lostRescueCount, int aliveRescuedCount,
                       int enemyKillCount, int playerHp, double rescueRate, double totalScore, String rating) {
        this.totalRescueTargetCount = totalRescueTargetCount;
        this.rescuedCount = rescuedCount;
        this.lostRescueCount = lostRescueCount;
        this.aliveRescuedCount = aliveRescuedCount;
        this.enemyKillCount = enemyKillCount;
        this.playerHp = playerHp;
        this.rescueRate = rescueRate;
        this.totalScore = totalScore;
        this.rating = rating;
    }

    public int getTotalRescueTargetCount() { return totalRescueTargetCount; }
    public int getRescuedCount() { return rescuedCount; }
    public int getLostRescueCount() { return lostRescueCount; }
    public int getAliveRescuedCount() { return aliveRescuedCount; }
    public int getEnemyKillCount() { return enemyKillCount; }
    public int getPlayerHp() { return playerHp; }
    public double getRescueRate() { return rescueRate; }
    public double getTotalScore() { return totalScore; }
    public String getRating() { return rating; }

    public int getTotalCount() { return totalRescueTargetCount; }
    public int getLostCount() { return lostRescueCount; }
    public int getRemainingHp() { return playerHp; }
    public double getRescuedRatio() { return rescueRate; }
}
