package com.binge.GameProject.ui;

import com.binge.GameProject.audio.AudioSystem;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.binge.GameProject.DisplayMode;

public class PauseUI {
    private Group uiRoot;
    private StackPane mainContainer;
    private VBox pausePanel;
    private VBox settingsPanel;
    
    private PauseButton btnDisplayMode;
    private DisplayMode currentDisplayMode = DisplayMode.WINDOWED;
    private java.util.function.Consumer<DisplayMode> onDisplayModeChange;
    private SciFiSlider sliderVolume;
    private java.util.function.Consumer<Double> onVolumeChange;
    
    private Runnable onResume;
    private Runnable onRestart;
    private Runnable onReturnToMenu;

    public PauseUI(Group root, int width, int height) {
        this.uiRoot = root;
        
        mainContainer = new StackPane();
        mainContainer.setPrefSize(width, height);
        mainContainer.setVisible(false);
        
        // 1. 半透明黑色遮罩背景 (Glassmorphism Effect)
        Rectangle bgMask = new Rectangle(width, height);
        bgMask.setFill(Color.web("#03030d", 0.75));
        
        this.pausePanel = new VBox(40);
        pausePanel.setAlignment(Pos.CENTER);
        
        // 標題 (Paused Title)
        Text title = new Text("SYSTEM PAUSED");
        title.setFont(new Font("Consolas", 64));
        title.setFill(Color.web("#ffffff"));
        DropShadow titleGlow = new DropShadow(30, Color.web("#00ffff"));
        titleGlow.setInput(new Glow(0.5));
        title.setEffect(titleGlow);
        
        // 按鈕區
        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        PauseButton btnResume = new PauseButton("RESUME SIMULATION");
        btnResume.setOnMouseClicked(e -> {
            if (onResume != null) onResume.run();
        });
        
        PauseButton btnRestart = new PauseButton("RESTART MISSION");
        btnRestart.setOnMouseClicked(e -> {
            if (onRestart != null) onRestart.run();
        });
        
        PauseButton btnSettings = new PauseButton("SETTINGS");
        btnSettings.setOnMouseClicked(e -> showSettings());
        
        PauseButton btnReturn = new PauseButton("ABORT TO MENU");
        btnReturn.setOnMouseClicked(e -> {
            if (onReturnToMenu != null) onReturnToMenu.run();
        });
        
        buttonBox.getChildren().addAll(btnResume, btnRestart, btnSettings, btnReturn);
        pausePanel.getChildren().addAll(title, buttonBox);
        
        // 3. 設定面板 (Settings Panel)
        settingsPanel = new VBox(30);
        settingsPanel.setAlignment(Pos.CENTER);
        settingsPanel.setVisible(false);
        
        Text settingsTitle = new Text("SYSTEM SETTINGS");
        settingsTitle.setFont(new Font("Consolas", 50));
        settingsTitle.setFill(Color.web("#00ffff"));
        
        PauseButton btnBack = new PauseButton("BACK TO PAUSE");
        btnBack.setOnMouseClicked(e -> showPausePanel());
        
        btnDisplayMode = new PauseButton("SCREEN: WINDOWED");
        btnDisplayMode.setOnMouseClicked(e -> {
            DisplayMode nextMode;
            if (currentDisplayMode == DisplayMode.WINDOWED) {
                nextMode = DisplayMode.BORDERLESS_WINDOWED;
            } else if (currentDisplayMode == DisplayMode.BORDERLESS_WINDOWED) {
                nextMode = DisplayMode.MAXIMIZED;
            } else {
                nextMode = DisplayMode.WINDOWED;
            }
            setDisplayModeText(nextMode);
            if (onDisplayModeChange != null) {
                onDisplayModeChange.accept(nextMode);
            }
        });
        
        double initVol = AudioSystem.getInstance() != null ? AudioSystem.getInstance().getMasterVolume() : 1.0;
        sliderVolume = new SciFiSlider("VOLUME: ", 320, 50, initVol);
        sliderVolume.setOnValueChange(val -> {
            if (AudioSystem.getInstance() != null) {
                AudioSystem.getInstance().setMasterVolume(val);
            }
            if (onVolumeChange != null) {
                onVolumeChange.accept(val);
            }
        });

        settingsPanel.getChildren().addAll(
            settingsTitle,
            sliderVolume,
            new Text("GRAPHICS: CINEMATIC"),
            btnDisplayMode,
            btnBack
        );
        
        // 將文字顏色設為白色
        for (javafx.scene.Node n : settingsPanel.getChildren()) {
            if (n instanceof Text && n != settingsTitle) {
                ((Text)n).setFont(new Font("Consolas", 18));
                ((Text)n).setFill(Color.WHITE);
            }
        }
        
        mainContainer.getChildren().addAll(bgMask, pausePanel, settingsPanel);
        uiRoot.getChildren().add(mainContainer);
    }

    public void setOnResume(Runnable callback) { this.onResume = callback; }
    public void setOnRestart(Runnable callback) { this.onRestart = callback; }
    public void setOnReturnToMenu(Runnable callback) { this.onReturnToMenu = callback; }

    public void setOnDisplayModeChange(java.util.function.Consumer<DisplayMode> callback) {
        this.onDisplayModeChange = callback;
    }

    public void setVolume(double val) {
        if (sliderVolume != null) {
            sliderVolume.setValue(val);
        }
    }

    public void setOnVolumeChange(java.util.function.Consumer<Double> callback) {
        this.onVolumeChange = callback;
    }
    
    public void setDisplayModeText(DisplayMode mode) {
        this.currentDisplayMode = mode;
        if (btnDisplayMode != null) {
            String modeStr;
            switch (mode) {
                case BORDERLESS_WINDOWED:
                    modeStr = "BORDERLESS";
                    break;
                case MAXIMIZED:
                    modeStr = "MAXIMIZED";
                    break;
                case WINDOWED:
                default:
                    modeStr = "WINDOWED";
                    break;
            }
            btnDisplayMode.setText("SCREEN: " + modeStr);
        }
    }
    
    private void showSettings() {
        pausePanel.setVisible(false);
        settingsPanel.setVisible(true);
    }
    
    private void showPausePanel() {
        settingsPanel.setVisible(false);
        pausePanel.setVisible(true);
    }

    public void show() {
        showPausePanel(); // 確保每次打開時顯示的是暫停選單而非設定
        mainContainer.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(250), mainContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public void hide() {
        FadeTransition ft = new FadeTransition(Duration.millis(150), mainContainer);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> mainContainer.setVisible(false));
        ft.play();
    }

    public boolean isVisible() {
        return mainContainer.isVisible();
    }

    // 自訂的 Sci-Fi 風格暫停按鈕
    private static class PauseButton extends StackPane {
        private Rectangle bg;
        private Text text;
        
        public void setText(String label) {
            text.setText(label);
        }

        public PauseButton(String label) {
            setAlignment(Pos.CENTER);
            
            bg = new Rectangle(320, 50);
            bg.setFill(Color.web("#001122", 0.5));
            bg.setStroke(Color.web("#005588"));
            bg.setStrokeWidth(2);
            bg.setArcWidth(10);
            bg.setArcHeight(10);
            
            this.text = new Text(label);
            text.setFont(new Font("Consolas", 18));
            text.setFill(Color.web("#00ffff"));
            
            getChildren().addAll(bg, text);
            
            DropShadow hoverGlow = new DropShadow(20, Color.web("#00ffff"));
            
            setOnMouseEntered(e -> {
                bg.setFill(Color.web("#003355", 0.8));
                bg.setStroke(Color.web("#00ffff"));
                bg.setEffect(hoverGlow);
                setTranslateX(8);
                setCursor(javafx.scene.Cursor.HAND);
                if (AudioSystem.getInstance() != null) {
                    AudioSystem.getInstance().playSwitch();
                }
            });
            
            setOnMouseExited(e -> {
                bg.setFill(Color.web("#001122", 0.5));
                bg.setStroke(Color.web("#005588"));
                bg.setEffect(null);
                setTranslateX(0);
            });

            setOnMousePressed(e -> {
                if (AudioSystem.getInstance() != null) {
                    AudioSystem.getInstance().playButtonPress();
                }
            });
        }
    }
}
