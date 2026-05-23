package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.utils.GameConfig;
import javafx.scene.paint.Color;

public class MobileEnemy extends Enemy {
    private static final double MOBILE_AGGRO_RADIUS = TURRET_AGGRO_RADIUS * 6.0;
    private final Vector2D spawnPosition;
    private final Vector2D movementVelocity = new Vector2D();
    private double patrolAngle;

    public MobileEnemy(double x, double y) {
        super(x, y, MOBILE_AGGRO_RADIUS, Color.web("#35ff74"), true,
                GameConfig.MOVING_ENEMY_MOVE_SPEED, GameConfig.MOVING_ENEMY_PREFERRED_DISTANCE);
        this.spawnPosition = new Vector2D(x, y);
        this.patrolAngle = Math.atan2(y, x);
    }

    @Override
    public void updateMovement(Player player, double dt) {
        if (!isAlive() || player == null) return;

        double playerDistance = getPosition().distance(player.getPosition());
        Vector2D target;
        if (playerDistance <= GameConfig.MOVING_ENEMY_CHASE_RADIUS) {
            target = player.getPosition().add(playerForward(player).multiply(GameConfig.MOVING_ENEMY_PREFERRED_DISTANCE));
        } else {
            patrolAngle += 0.42 * dt;
            target = spawnPosition.add(new Vector2D(Math.cos(patrolAngle), Math.sin(patrolAngle))
                    .multiply(GameConfig.MOVING_ENEMY_PATROL_RADIUS));
        }

        Vector2D toTarget = target.subtract(getPosition());
        Vector2D desiredVelocity = toTarget.magnitudeSquared() < 1.0
                ? new Vector2D()
                : toTarget.normalize().multiply(GameConfig.MOVING_ENEMY_MOVE_SPEED);
        double lerp = 1.0 - Math.pow(0.08, dt * 60.0);
        movementVelocity.x += (desiredVelocity.x - movementVelocity.x) * lerp;
        movementVelocity.y += (desiredVelocity.y - movementVelocity.y) * lerp;
        getPosition().addMut(movementVelocity.multiply(dt));
        updateView();
    }

    private Vector2D playerForward(Player player) {
        double rad = Math.toRadians(player.getRotationAngle());
        return new Vector2D(Math.sin(rad), Math.cos(rad));
    }

    @Override
    protected double getShootCooldownDuration() {
        return GameConfig.MOVING_ENEMY_SHOOT_COOLDOWN;
    }
}
