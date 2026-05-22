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
import com.binge.GameProject.ui.PauseUI;
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

    private Stage currentStage;
    private DisplayMode currentMode = null;
    private Scene scene;
    private javafx.scene.layout.Pane rootPane;
    private MainMenuUI mainMenuUI;
    private PauseUI pauseUI;
    private Runnable updateScale;

    // start 方法是 JavaFX 應用程式開始執行的地方
    @Override
    public void start(Stage primaryStage) {
        // 0. 防止視窗切換重建時 JavaFX 自動結束
        javafx.application.Platform.setImplicitExit(false);

        // 1. 設定主場景結構
        // rootPane 是最底層的 Pane，可供視窗縮放與全螢幕使用
        this.rootPane = new javafx.scene.layout.Pane();
        rootPane.setStyle("-fx-background-color: black;");

        // gameScaleGroup 是放置所有遊戲畫面的容器，用來做等比例縮放
        Group gameScaleGroup = new Group();
        rootPane.getChildren().add(gameScaleGroup);

        // 建立主場景 (Scene)，並開啟反鋸齒 (SceneAntialiasing.BALANCED) 讓邊緣更平滑
        this.scene = new Scene(rootPane, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK); // 背景塗黑

        // 設定等比例縮放 Transform
        javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(1.0, 1.0, 0, 0);
        gameScaleGroup.getTransforms().add(scale);

        this.updateScale = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            double scaleX = w / WIDTH;
            double scaleY = h / HEIGHT;
            double finalScale = Math.min(scaleX, scaleY);
            
            scale.setX(finalScale);
            scale.setY(finalScale);
            
            // 計算置中偏移量 (Pillarbox/Letterbox)
            double targetWidth = WIDTH * finalScale;
            double targetHeight = HEIGHT * finalScale;
            gameScaleGroup.setTranslateX((w - targetWidth) / 2.0);
            gameScaleGroup.setTranslateY((h - targetHeight) / 2.0);
        };

        scene.widthProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        scene.heightProperty().addListener((obs, oldVal, newVal) -> updateScale.run());

        // 建立一個專門用來放 3D 遊戲物件的容器 (例如飛船、星球)
        Group worldRoot = new Group();
        // SubScene 是子場景，用來獨立渲染 3D 世界，不會影響到 2D 的 UI
        SubScene worldSubScene = new SubScene(worldRoot, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        worldSubScene.setFill(Color.web("#050510")); // 設定為深邃的宇宙顏色 (極深的藍黑色)

        // 建立一個專門用來放 2D UI 介面的容器 (例如速度表、警告文字)
        Group uiRoot = new Group();
        
        // 將 3D 世界和 2D UI 一起加到縮放容器中 (UI 後加，所以會顯示在 3D 的上層)
        gameScaleGroup.getChildren().addAll(worldSubScene, uiRoot);

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
        this.mainMenuUI = new MainMenuUI(uiRoot, WIDTH, HEIGHT);
        this.pauseUI = new PauseUI(uiRoot, WIDTH, HEIGHT);
        pauseUI.setOnDisplayModeChange(this::setDisplayMode);
        
        // 雙向音量同步
        mainMenuUI.setOnVolumeChange(val -> pauseUI.setVolume(val));
        pauseUI.setOnVolumeChange(val -> mainMenuUI.setVolume(val));
        
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

        // 設定暫停畫面的按鈕邏輯
        pauseUI.setOnResume(() -> {
            pauseUI.hide();
            gameManager.resumeGame();
        });

        pauseUI.setOnRestart(() -> {
            pauseUI.hide();
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().stopMenuMusic();
            }
            gameManager.resetGame();
            gameLoop.resetMissionClearTrigger();
        });

        pauseUI.setOnReturnToMenu(() -> {
            pauseUI.hide();
            gameManager.resetGame();
            gameManager.setCurrentState(com.binge.GameProject.engine.GameState.MAIN_MENU);
            cameraManager.setMenuMode(true);
            mainMenuUI.fadeInAndShow();
            gameLoop.resetMissionClearTrigger();
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().playMenuMusic();
            }
        });

        // 監聽鍵盤事件以開啟或關閉暫停選單 (按下 ESC 或 P 鍵) 以及切換全螢幕 (F11 或 Alt+Enter)
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE || event.getCode() == javafx.scene.input.KeyCode.P) {
                var state = gameManager.getCurrentState();
                if (state == com.binge.GameProject.engine.GameState.PLAYING 
                        || state == com.binge.GameProject.engine.GameState.BULLET_TIME) {
                    gameManager.pauseGame();
                    pauseUI.show();
                } else if (state == com.binge.GameProject.engine.GameState.PAUSED) {
                    pauseUI.hide();
                    gameManager.resumeGame();
                }
            } else if (event.getCode() == javafx.scene.input.KeyCode.F11 || (event.getCode() == javafx.scene.input.KeyCode.ENTER && event.isAltDown())) {
                if (currentMode == DisplayMode.BORDERLESS_WINDOWED) {
                    setDisplayMode(DisplayMode.WINDOWED);
                } else {
                    setDisplayMode(DisplayMode.BORDERLESS_WINDOWED);
                }
                event.consume();
            }
        });

        gameLoop.start(); // 開始運作
        if (AudioSystem.getInstance() != null) {
            AudioSystem.getInstance().playMenuMusic();
        }

        // 8. 顯示視窗設定
        this.currentStage = primaryStage;
        
        // 註冊選單的顯示模式切換回呼
        mainMenuUI.setOnDisplayModeChange(this::setDisplayMode);
        
        // 設置初始顯示模式為 WINDOWED
        setDisplayMode(DisplayMode.WINDOWED);
    }

    public void setDisplayMode(DisplayMode mode) {
        if (mode == currentMode && currentStage != null && currentStage.isShowing()) {
            return;
        }

        // 判斷是否需要重建 Stage
        boolean oldIsDecorated = (currentMode == null || currentMode == DisplayMode.WINDOWED || currentMode == DisplayMode.MAXIMIZED);
        boolean newIsDecorated = (mode == DisplayMode.WINDOWED || mode == DisplayMode.MAXIMIZED);

        Stage oldStage = currentStage;
        
        if (oldStage == null || oldIsDecorated != newIsDecorated) {
            Stage newStage = new Stage();
            if (newIsDecorated) {
                newStage.initStyle(javafx.stage.StageStyle.DECORATED);
            } else {
                newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            }
            
            newStage.setTitle("Galactic Harmony");
            newStage.setResizable(true);
            newStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
            newStage.setFullScreenExitHint("");
            
            // 點擊關閉按鈕時徹底結束程式
            newStage.setOnCloseRequest(e -> System.exit(0));

            if (oldStage != null) {
                oldStage.setScene(null);
                newStage.setX(oldStage.getX());
                newStage.setY(oldStage.getY());
                oldStage.close();
            }
            
            newStage.setScene(scene);
            currentStage = newStage;
        } else {
            // 確保 scene 已經掛載至目前的 Stage (例如初次運行使用傳入的 primaryStage)
            if (currentStage.getScene() != scene) {
                currentStage.setScene(scene);
            }
            // 確保關閉按鈕事件有正確綁定，點擊時徹底結束程式
            currentStage.setOnCloseRequest(e -> System.exit(0));
        }

        currentMode = mode;
        
        // 根據模式設定大小與位置
        if (mode == DisplayMode.WINDOWED) {
            currentStage.setMaximized(false);
            currentStage.setFullScreen(false);
            currentStage.setWidth(1280);
            currentStage.setHeight(720);
            
            // 視窗置中
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            currentStage.setX((bounds.getWidth() - 1280) / 2 + bounds.getMinX());
            currentStage.setY((bounds.getHeight() - 720) / 2 + bounds.getMinY());
            
        } else if (mode == DisplayMode.BORDERLESS_WINDOWED) {
            currentStage.setMaximized(false);
            currentStage.setFullScreen(false);
            
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D screenBounds = screen.getBounds();
            
            currentStage.setX(screenBounds.getMinX());
            currentStage.setY(screenBounds.getMinY());
            currentStage.setWidth(screenBounds.getWidth());
            currentStage.setHeight(screenBounds.getHeight());
            
        } else if (mode == DisplayMode.MAXIMIZED) {
            currentStage.setFullScreen(false);
            currentStage.setMaximized(true);
        }

        if (!currentStage.isShowing()) {
            currentStage.show();
        }

        // 同步選單按鈕文字
        if (mainMenuUI != null) {
            mainMenuUI.setDisplayModeText(mode);
        }
        if (pauseUI != null) {
            pauseUI.setDisplayModeText(mode);
        }

        // 更新縮放比例
        if (updateScale != null) {
            updateScale.run();
        }

        // 重新獲得鍵盤焦點
        if (rootPane != null) {
            rootPane.requestFocus();
        }
    }

    // Java 程式的標準進入點
    public static void main(String[] args) {
        launch(args); // 啟動 JavaFX 應用程式
    }
}
