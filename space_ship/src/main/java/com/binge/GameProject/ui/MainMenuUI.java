package com.binge.GameProject.ui;

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

public class MainMenuUI {
    private Group uiRoot;
    private StackPane mainContainer;
    private VBox menuPanel;
    private VBox settingsPanel;
    
    private Runnable onStartMission;
    
    private MenuButton btnDisplayMode;
    private DisplayMode currentDisplayMode = DisplayMode.WINDOWED;
    private java.util.function.Consumer<DisplayMode> onDisplayModeChange;

    public MainMenuUI(Group root, int width, int height) {
        this.uiRoot = root;
        
        mainContainer = new StackPane();
        mainContainer.setPrefSize(width, height);
        
        // 1. 主選單面板
        menuPanel = new VBox(30);
        menuPanel.setAlignment(Pos.CENTER);
        
        // 標題 (Title)
        Text title = new Text("GRAVITY DRIFT");
        title.setFont(new Font("Consolas", 90));
        title.setFill(Color.web("#ffffff"));
        DropShadow titleGlow = new DropShadow(40, Color.web("#00ffff"));
        titleGlow.setInput(new Glow(0.6));
        title.setEffect(titleGlow);
        
        // 副標題 (Subtitle)
        Text subtitle = new Text("O R B I T A L   C O M B A T   I N I T I A T I V E");
        subtitle.setFont(new Font("Consolas", 24));
        subtitle.setFill(Color.web("#888888"));
        
        VBox titleBox = new VBox(5, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setTranslateY(-100);
        
        // 按鈕區
        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        MenuButton btnStart = new MenuButton("START MISSION");
        btnStart.setOnMouseClicked(e -> {
            if (onStartMission != null) {
                onStartMission.run();
            }
        });
        
        MenuButton btnSettings = new MenuButton("SETTINGS");
        btnSettings.setOnMouseClicked(e -> showSettings());
        
        MenuButton btnCredits = new MenuButton("CREDITS");
        
        MenuButton btnExit = new MenuButton("EXIT SYSTEM");
        btnExit.setOnMouseClicked(e -> System.exit(0));
        
        buttonBox.getChildren().addAll(btnStart, btnSettings, btnCredits, btnExit);
        menuPanel.getChildren().addAll(titleBox, buttonBox);
        
        // 2. 設定面板 (Settings Panel - Mockup)
        settingsPanel = new VBox(30);
        settingsPanel.setAlignment(Pos.CENTER);
        settingsPanel.setVisible(false);
        
        Text settingsTitle = new Text("SYSTEM SETTINGS");
        settingsTitle.setFont(new Font("Consolas", 50));
        settingsTitle.setFill(Color.web("#00ffff"));
        
        MenuButton btnBack = new MenuButton("BACK TO MENU");
        btnBack.setOnMouseClicked(e -> showMenu());
        
        btnDisplayMode = new MenuButton("SCREEN: WINDOWED");
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
        
        settingsPanel.getChildren().addAll(
            settingsTitle,
            new Text("AUDIO: 100%"),
            new Text("GRAPHICS: CINEMATIC"),
            btnDisplayMode,
            btnBack
        );
        
        // 將文字顏色設為白色
        for (javafx.scene.Node n : settingsPanel.getChildren()) {
            if (n instanceof Text && n != settingsTitle) {
                ((Text)n).setFont(new Font("Consolas", 20));
                ((Text)n).setFill(Color.WHITE);
            }
        }
        
        mainContainer.getChildren().addAll(menuPanel, settingsPanel);
        uiRoot.getChildren().add(mainContainer);
        
        // 淡入進場
        mainContainer.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.seconds(2.0), mainContainer);
        ft.setToValue(1.0);
        ft.play();
    }
    
    public void setOnStartMission(Runnable callback) {
        this.onStartMission = callback;
    }
    
    public void fadeOutAndHide() {
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), mainContainer);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> mainContainer.setVisible(false));
        ft.play();
    }
    
    public void fadeInAndShow() {
        mainContainer.setVisible(true);
        showMenu(); // 確保顯示的是主選單而非設定
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), mainContainer);
        ft.setToValue(1.0);
        ft.play();
    }
    
    public void setOnDisplayModeChange(java.util.function.Consumer<DisplayMode> callback) {
        this.onDisplayModeChange = callback;
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
        menuPanel.setVisible(false);
        settingsPanel.setVisible(true);
    }
    
    private void showMenu() {
        settingsPanel.setVisible(false);
        menuPanel.setVisible(true);
    }
    
    // 客製化科幻按鈕
    private static class MenuButton extends StackPane {
        private Rectangle bg;
        private Text text;
        
        public void setText(String label) {
            text.setText(label);
        }
        
        public MenuButton(String label) {
            bg = new Rectangle(300, 50);
            bg.setFill(Color.web("#001122", 0.5));
            bg.setStroke(Color.web("#005588"));
            bg.setStrokeWidth(2);
            
            text = new Text(label);
            text.setFont(new Font("Consolas", 22));
            text.setFill(Color.web("#00ffff"));
            
            getChildren().addAll(bg, text);
            
            // Hover 動畫
            DropShadow hoverGlow = new DropShadow(20, Color.web("#00ffff"));
            
            setOnMouseEntered(e -> {
                bg.setFill(Color.web("#003355", 0.8));
                bg.setStroke(Color.web("#00ffff"));
                bg.setEffect(hoverGlow);
                setTranslateX(10); // 些微向右滑動
                setCursor(javafx.scene.Cursor.HAND);
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playSwitch();
                }
            });
            
            setOnMouseExited(e -> {
                bg.setFill(Color.web("#001122", 0.5));
                bg.setStroke(Color.web("#005588"));
                bg.setEffect(null);
                setTranslateX(0);
            });

            setOnMousePressed(e -> {
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playButtonPress();
                }
            });
        }
    }
}
