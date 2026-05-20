package com.binge.GameProject;

import com.binge.GameProject.engine.GameLoop;
import com.binge.GameProject.engine.GameManager;
import com.binge.GameProject.engine.InputManager;
import com.binge.GameProject.rendering.CameraManager;
import com.binge.GameProject.rendering.LightSystem;
import com.binge.GameProject.rendering.ParticleRenderer;
import com.binge.GameProject.ui.HUDManager;
import com.binge.GameProject.ui.MainMenuUI;
import com.binge.GameProject.ui.MissionResultUI;
import com.binge.GameProject.audio.AudioSystem;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// 這是遊戲的主程式入口，繼承自 JavaFX 的 Application
public class Main extends Application {
    // 設定遊戲視窗的寬度與高度
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    // start 方法是 JavaFX 應用程式開始執行的地方
    @Override
    public void start(Stage primaryStage) {
        // 1. 設定主場景結構
        // root 是所有畫面的最底層容器
        Group root = new Group();
        // 建立主場景 (Scene)，並開啟反鋸齒 (SceneAntialiasing.BALANCED) 讓邊緣更平滑
        Scene scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK); // 背景塗黑

        // 建立一個專門用來放 3D 遊戲物件的容器 (例如飛船、星球)
        Group worldRoot = new Group();
        // SubScene 是子場景，用來獨立渲染 3D 世界，不會影響到 2D 的 UI
        SubScene worldSubScene = new SubScene(worldRoot, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        worldSubScene.setFill(Color.web("#050510")); // 設定為深邃的宇宙顏色 (極深的藍黑色)

        // 建立一個專門用來放 2D UI 介面的容器 (例如速度表、警告文字)
        Group uiRoot = new Group();
        
        // 將 3D 世界和 2D UI 一起加到底層容器中 (UI 後加，所以會顯示在 3D 的上層)
        root.getChildren().addAll(worldSubScene, uiRoot);

        // 2. 攝影機設定
        // PerspectiveCamera 是具有透視效果的 3D 攝影機 (近大遠小)
        PerspectiveCamera camera = new PerspectiveCamera(true);
        // 將攝影機綁定到 3D 子場景中
        worldSubScene.setCamera(camera);
        // 建立我們自己寫的 CameraManager，用來控制攝影機跟隨飛船
        CameraManager cameraManager = new CameraManager(camera);

        // 3. 環境與光影
        // 呼叫 LightSystem 來設置環境光與恆星的點光源
        LightSystem.setupLights(worldRoot);
        // 產生靜態的背景星星點綴
        ParticleRenderer.generateStarfield(uiRoot, WIDTH, HEIGHT);

        // 4. 遊戲邏輯與實體總管
        // GameManager 負責管理所有的飛船、星球以及物理更新
        GameManager gameManager = new GameManager(worldRoot, cameraManager);
        
        // 5. 輸入系統
        // 將我們自己寫的 InputManager 綁定到主場景，這樣才能收到玩家的鍵盤按鍵
        InputManager.getInstance().attachToScene(scene);

        // 6. 建立 UI 抬頭顯示器 (HUD) 與任務結算畫面 (MissionResultUI)
        HUDManager hudManager = new HUDManager(uiRoot);
        MissionResultUI missionResultUI = new MissionResultUI(uiRoot, WIDTH, HEIGHT);
        MainMenuUI mainMenuUI = new MainMenuUI(uiRoot, WIDTH, HEIGHT);
        
        // 7. 啟動遊戲主迴圈
        // GameLoop 是一個計時器，每秒會執行約 60 次來更新遊戲畫面與邏輯
        GameLoop gameLoop = new GameLoop(gameManager, cameraManager, hudManager, missionResultUI);
        
        // 設定點擊 Start Mission 後的動畫過場邏輯
        mainMenuUI.setOnStartMission(() -> {
            mainMenuUI.fadeOutAndHide();
            gameManager.setCurrentState(com.binge.GameProject.engine.GameState.STARTING_TRANSITION);
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().stopMenuMusic();
            }
            cameraManager.playStartTransition(() -> {
                // 過場動畫結束，正式進入遊戲
                gameManager.setCurrentState(com.binge.GameProject.engine.GameState.PLAYING);
            });
        });
        
        // 設定任務結算畫面的按鈕邏輯
        missionResultUI.setOnRetry(() -> {
            missionResultUI.hide();
            gameManager.resetGame();
            gameLoop.resetMissionClearTrigger();
        });
        
        missionResultUI.setOnReturnToMenu(() -> {
            missionResultUI.hide();
            gameManager.resetGame();
            gameManager.setCurrentState(com.binge.GameProject.engine.GameState.MAIN_MENU);
            cameraManager.setMenuMode(true);
            mainMenuUI.fadeInAndShow();
            gameLoop.resetMissionClearTrigger();
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().playMenuMusic();
            }
        });

        gameLoop.start(); // 開始運作
        if (AudioSystem.getInstance() != null) {
            AudioSystem.getInstance().playMenuMusic();
        }

        // 8. 顯示視窗設定
        primaryStage.setTitle("Galactic Harmony"); // 設定視窗標題
        primaryStage.setScene(scene); // 將場景放入視窗
        primaryStage.setResizable(false); // 禁止使用者改變視窗大小，避免排版跑掉
        primaryStage.show(); // 正式顯示視窗
        
        // 請求取得鍵盤焦點，確保玩家一開啟遊戲按 W/A/D 就有反應
        root.requestFocus();
    }

    // Java 程式的標準進入點
    public static void main(String[] args) {
        launch(args); // 啟動 JavaFX 應用程式
    }
}
