package com.binge.GameProject.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import com.binge.GameProject.gameplay.ScoreResult;

// MissionResultUI 提供電影級任務結算畫面 (全息 UI)
public class MissionResultUI {
    private StackPane uiRoot;
    private VBox mainPanel;
    private Rectangle darkOverlay;
    private VBox leftStats;
    private Text rankLabel;
    private Text subTitle;
    private ResultSequenceManager resultSequenceManager;

    private Runnable onRetry;
    private Runnable onReturnToMenu;

    public MissionResultUI(Group root, double width, double height) {
        this.uiRoot = new StackPane();
        uiRoot.setPrefSize(width, height);
        
        // 背景暗化遮罩
        darkOverlay = new Rectangle(width, height, Color.web("#000000", 0.7));

        // 主面板
        mainPanel = new VBox(40);
        mainPanel.setAlignment(Pos.CENTER);

        // 標題
        Text title = new Text("MISSION END");
        title.setFont(new Font("Consolas", 72));
        title.setFill(Color.web("#00FFFF"));
        
        subTitle = new Text("GALACTIC HARMONY RESCUE REPORT");
        subTitle.setFont(new Font("Consolas", 24));
        subTitle.setFill(Color.web("#CCCCCC"));

        // 統計資料區塊 (左右分欄)
        HBox statsBox = new HBox(150);
        statsBox.setAlignment(Pos.CENTER);

        // 左側：數據
        leftStats = new VBox(15);
        leftStats.setAlignment(Pos.CENTER_LEFT);

        // 右側：評價與加分
        VBox rightStats = new VBox(15);
        rightStats.setAlignment(Pos.CENTER_RIGHT);
        
        rankLabel = new Text("RANK: -");
        rankLabel.setFont(new Font("Consolas", 48));
        rankLabel.setFill(Color.web("#FFD700")); // 金色
        resultSequenceManager = new ResultSequenceManager(leftStats, rankLabel);
        
        Text bonus1 = new Text("+ IDOL RESCUE DATA VERIFIED");
        bonus1.setFont(new Font("Consolas", 18));
        bonus1.setFill(Color.web("#00FFCC"));
        
        Text bonus2 = new Text("+ SNAKE HITBOX ACTIVE");
        bonus2.setFont(new Font("Consolas", 18));
        bonus2.setFill(Color.web("#00FFCC"));
        
        rightStats.getChildren().addAll(rankLabel, bonus1, bonus2);
        statsBox.getChildren().addAll(leftStats, rightStats);

        // 按鈕區
        HBox buttonBox = new HBox(30);
        buttonBox.setAlignment(Pos.CENTER);
        
        ResultButton retryBtn = new ResultButton("RETRY");
        ResultButton menuBtn = new ResultButton("RETURN TO MENU");
        
        // 在此可加入事件監聽 (例如重置遊戲等)
        retryBtn.setOnMouseClicked(e -> {
            if (onRetry != null) onRetry.run();
        });
        menuBtn.setOnMouseClicked(e -> {
            if (onReturnToMenu != null) onReturnToMenu.run();
        });
        
        buttonBox.getChildren().addAll(retryBtn, menuBtn);

        // 組裝面板
        mainPanel.getChildren().addAll(title, subTitle, statsBox, buttonBox);
        uiRoot.getChildren().addAll(darkOverlay, mainPanel);
        
        // 預設為隱藏
        uiRoot.setOpacity(0);
        uiRoot.setVisible(false);
        root.getChildren().add(uiRoot);
    }

    private void addStatRow(VBox container, String labelText, String valueText) {
        HBox row = new HBox(20);
        Text label = new Text(labelText);
        label.setFont(new Font("Consolas", 20));
        label.setFill(Color.GRAY);
        
        Text value = new Text(valueText);
        value.setFont(new Font("Consolas", 20));
        value.setFill(Color.WHITE);
        
        row.getChildren().addAll(label, value);
        container.getChildren().add(row);
    }

    public void updateResult(ScoreResult result) {
        subTitle.setText(result.getPlayerHp() <= 0 ? "RESULTS RECOVERED" : "GALACTIC HARMONY RESCUE REPORT");
        resultSequenceManager.start(result);
    }

    // 觸發顯示動畫
    public void show() {
        uiRoot.setVisible(true);
        uiRoot.toFront(); // 確保 UI 在最上層
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), uiRoot);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
    
    public void hide() {
        if (resultSequenceManager != null) {
            resultSequenceManager.stop();
        }
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), uiRoot);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            uiRoot.setVisible(false);
        });
        ft.play();
    }
    
    public void setOnRetry(Runnable onRetry) {
        this.onRetry = onRetry;
    }

    public void setOnReturnToMenu(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
    }

    // 自訂的科幻按鈕，採用 StackPane 以免在縮放與全螢幕下與 JavaFX Button 事件衝突
    private static class ResultButton extends StackPane {
        private Rectangle bg;
        private Text text;

        public ResultButton(String label) {
            setAlignment(Pos.CENTER);

            bg = new Rectangle(240, 50);
            bg.setFill(Color.TRANSPARENT);
            bg.setStroke(Color.web("#00FFFF"));
            bg.setStrokeWidth(2);

            text = new Text(label);
            text.setFont(new Font("Consolas", 18));
            text.setFill(Color.web("#00FFFF"));

            getChildren().addAll(bg, text);

            setOnMouseEntered(e -> {
                bg.setFill(Color.web("#00FFFF"));
                text.setFill(Color.BLACK);
                setCursor(javafx.scene.Cursor.HAND);
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playSwitch();
                }
            });

            setOnMouseExited(e -> {
                bg.setFill(Color.TRANSPARENT);
                text.setFill(Color.web("#00FFFF"));
            });

            setOnMousePressed(e -> {
                if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                    com.binge.GameProject.audio.AudioSystem.getInstance().playButtonPress();
                }
            });
        }
    }
}
