package com.binge.GameProject.model;

import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.rendering.EnemyDeathExplosion;
import com.binge.GameProject.utils.GameConfig;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class Enemy extends GameObject {
    private int hp = 3;
    public static final double TURRET_AGGRO_RADIUS = 1650.0;
    private final double aggroRadius;
    private final boolean mobile;
    private final double movementSpeed;
    private final double stopDistance;
    private double shootCooldown = 1.0;
    private boolean alive = true;
    private boolean isDestroyed;
    private boolean isDying;
    private double deathFlashTimer;
    private final EnemyView enemyView;
    private final Group root = new Group();
    private EnemyDeathExplosion deathExplosion;

    public Enemy(double x, double y) {
        this(x, y, TURRET_AGGRO_RADIUS, Color.web("#ff3355"), false, 0.0, 0.0);
    }

    protected Enemy(double x, double y, double aggroRadius, Color coreColor, boolean mobile,
                    double movementSpeed, double stopDistance) {
        this.position.set(x, y);
        this.aggroRadius = aggroRadius;
        this.mobile = mobile;
        this.movementSpeed = movementSpeed;
        this.stopDistance = stopDistance;
        this.enemyView = new EnemyView(aggroRadius, coreColor);
        root.getChildren().add(enemyView.getRoot());
        this.view = root;
        updateView();
    }

    @Override
    public void update(double dt) {
        shootCooldown -= dt;
        enemyView.update(dt);
        if (isDying) {
            deathFlashTimer -= dt;
            if (deathFlashTimer <= 0) {
                enemyView.setBodyVisible(false);
            }
            if (deathExplosion != null) {
                deathExplosion.update(dt);
                if (deathExplosion.isFinished()) {
                    onDeathExplosionFinished();
                }
            }
        }
    }

    public void updateMovement(Player player, double dt) {
        if (!alive || !mobile || player == null) return;
        double distance = position.distance(player.getPosition());
        if (distance <= stopDistance) return;
        var direction = player.getPosition().subtract(position).normalize();
        position.addMut(direction.multiply(movementSpeed * dt));
        updateView();
    }

    public boolean canShoot() {
        return alive && !isDying && !isDestroyed && shootCooldown <= 0;
    }

    public void resetShootCooldown() {
        shootCooldown = getShootCooldownDuration();
        enemyView.playChargeFlash();
    }

    protected double getShootCooldownDuration() {
        return GameConfig.TURRET_SHOOT_COOLDOWN;
    }

    public boolean takeDamage(int amount) {
        if (!alive || isDying || isDestroyed) return false;
        hp -= amount;
        enemyView.playHitFlash();
        if (hp <= 0) {
            destroy();
            return true;
        }
        return false;
    }

    public void destroy() {
        if (isDying || isDestroyed) return;
        alive = false;
        isDying = true;
        deathFlashTimer = 0.10;
        enemyView.playDeathFlash();
        deathExplosion = new EnemyDeathExplosion(new Vector2D(0, 0));
        root.getChildren().add(deathExplosion.getView());
    }

    public void onDeathExplosionFinished() {
        isDying = false;
        isDestroyed = true;
        isDead = true;
    }

    public void playMuzzleFlash() {
        enemyView.playMuzzleFlash();
    }

    public double getAggroRadius() { return aggroRadius; }
    public boolean isMobile() { return mobile; }
    public double getStopDistance() { return stopDistance; }
    public boolean isAlive() { return alive; }
    public boolean isDestroyed() { return isDestroyed; }
    public boolean isDying() { return isDying; }
}
