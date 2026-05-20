package com.binge.GameProject.gameplay;

public class ScoreManager {
    public ScoreResult calculate(int rescuedCount, int totalCount, int lostCount, int remainingHp) {
        double ratio = totalCount == 0 ? 0 : (double) rescuedCount / totalCount;
        String rating;
        if (remainingHp <= 0 || rescuedCount == 0) {
            rating = "F";
        } else if (ratio >= 1.0 && remainingHp == 5) {
            rating = "S";
        } else if ((ratio >= 1.0 && remainingHp >= 3) || (ratio >= 0.8 && remainingHp >= 4)) {
            rating = "A";
        } else if ((ratio >= 0.8 && remainingHp >= 1) || (ratio >= 0.5 && remainingHp >= 3)) {
            rating = "B";
        } else {
            rating = "C";
        }
        return new ScoreResult(rescuedCount, totalCount, lostCount, remainingHp, ratio, rating);
    }
}
