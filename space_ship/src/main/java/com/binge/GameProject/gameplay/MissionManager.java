package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Idol;

import java.util.List;

public class MissionManager {
    private boolean finalRescueTriggered;

    public void reset() {
        finalRescueTriggered = false;
    }

    public boolean shouldStartBulletTime(List<Idol> idols, int hp) {
        if (finalRescueTriggered || hp <= 0 || idols.isEmpty()) return false;
        for (Idol idol : idols) {
            if (idol.isAvailable()) return false;
        }
        finalRescueTriggered = true;
        return true;
    }

    public boolean isFinalRescueTriggered() {
        return finalRescueTriggered;
    }
}
