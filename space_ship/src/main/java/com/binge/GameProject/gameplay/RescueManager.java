package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Hitbox;

import java.util.List;

public class RescueManager {
    private static final double IDOL_BODY_RADIUS = 53.0;
    private static final double RESCUE_ATTRACTION_EXTRA_RADIUS = 84.0;
    private String lastEventText = "";
    private double lastEventTimer = 0;
    private int totalRescuedCount;

    public void reset() {
        lastEventText = "";
        lastEventTimer = 0;
        totalRescuedCount = 0;
    }

    public void update(Player player, List<Idol> idols, double dt) {
        lastEventTimer = Math.max(0, lastEventTimer - dt);
        player.getRescueGroup().update(player, dt);

        for (Idol idol : idols) {
            idol.updateBillboardFacing(player.getRotationAngle(), dt);
            if (!idol.isAvailable()) continue;
            double distance = idol.getPosition().distance(player.getPosition());
            idol.setVolume(Math.max(0.0, 1.0 - distance / 9000.0));
            for (Hitbox hitbox : player.getRescueHitboxes()) {
                if (isRescueTriggered(hitbox, idol)) {
                    rescue(player, idol);
                    break;
                }
            }
        }
    }

    private boolean isRescueTriggered(Hitbox hitbox, Idol idol) {
        return hitbox.intersects(idol.getPosition(), IDOL_BODY_RADIUS + RESCUE_ATTRACTION_EXTRA_RADIUS);
    }

    private void rescue(Player player, Idol idol) {
        idol.rescue();
        player.getRescueGroup().add(idol);
        totalRescuedCount++;
        lastEventText = "RESCUED: " + idol.getDisplayName();
        lastEventTimer = 2.0;
        if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
            com.binge.GameProject.audio.AudioSystem.getInstance().playPickup();
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

    public int getTotalRescuedCount() {
        return totalRescuedCount;
    }
}
