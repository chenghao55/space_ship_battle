package com.binge.GameProject.engine;

import com.binge.GameProject.rendering.CameraManager;
import com.binge.GameProject.ui.HUDManager;
import com.binge.GameProject.ui.MissionResultUI;
import javafx.animation.AnimationTimer;

// GameLoop 繼承了 JavaFX 的 AnimationTimer
// AnimationTimer 是一個專門用在遊戲和動畫的迴圈，每當螢幕準備好畫下一幀時，就會呼叫 handle 方法 (通常是每秒 60 次)
public class GameLoop extends AnimationTimer {
    // 記錄上一次執行的時間 (奈秒，1秒 = 10億奈秒)
    private long lastNanoTime = 0;
    
    // 需要被持續更新的三個系統管理員
    private GameManager gameManager;
    private CameraManager cameraManager;
    private HUDManager hudManager;
    private MissionResultUI missionResultUI;
    private javafx.scene.Scene scene;
    private javafx.scene.SubScene worldSubScene;
    private boolean missionClearTriggered = false;
    private java.awt.Robot robot;
    private javafx.stage.Window lastWindow = null;
    private int windowChangedFrames = 0;

    // 建構子：接收並儲存這三個管理員
    public GameLoop(GameManager gameManager, CameraManager cameraManager, HUDManager hudManager, MissionResultUI missionResultUI, javafx.scene.Scene scene, javafx.scene.SubScene worldSubScene) {
        this.gameManager = gameManager;
        this.cameraManager = cameraManager;
        this.hudManager = hudManager;
        this.missionResultUI = missionResultUI;
        this.scene = scene;
        this.worldSubScene = worldSubScene;
        try {
            this.robot = new java.awt.Robot();
        } catch (Exception e) {
            System.err.println("Failed to initialize java.awt.Robot: " + e.getMessage());
        }
    }

    // 這個方法每幀都會被自動呼叫一次
    @Override
    public void handle(long currentNanoTime) {
        // 如果是第一次執行，先記錄下當前時間就跳出
        if (lastNanoTime == 0) {
            lastNanoTime = currentNanoTime;
            return;
        }

        // 計算距離上一幀經過了多少時間 (單位轉換為秒，dt = Delta Time)
        // 使用 dt 的好處是，即使電腦變慢 (掉幀)，遊戲物理的速度依然會保持一致
        double dt = (currentNanoTime - lastNanoTime) / 1000000000.0;
        lastNanoTime = currentNanoTime; // 更新記錄的時間

        // 1. 更新核心邏輯與物理 (例如：引力計算、飛船移動)
        gameManager.update(dt);

        // 2. 更新第三人稱鏡頭 (讓攝影機緊跟在飛船後方)
        if (cameraManager != null) {
            cameraManager.update(gameManager.getPlayer(), dt);
        }

        // 3. 更新 UI 介面 (例如：更新目前的速度數值與警告文字)
        if (hudManager != null) {
            if (gameManager.getCurrentState() == GameState.PLAYING 
                    || gameManager.getCurrentState() == GameState.BULLET_TIME 
                    || gameManager.getCurrentState() == GameState.PAUSED
                    || gameManager.getCurrentState() == GameState.ENDING_FREEZE) {
                hudManager.setVisible(true);
                if (gameManager.getCurrentState() != GameState.PAUSED) {
                    hudManager.update(gameManager, dt);
                }
            } else {
                hudManager.setVisible(false);
            }
        }
        
        // 4. 觸發任務結算畫面
        if (gameManager.getCurrentState() == GameState.MISSION_CLEAR && !missionClearTriggered) {
            missionClearTriggered = true;
            if (missionResultUI != null) {
                missionResultUI.updateResult(gameManager.getScoreResult());
                missionResultUI.show();
            }
        }

        // 5. 更新滑鼠游標顯示狀態 (遊戲中隱藏，選單/暫停時顯示)
        if (scene != null) {
            javafx.stage.Window window = scene.getWindow();
            if (window != lastWindow) {
                lastWindow = window;
                windowChangedFrames = 10; // 視窗切換時，在接下來的 10 幀內強制重設游標狀態，以覆蓋 JavaFX 視窗 Peer 初始化
            }
            if (windowChangedFrames > 0) {
                windowChangedFrames--;
            }
            boolean forceUpdate = windowChangedFrames > 0;

            GameState state = gameManager.getCurrentState();
            if (state == GameState.PLAYING 
                    || state == GameState.STARTING_TRANSITION 
                    || state == GameState.ENDING_FREEZE 
                    || state == GameState.BULLET_TIME) {
                
                if (forceUpdate || scene.getCursor() != javafx.scene.Cursor.NONE) {
                    scene.setCursor(javafx.scene.Cursor.DEFAULT);
                    scene.setCursor(javafx.scene.Cursor.NONE);
                }
                if (scene.getRoot() != null && (forceUpdate || scene.getRoot().getCursor() != javafx.scene.Cursor.NONE)) {
                    scene.getRoot().setCursor(javafx.scene.Cursor.DEFAULT);
                    scene.getRoot().setCursor(javafx.scene.Cursor.NONE);
                }
                if (worldSubScene != null && (forceUpdate || worldSubScene.getCursor() != javafx.scene.Cursor.NONE)) {
                    worldSubScene.setCursor(javafx.scene.Cursor.DEFAULT);
                    worldSubScene.setCursor(javafx.scene.Cursor.NONE);
                }

                // 限制游標實際位移，將其鎖定在視窗中心
                if (window != null && window.isFocused()) {
                    double centerX = window.getX() + window.getWidth() / 2.0;
                    double centerY = window.getY() + window.getHeight() / 2.0;
                    if (robot != null) {
                        robot.mouseMove((int) centerX, (int) centerY);
                    }
                }
            } else {
                if (scene.getCursor() != javafx.scene.Cursor.DEFAULT) {
                    scene.setCursor(javafx.scene.Cursor.DEFAULT);
                }
                if (scene.getRoot() != null && scene.getRoot().getCursor() != javafx.scene.Cursor.DEFAULT) {
                    scene.getRoot().setCursor(javafx.scene.Cursor.DEFAULT);
                }
                if (worldSubScene != null && worldSubScene.getCursor() != javafx.scene.Cursor.DEFAULT) {
                    worldSubScene.setCursor(javafx.scene.Cursor.DEFAULT);
                }
            }
        }
    }
    
    public void resetMissionClearTrigger() {
        this.missionClearTriggered = false;
    }
}
