package com.binge.GameProject.audio;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.Player;

public class IdolVoiceController {
    private static final double RADAR_SENSE_RANGE = 9000.0;// 1800.0原數據*5

    public double calculateVolume(Idol idol, Player player) {
        if (idol == null || player == null) return 0.0;
        double distance = idol.getPosition().distance(player.getPosition());
        return calculateRadarPulseAmplitude(distance);
    }

    public double calculateRadarPulseAmplitude(double distance) {
        if (distance >= RADAR_SENSE_RANGE) return 0.0;
        return Math.max(0.0, Math.min(1.0, 1.0 - distance / RADAR_SENSE_RANGE));
    }
}
