package com.binge.GameProject.audio;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.Player;

public class IdolVoiceController {
    public double calculateVolume(Idol idol, Player player) {
        if (idol == null || player == null) return 0.0;
        double distance = idol.getPosition().distance(player.getPosition());
        return calculateRadarPulseAmplitude(distance);
    }

    public double calculateRadarPulseAmplitude(double distance) {
        double closeness = Math.max(0.0, 1.0 - Math.min(1.0, distance / 3000.0));
        return Math.max(0.0, Math.min(1.0, 0.2 + closeness * 0.8));
    }
}
