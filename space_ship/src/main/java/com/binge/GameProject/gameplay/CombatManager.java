package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Bullet;
import com.binge.GameProject.model.BulletOwner;
import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.EnemyBullet;
import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Hitbox;
import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.rendering.CameraManager;

import java.util.List;

public class CombatManager {
    private static final double ENEMY_BULLET_SPEED = 1350.0;
    private double invulnerabilityTimer;
    private String warningText = "";
    private double warningTimer;

    public void update(Player player, List<Enemy> enemies, List<GameObject> dynamicObjects, List<GameObject> staticObjects,
                       double dt, GameObjectAdder adder, DamageCallback damageCallback, CameraManager cameraManager) {
        invulnerabilityTimer = Math.max(0, invulnerabilityTimer - dt);
        warningTimer = Math.max(0, warningTimer - dt);

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            double distance = enemy.getPosition().distance(player.getPosition());
            if (distance <= enemy.getAggroRadius() && enemy.canShoot()) {
                Vector2D direction = calculatePredictiveAim(enemy, player, distance);
                adder.add(new EnemyBullet(enemy.getPosition().x, enemy.getPosition().y, direction.multiply(ENEMY_BULLET_SPEED)));
                enemy.playMuzzleFlash();
                enemy.resetShootCooldown();
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playSpatialClip(
                        com.binge.GameProject.audio.AudioSystem.getInstance().getLaserSfx(),
                        enemy.getPosition(),
                        player,
                        0.7
                    );
                }
            }

            if (distance < 55) {
                enemy.takeDamage(99);
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playHit();
                    com.binge.GameProject.audio.AudioSystem.getInstance().playSpatialClip(
                        com.binge.GameProject.audio.AudioSystem.getInstance().getExplosionSfx(),
                        enemy.getPosition(),
                        player,
                        1.0
                    );
                }
                damagePlayer(player, damageCallback, cameraManager, "COLLISION DAMAGE");
            }
        }

        for (GameObject object : dynamicObjects) {
            if (!(object instanceof Bullet bullet) || bullet.isDead()) continue;

            for (GameObject staticObject : staticObjects) {
                if (staticObject instanceof Planet planet) {
                    double distance = bullet.getPosition().distance(planet.getPosition());
                    if (distance <= planet.getRadius() + bullet.getRadius()) {
                        bullet.setDead(true);
                        warningText = "BULLET SPARKED ON PLANET / STAR";
                        warningTimer = 0.8;
                        break;
                    }
                }
            }
            if (bullet.isDead()) continue;

            if (bullet.getOwner() == BulletOwner.PLAYER) {
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    if (enemy.getPosition().distance(bullet.getPosition()) < 48) {
                        enemy.takeDamage(1);
                        bullet.setDead(true);
                        if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                            com.binge.GameProject.audio.AudioSystem.getInstance().playSpatialClip(
                                com.binge.GameProject.audio.AudioSystem.getInstance().getHitSfx(),
                                enemy.getPosition(),
                                player,
                                0.8
                            );
                            if (!enemy.isAlive()) {
                                com.binge.GameProject.audio.AudioSystem.getInstance().playSpatialClip(
                                    com.binge.GameProject.audio.AudioSystem.getInstance().getExplosionSfx(),
                                    enemy.getPosition(),
                                    player,
                                    1.0
                                );
                            }
                        }
                        warningText = "ENEMY HIT";
                        warningTimer = 0.8;
                        break;
                    }
                }
            } else {
                for (Hitbox hitbox : player.getFullBodyHitboxes()) {
                    if (hitbox.intersects(bullet.getPosition(), bullet.getRadius())) {
                        bullet.setDead(true);
                        if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                            com.binge.GameProject.audio.AudioSystem.getInstance().playHit();
                        }
                        damagePlayer(player, damageCallback, cameraManager, "INCOMING HIT");
                        break;
                    }
                }
            }
        }
    }

    private Vector2D calculatePredictiveAim(Enemy enemy, Player player, double distance) {
        double leadTime = Math.min(1.35, distance / ENEMY_BULLET_SPEED);
        Vector2D predicted = player.getPosition().add(player.getVelocity().multiply(leadTime));
        Vector2D direction = predicted.subtract(enemy.getPosition()).normalize();
        if (direction.magnitudeSquared() == 0) {
            return player.getPosition().subtract(enemy.getPosition()).normalize();
        }
        return direction;
    }

    private void damagePlayer(Player player, DamageCallback damageCallback, CameraManager cameraManager, String warning) {
        if (invulnerabilityTimer > 0) return;
        player.takeDamage(1);
        damageCallback.onDamage();
        if (cameraManager != null) cameraManager.addCameraShake(16, 0.35);
        invulnerabilityTimer = 1.0;
        warningText = warning;
        warningTimer = 1.8;
    }

    public String getWarningText() {
        return warningTimer > 0 ? warningText : "";
    }

    public interface GameObjectAdder {
        void add(GameObject object);
    }

    public interface DamageCallback {
        void onDamage();
    }
}
