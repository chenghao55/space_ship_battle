package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import javafx.scene.paint.Color;

public class PlayerBullet extends Bullet {
    public PlayerBullet(double x, double y, Vector2D velocity) {
        super(x, y, velocity, BulletOwner.PLAYER, Color.web("#00ffff"), 5.0, 2.0);
    }
}
