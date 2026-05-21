package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolState;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Hitbox;

import java.util.List;

public class RescueManager {
    private static final double IDOL_BODY_RADIUS = 18.0;
    private static final double RESCUE_ATTRACTION_EXTRA_RADIUS = 52.0;
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

            if (idol.getState() == IdolState.HIDDEN && isWithinAttractionRange(player, idol)) {
                idol.detect();
            }

            if (idol.getState() == IdolState.DETECTED) {
                for (Hitbox hitbox : player.getRescueHitboxes()) {
                    if (isRescueTriggered(hitbox, idol)) {
                        rescue(player, idol);
                        break;
                    }
                }
            }
        }
    }

    private boolean isWithinAttractionRange(Player player, Idol idol) {
        for (Hitbox hitbox : player.getRescueHitboxes()) {
            if (isRescueTriggered(hitbox, idol)) return true;
        }
        return false;
    }

    private boolean isRescueTriggered(Hitbox hitbox, Idol idol) {
        return hitbox.intersects(idol.getPosition(), IDOL_BODY_RADIUS + RESCUE_ATTRACTION_EXTRA_RADIUS);
    }

    private void rescue(Player player, Idol idol) {
        idol.rescue();
        player.getRescueGroup().add(idol);
        lastEventText = "RESCUED: " + idol.getDisplayName();
        lastEventTimer = 2.0;
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
