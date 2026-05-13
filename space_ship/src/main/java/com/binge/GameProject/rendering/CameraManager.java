package com.binge.GameProject.rendering;

import com.binge.GameProject.engine.InputManager;
import com.binge.GameProject.model.Player;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

// CameraManager 負責控制遊戲中的攝影機，讓它平滑跟隨飛船 (第三人稱視角)
public class CameraManager {
    private PerspectiveCamera camera;
    
    // 攝影機架構變形器
    private Translate pivotTranslate = new Translate();
    private Rotate pivotRotateY = new Rotate(0, Rotate.Y_AXIS);
    private Rotate rollRotateZ = new Rotate(0, Rotate.Z_AXIS);
    private Rotate pitchRotateX = new Rotate(-10, Rotate.X_AXIS); // 微微向下俯視 10 度
    private Translate cameraOffset = new Translate(0, -80, -450); // 位於飛船後方 450，上方 80

    // 平滑插值的狀態變數
    private double currentX = 0, currentZ = 0;
    private double currentYRot = 0;
    private double currentRoll = 0;
    private double currentFOV = 60.0;
    private boolean isFirstFrame = true;
    
    // 攝影機震動變數
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    
    // 主選單與過場狀態
    private boolean menuMode = true;
    private boolean transitioning = false;
    private double menuOrbitAngle = 0;
    private Runnable onTransitionEnd;

    public CameraManager(PerspectiveCamera camera) {
        this.camera = camera;
        this.camera.setNearClip(1.0);
        this.camera.setFarClip(50000.0);
        
        // 按照層級套用變形：先移動到追蹤點 -> 水平旋轉 -> 滾轉 -> 俯仰 -> 推遠鏡頭
        this.camera.getTransforms().addAll(pivotTranslate, pivotRotateY, rollRotateZ, pitchRotateX, cameraOffset);
    }

    public void update(Player player, double dt) {
        if (player == null) return;
        
        double targetX = player.getPosition().x;
        double targetZ = player.getPosition().y;
        double targetYRot = player.getRotationAngle();
        
        if (isFirstFrame) {
            currentX = targetX;
            currentZ = targetZ;
            currentYRot = targetYRot;
            isFirstFrame = false;
        }
        
        if (transitioning) {
            // 過場動畫：急速向前推進 (FOV 飆升，模擬發射升空)
            double targetFOV = 130.0;
            currentFOV += (targetFOV - currentFOV) * 2.0 * dt;
            camera.setFieldOfView(currentFOV);
            
            // 拉近鏡頭
            cameraOffset.setZ(cameraOffset.getZ() + (-50 - cameraOffset.getZ()) * 3.0 * dt);
            
            // 跟隨玩家位置 (不延遲)
            pivotTranslate.setX(targetX);
            pivotTranslate.setZ(targetZ);
            
            if (Math.abs(currentFOV - targetFOV) < 2.0) {
                transitioning = false;
                // 重置為正常遊玩狀態
                cameraOffset.setZ(-450);
                cameraOffset.setY(-80);
                if (onTransitionEnd != null) onTransitionEnd.run();
            }
            return;
        }
        
        if (menuMode) {
            // 主選單模式：鏡頭拉遠並圍繞玩家緩慢公轉
            menuOrbitAngle += 2.0 * dt; // 緩慢自轉
            pivotRotateY.setAngle(menuOrbitAngle);
            
            // 鎖定目標位置
            currentX += (targetX - currentX) * 2.0 * dt;
            currentZ += (targetZ - currentZ) * 2.0 * dt;
            pivotTranslate.setX(currentX);
            pivotTranslate.setZ(currentZ);
            
            // 拉遠鏡頭
            cameraOffset.setZ(-1500);
            cameraOffset.setY(-300);
            camera.setFieldOfView(60.0);
            return;
        }

        // 以下為正常 PLAYING 狀態的攝影機邏輯
        
        // 平滑恢復 camera offset
        cameraOffset.setZ(cameraOffset.getZ() + (-450 - cameraOffset.getZ()) * 5.0 * dt);
        cameraOffset.setY(cameraOffset.getY() + (-80 - cameraOffset.getY()) * 5.0 * dt);

        // 1. 位置平滑跟隨 (Lerp)
        double posLerpSpeed = 10.0;
        currentX += (targetX - currentX) * posLerpSpeed * dt;
        currentZ += (targetZ - currentZ) * posLerpSpeed * dt;
        pivotTranslate.setX(currentX);
        pivotTranslate.setZ(currentZ);

        // 2. 旋轉平滑跟隨 (Slerp 模擬)
        double rotDiff = targetYRot - currentYRot;
        while (rotDiff < -180) rotDiff += 360;
        while (rotDiff > 180) rotDiff -= 360;
        
        double rotLerpSpeed = 12.0;
        currentYRot += rotDiff * rotLerpSpeed * dt;
        pivotRotateY.setAngle(currentYRot);

        // 3. 動態轉向傾斜 (Roll)
        InputManager input = InputManager.getInstance();
        double targetRoll = 0;
        if (input.isPressed("A")) targetRoll = -15.0; // 向左傾斜
        if (input.isPressed("D")) targetRoll = 15.0;  // 向右傾斜
        
        double rollLerpSpeed = 8.0;
        currentRoll += (targetRoll - currentRoll) * rollLerpSpeed * dt;
        rollRotateZ.setAngle(currentRoll);

        // 4. 動態視野角 (Dynamic FOV) - Boost 時拉伸畫面增強速度感
        double targetFOV = player.isBoosting() ? 95.0 : 60.0;
        double fovLerpSpeed = player.isBoosting() ? 5.0 : 3.0;
        currentFOV += (targetFOV - currentFOV) * fovLerpSpeed * dt;
        camera.setFieldOfView(currentFOV);
        
        // 5. 處理攝影機震動 (Camera Shake)
        if (shakeDuration > 0) {
            double offsetX = (Math.random() - 0.5) * 2 * shakeIntensity;
            double offsetZ = (Math.random() - 0.5) * 2 * shakeIntensity;
            pivotTranslate.setX(currentX + offsetX);
            pivotTranslate.setZ(currentZ + offsetZ);
            
            shakeDuration -= dt;
            if (shakeDuration <= 0) {
                shakeIntensity = 0;
            } else {
                // 震動隨時間衰減
                shakeIntensity *= 0.95;
            }
        }
    }
    
    // 觸發攝影機震動
    public void addCameraShake(double intensity, double duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
    }
    
    // 設定是否為主選單模式
    public void setMenuMode(boolean menuMode) {
        this.menuMode = menuMode;
    }
    
    // 播放進入遊戲的電影級過場運鏡
    public void playStartTransition(Runnable onFinished) {
        this.menuMode = false;
        this.transitioning = true;
        this.onTransitionEnd = onFinished;
    }
}
