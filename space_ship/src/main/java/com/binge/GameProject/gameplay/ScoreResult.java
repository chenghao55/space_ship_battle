package com.binge.GameProject.gameplay;

public class ScoreResult {
    private final int rescuedCount;
    private final int totalCount;
    private final int lostCount;
    private final int remainingHp;
    private final double rescuedRatio;
    private final String rating;

    public ScoreResult(int rescuedCount, int totalCount, int lostCount, int remainingHp, double rescuedRatio, String rating) {
        this.rescuedCount = rescuedCount;
        this.totalCount = totalCount;
        this.lostCount = lostCount;
        this.remainingHp = remainingHp;
        this.rescuedRatio = rescuedRatio;
        this.rating = rating;
    }

    public int getRescuedCount() { return rescuedCount; }
    public int getTotalCount() { return totalCount; }
    public int getLostCount() { return lostCount; }
    public int getRemainingHp() { return remainingHp; }
    public double getRescuedRatio() { return rescuedRatio; }
    public String getRating() { return rating; }
}
