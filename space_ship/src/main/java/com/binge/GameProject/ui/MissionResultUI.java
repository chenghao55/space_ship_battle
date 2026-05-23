package com.binge.GameProject.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import com.binge.GameProject.gameplay.ScoreResult;

// MissionResultUI 提供電影級任務結算畫面 (全息 UI)
public class MissionResultUI {
    private Group uiRoot;
    private VBox mainPanel;
    private Rectangle darkOverlay;
    private VBox leftStats;
    private Text rankLabel;
    private Text subTitle;
    private ResultSequenceManager resultSequenceManager;

    private Runnable onRetry;
    private Runnable onReturnToMenu;

    public MissionResultUI(Group root, double width, double height) {
        this.uiRoot = new Group();
        
        // 背景暗化遮罩
        darkOverlay = new Rectangle(width, height, Color.web("#000000", 0.7));
        uiRoot.getChildren().add(darkOverlay);

        // 主面板
        mainPanel = new VBox(40);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setPrefWidth(width);
        mainPanel.setPrefHeight(height);

        // 標題
        Text title = new Text("MISSION COMPLETE");
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
        
        Button retryBtn = createStyledButton("RETRY");
        Button menuBtn = createStyledButton("RETURN TO MENU");
        
        // 在此可加入事件監聽 (例如重置遊戲等)
        retryBtn.setOnAction(e -> {
            if (onRetry != null) onRetry.run();
        });
        menuBtn.setOnAction(e -> {
            if (onReturnToMenu != null) onReturnToMenu.run();
        });
        
        buttonBox.getChildren().addAll(retryBtn, menuBtn);

        // 組裝面板
        mainPanel.getChildren().addAll(title, subTitle, statsBox, buttonBox);
        uiRoot.getChildren().add(mainPanel);
        
        // 預設為隱藏
        uiRoot.setOpacity(0);
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
        subTitle.setText(result.getPlayerHp() <= 0 ? "SHIP LOST // RESULTS RECOVERED" : "GALACTIC HARMONY RESCUE REPORT");
        resultSequenceManager.start(result);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(new Font("Consolas", 18));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00FFFF; -fx-border-color: #00FFFF; -fx-border-width: 2; -fx-padding: 10 30;");
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: #00FFFF; -fx-text-fill: #000000; -fx-border-color: #00FFFF; -fx-border-width: 2; -fx-padding: 10 30;");
            if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                com.binge.GameProject.audio.AudioSystem.getInstance().playSwitch();
            }
        });
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00FFFF; -fx-border-color: #00FFFF; -fx-border-width: 2; -fx-padding: 10 30;"));
        
        btn.setOnMousePressed(e -> {
            if (com.binge.GameProject.audio.AudioSystem.getInstance() != null) {
                com.binge.GameProject.audio.AudioSystem.getInstance().playButtonPress();
            }
        });
        
        return btn;
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
}
