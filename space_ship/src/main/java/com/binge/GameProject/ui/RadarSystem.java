package com.binge.GameProject.ui;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.Player;

public class RadarSystem {
    public PulseData calculatePulse(Player player, Idol idol) {
        if (player == null || idol == null) return new PulseData(0, Double.MAX_VALUE, 0, 0);
        double dx = idol.getPosition().x - player.getPosition().x;
        double dy = idol.getPosition().y - player.getPosition().y;
        double angle = Math.toDegrees(Math.atan2(dx, dy)) - player.getRotationAngle();
        double distance = idol.getPosition().distance(player.getPosition());
        double closeness = Math.max(0, 1.0 - Math.min(1.0, distance / 3000.0));
        return new PulseData(angle, distance, 1.5 + closeness * 5.5, 0.2 + closeness * 0.8);
    }

    public record RadarThreat(double angle, double distance, double intensity) {}
    public record PulseData(double angle, double distance, double pulseSpeed, double amplitude) {}
}
