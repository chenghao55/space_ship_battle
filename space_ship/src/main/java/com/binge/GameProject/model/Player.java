package com.binge.GameProject.model;

import com.binge.GameProject.engine.GameManager;
import com.binge.GameProject.engine.InputManager;
import com.binge.GameProject.physics.Hitbox;
import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.utils.GameConfig;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

// Player 是玩家操控的太空飛船，繼承自 GameObject
public class Player extends GameObject {
    private double thrust = GameConfig.PLAYER_BASE_THRUST; // 向前推進力
    private double lateralThrust = 130.0; // 左右平移 (側滑) 的推力
    private double brakeFactor = 0.95; // 煞車力道 (每幀保留的速度比例)

    private double rotationAngle = 0; // 飛船目前的面向角度 (0度代表朝向 +Z 軸)
    private double rotationSpeed = 150.0; // 飛船轉向速度 (提升三倍)
    private Rotate rotateTransform;

    private double fireCooldown = 0.0; // 射擊冷卻計時器
    private double boostEnergy = 100.0; // Boost 能量 (滿分 100)
    private boolean isOverheating = false; // 是否在危險區過熱
    private double overheatTimer = 0.0;
    private double planetCollisionCooldown = 0.0;
    private double invincibleTimer = 0.0;
    private boolean forwardThrustActive = false;
    private int hp = 5;
    private final int maxHp = 5;
    private double driftIntensity = 0.0;
    private final RescueGroup rescueGroup = new RescueGroup();

    private GameManager gameManager; // 讓飛船可以呼叫 GameManager 來產生子彈

    // 建構子：需要傳入 GameManager 以便產生子彈
    public Player(double startX, double startY, GameManager gm) {
        this.position.set(startX, startY);
        this.gameManager = gm;

        // 嘗試使用 OBJImporter 載入戰鬥機模型
        // String modelPath = "src/com/binge/GameProject/resource/model/SpaceFighter.obj";
        javafx.scene.shape.MeshView meshView = com.binge.GameProject.utils.OBJImporter.loadModel("/model/SpaceFighter.obj");


        Group wrapper = new Group();

        if (meshView != null) {
            PhongMaterial mat = new PhongMaterial(Color.web("#4a6f8f"));
            mat.setSpecularColor(Color.web("#ffffff"));
            meshView.setMaterial(mat);

            meshView.setScaleX(0.10);
            meshView.setScaleY(0.10);
            meshView.setScaleZ(0.10);

            // 修正原始模型的朝向
            // 依照指示，將垂直建模繞垂直軸 (Y軸) 旋轉 180 度，然後再壓平
            meshView.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
            meshView.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

            // 將模型往後拉 (指出螢幕方向)，讓子彈看起來是從飛機前端射出
            meshView.setTranslateY(100);

            wrapper.getChildren().add(meshView);
        } else {
            Box box = new Box(40, 20, 80);
            PhongMaterial mat = new PhongMaterial(Color.AQUA);
            mat.setSpecularColor(Color.WHITE);
            box.setMaterial(mat);
            wrapper.getChildren().add(box);
        }

        this.view = wrapper;
        this.rotateTransform = new Rotate(0, Rotate.Y_AXIS);
        wrapper.getTransforms().add(rotateTransform);

        updateView();
    }

    @Override
    public void update(double dt) {
        InputManager input = InputManager.getInstance();
        boolean canControl = gameManager != null
                && gameManager.getCurrentState() == com.binge.GameProject.engine.GameState.PLAYING;
        forwardThrustActive = false;

        // --- 0. 狀態與能量恢復 ---
        if (boostEnergy < 100.0 && !input.isPressed("Shift")) {
            boostEnergy += 20.0 * dt; // 每秒恢復 20 點 (約 5 秒滿)
            if (boostEnergy > 100.0)
                boostEnergy = 100.0;
        }
        if (isOverheating) {
            overheatTimer -= dt;
            if (overheatTimer <= 0)
                isOverheating = false;
        }
        if (planetCollisionCooldown > 0) {
            planetCollisionCooldown -= dt;
        }
        if (invincibleTimer > 0) {
            invincibleTimer -= dt;
            if (view != null) view.setOpacity((Math.sin(invincibleTimer * 34.0) > 0) ? 0.42 : 1.0);
        } else if (view != null && view.getOpacity() != 1.0) {
            view.setOpacity(1.0);
        }

        // --- 1. 處理轉向 (A / D) ---
        if (canControl) {
            double turnAmount = 0.0;
            if (input.isPressed("A") || input.isPressed("LEFT")) {
                turnAmount -= rotationSpeed * dt; // 往左轉
            }
            if (input.isPressed("D") || input.isPressed("RIGHT")) {
                turnAmount += rotationSpeed * dt; // 往右轉
            }

            if (Math.abs(turnAmount) > 0.0001) {
                rotationAngle += turnAmount;
                if (velocity.magnitudeSquared() > 1.0) {
                    double mag = Math.sqrt(velocity.magnitudeSquared());
                    double rad = Math.toRadians(rotationAngle);
                    velocity.set(mag * Math.sin(rad), mag * Math.cos(rad));
                }
            }
        }

        rotationAngle = rotationAngle % 360;

        // --- 2. 處理推進 (W, Shift) 與 煞車 (S) ---
        double currentThrust = thrust;
        boolean isBoosting = canControl && input.isPressed("Shift") && boostEnergy > 0;
        if (isBoosting) {
            currentThrust *= GameConfig.PLAYER_BOOST_MULTIPLIER; // 約增加 66%
            boostEnergy -= 20.0 * dt; // 每秒消耗 20 點
            if (boostEnergy < 0)
                boostEnergy = 0;
        }

        if (canControl) {
            if (input.isPressed("W") || input.isPressed("UP")) {
                double rad = Math.toRadians(rotationAngle);
                acceleration.addMut(new Vector2D(Math.sin(rad), Math.cos(rad)).multiply(currentThrust));
                forwardThrustActive = true;
            }
            if (input.isPressed("S") || input.isPressed("DOWN")) {
                velocity.multiplyMut(brakeFactor);
            }
        }

        // --- 3. 機頭永遠朝向速度方向 ---
        // 當推進、碰撞或邊界回推改變速度方向時，機頭（與攝影機）必須瞬間跟隨切換！
        if (velocity.magnitudeSquared() > 1.0) {
            double oldAngle = rotationAngle;
            rotationAngle = Math.toDegrees(Math.atan2(velocity.x, velocity.y));
            double diff = Math.abs(rotationAngle - oldAngle);
            while (diff > 180) diff -= 360;
            driftIntensity = Math.min(1.0, Math.abs(diff) / 90.0);
        } else {
            driftIntensity = Math.max(0, driftIntensity - dt * 2.0);
        }

        // 更新畫面旋轉
        rotateTransform.setAngle(rotationAngle);

        // --- 4. 處理射擊 (E 或 滑鼠左鍵) ---
        if (fireCooldown > 0) {
            fireCooldown -= dt;
        }

        if (canControl) {
            boolean isFiring = input.isPressed("E") || input.isMousePressed(javafx.scene.input.MouseButton.PRIMARY);
            if (isFiring && fireCooldown <= 0) {
                fireBullet();
                fireCooldown = 0.15;
            }
        }
    }

    // 發射子彈的邏輯 (雙管雷射)
    private void fireBullet() {
        if (gameManager != null) {
            if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                com.binge.GameProject.audio.AudioSystem.getInstance().playSpatialClip(
                    com.binge.GameProject.audio.AudioSystem.getInstance().getLaserSfx(),
                    position,
                    this,
                    1.0
                );
            }
            double rad = Math.toRadians(rotationAngle);
            Vector2D forward = new Vector2D(Math.sin(rad), Math.cos(rad));
            Vector2D right = new Vector2D(Math.cos(rad), -Math.sin(rad)); // 右側向量

            // 左右兩側的發射點 (距離中心 20 單位，往前 80 單位，對齊建模機頭)
            Vector2D spawnLeft = position.add(forward.multiply(80)).add(right.multiply(-20));
            Vector2D spawnRight = position.add(forward.multiply(80)).add(right.multiply(20));

            // 確保子彈速度遠大於飛機本身速度 (包含 boost)
            Vector2D bulletVelLeft = velocity.add(forward.multiply(3000));
            Vector2D bulletVelRight = velocity.add(forward.multiply(3000));

            PlayerBullet b1 = new PlayerBullet(spawnLeft.x, spawnLeft.y, bulletVelLeft);
            PlayerBullet b2 = new PlayerBullet(spawnRight.x, spawnRight.y, bulletVelRight);

            gameManager.addGameObject(b1);
            gameManager.addGameObject(b2);
        }
    }

    // 當飛船進入危險區時由 PhysicsEngine 呼叫
    public void takeDangerDamage(double dt) {
        this.isOverheating = true;
        this.overheatTimer = 0.2; // 維持 0.2 秒過熱狀態
    }

    public boolean takePlanetCollisionDamage() {
        if (planetCollisionCooldown > 0) return false;
        if (!tryTakeDamage(1)) return false;
        planetCollisionCooldown = 1.2;
        this.isOverheating = true;
        this.overheatTimer = 0.45;
        return true;
    }

    public boolean isBoosting() {
        return InputManager.getInstance().isPressed("Shift") && boostEnergy > 0;
    }

    public boolean isForwardThrustActive() {
        return forwardThrustActive;
    }

    public void enforceFlightSpeedLimits() {
        double speed = velocity.magnitude();
        if (speed <= 0.0001) return;

        if (forwardThrustActive && speed < GameConfig.PLAYER_MIN_FLIGHT_SPEED) {
            velocity.set(velocity.normalize().multiply(GameConfig.PLAYER_MIN_FLIGHT_SPEED));
            return;
        }

        if (speed > GameConfig.PLAYER_MAX_FLIGHT_SPEED) {
            velocity.set(velocity.normalize().multiply(GameConfig.PLAYER_MAX_FLIGHT_SPEED));
        }
    }

    public double getBoostEnergy() {
        return boostEnergy;
    }

    public boolean isOverheating() {
        return isOverheating;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public boolean tryTakeDamage(int amount) {
        if (invincibleTimer > 0) return false;
        hp = Math.max(0, hp - amount);
        invincibleTimer = GameConfig.PLAYER_INVINCIBLE_SECONDS;
        return true;
    }

    public void takeDamage(int amount) {
        tryTakeDamage(amount);
    }

    public boolean isInvincible() {
        return invincibleTimer > 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public double getDriftIntensity() {
        return driftIntensity;
    }

    public RescueGroup getRescueGroup() {
        return rescueGroup;
    }

    public List<Hitbox> getFullBodyHitboxes() {
        List<Hitbox> hitboxes = new ArrayList<>();
        hitboxes.add(new Hitbox(new Vector2D(position.x, position.y), 34));
        hitboxes.addAll(rescueGroup.getHitboxes());
        return hitboxes;
    }

    public List<Hitbox> getRescueHitboxes() {
        return getFullBodyHitboxes();
    }
}
