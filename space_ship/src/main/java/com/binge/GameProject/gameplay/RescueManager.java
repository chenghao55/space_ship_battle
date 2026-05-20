package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolState;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Hitbox;

import java.util.List;

public class RescueManager {
    private String lastEventText = "";
    private double lastEventTimer = 0;

    public void update(Player player, List<Idol> idols, double dt) {
        lastEventTimer = Math.max(0, lastEventTimer - dt);
        player.getRescueGroup().update(player, dt);

        for (Idol idol : idols) {
            if (!idol.isAvailable()) continue;
            double distance = idol.getPosition().distance(player.getPosition());
            if (distance <= idol.getDetectRadius()) {
                idol.detect();
                idol.setVolume(Math.min(1.0, (idol.getDetectRadius() - distance) / idol.getDetectRadius()));
            }

            if (idol.getState() == IdolState.DETECTED) {
                for (Hitbox hitbox : player.getRescueHitboxes()) {
                    if (hitbox.intersects(idol.getPosition(), 18)) {
                        idol.rescue();
                        player.getRescueGroup().add(idol);
                        lastEventText = "RESCUED: " + idol.getDisplayName();
                        lastEventTimer = 2.0;
                        break;
                    }
                }
            }
        }
    }

    public Idol loseLastRescued(Player player) {
        Idol lost = player.getRescueGroup().removeLastAsLost();
        if (lost != null) {
            lastEventText = "IDOL LOST: " + lost.getDisplayName();
            lastEventTimer = 2.5;
        }
        return lost;
    }

    public String getEventText() {
        return lastEventTimer > 0 ? lastEventText : "";
    }
}
