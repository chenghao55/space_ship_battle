package com.binge.GameProject.gameplay;

public class ScoreManager {
    public ScoreResult calculate(int totalRescueTargetCount, int rescuedCount, int lostRescueCount,
                                 int aliveRescuedCount, int enemyKillCount, int playerHp) {
        double rescueRate = totalRescueTargetCount == 0 ? 0 : (double) rescuedCount / totalRescueTargetCount;
        double rescueScore = rescueRate * 60.0;
        double aliveScore = Math.min(aliveRescuedCount * 5.0, 25.0);
        double killScore = Math.min(enemyKillCount * 3.0, 15.0);
        double hpPenalty = hpPenalty(playerHp);
        double totalScore = clamp(rescueScore + aliveScore + killScore - hpPenalty, 0.0, 100.0);
        return new ScoreResult(totalRescueTargetCount, rescuedCount, lostRescueCount, aliveRescuedCount,
                enemyKillCount, playerHp, rescueRate, totalScore, ratingFor(totalScore));
    }

    private double hpPenalty(int playerHp) {
        if (playerHp <= 0) return 20.0;
        if (playerHp == 1) return 8.0;
        if (playerHp == 2) return 4.0;
        return 0.0;
    }

    private String ratingFor(double totalScore) {
        if (totalScore >= 95) return "S";
        if (totalScore >= 80) return "A";
        if (totalScore >= 60) return "B";
        if (totalScore >= 20) return "C";
        return "F";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
