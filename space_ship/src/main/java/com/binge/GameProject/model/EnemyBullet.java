package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.utils.GameConfig;
import javafx.scene.paint.Color;

public class EnemyBullet extends Bullet {
    public EnemyBullet(double x, double y, Vector2D velocity) {
        super(x, y, velocity, BulletOwner.ENEMY, Color.web("#ff4466"), GameConfig.ENEMY_BULLET_RADIUS, 4.0);
    }
}
