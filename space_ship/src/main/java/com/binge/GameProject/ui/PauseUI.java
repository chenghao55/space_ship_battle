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

public class PauseUI {
    private Group uiRoot;
    private StackPane mainContainer;
    
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
        
        VBox pausePanel = new VBox(40);
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
        
        PauseButton btnReturn = new PauseButton("ABORT TO MENU");
        btnReturn.setOnMouseClicked(e -> {
            if (onReturnToMenu != null) onReturnToMenu.run();
        });
        
        buttonBox.getChildren().addAll(btnResume, btnRestart, btnReturn);
        pausePanel.getChildren().addAll(title, buttonBox);
        
        mainContainer.getChildren().addAll(bgMask, pausePanel);
        uiRoot.getChildren().add(mainContainer);
    }

    public void setOnResume(Runnable callback) { this.onResume = callback; }
    public void setOnRestart(Runnable callback) { this.onRestart = callback; }
    public void setOnReturnToMenu(Runnable callback) { this.onReturnToMenu = callback; }

    public void show() {
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

        public PauseButton(String label) {
            setAlignment(Pos.CENTER);
            
            bg = new Rectangle(320, 50);
            bg.setFill(Color.web("#001122", 0.5));
            bg.setStroke(Color.web("#005588"));
            bg.setStrokeWidth(2);
            bg.setArcWidth(10);
            bg.setArcHeight(10);
            
            Text text = new Text(label);
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
