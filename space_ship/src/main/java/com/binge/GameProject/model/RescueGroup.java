package com.binge.GameProject.model;

import com.binge.GameProject.physics.Hitbox;
import com.binge.GameProject.physics.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RescueGroup {
    private final List<Idol> rescuedIdols = new ArrayList<>();
    private final List<Vector2D> trailPositions = new ArrayList<>();

    public void add(Idol idol) {
        rescuedIdols.add(idol);
        trailPositions.add(new Vector2D(idol.getPosition().x, idol.getPosition().y));
    }

    public void update(Player player, double dt) {
        Vector2D anchor = player.getPosition();
        for (int i = 0; i < trailPositions.size(); i++) {
            Vector2D current = trailPositions.get(i);
            Vector2D target = i == 0 ? anchor : trailPositions.get(i - 1);
            double follow = Math.min(1.0, dt * 8.0);
            double desiredGap = 54.0;
            Vector2D diff = current.subtract(target);
            if (diff.magnitude() > desiredGap) {
                Vector2D desired = target.add(diff.normalize().multiply(desiredGap));
                current.x += (desired.x - current.x) * follow;
                current.y += (desired.y - current.y) * follow;
            }

            Idol idol = rescuedIdols.get(i);
            idol.getPosition().set(current);
            idol.updateView();
        }
    }

    public Idol removeLastAsLost() {
        if (rescuedIdols.isEmpty()) return null;
        int last = rescuedIdols.size() - 1;
        trailPositions.remove(last);
        Idol idol = rescuedIdols.remove(last);
        idol.markLost();
        return idol;
    }

    public List<Hitbox> getHitboxes() {
        List<Hitbox> hitboxes = new ArrayList<>();
        for (Vector2D position : trailPositions) {
            hitboxes.add(new Hitbox(new Vector2D(position.x, position.y), 26));
        }
        return hitboxes;
    }

    public List<Idol> getRescuedIdols() {
        return Collections.unmodifiableList(rescuedIdols);
    }

    public int size() {
        return rescuedIdols.size();
    }

    public void clear() {
        rescuedIdols.clear();
        trailPositions.clear();
    }
}
