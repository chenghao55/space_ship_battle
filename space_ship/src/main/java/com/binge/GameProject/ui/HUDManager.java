package com.binge.GameProject.ui;

import com.binge.GameProject.engine.SignalManager;
import com.binge.GameProject.model.Player;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

// HUDManager 負責繪製左下角的科幻圓形雷達、能量條與狀態文字
public class HUDManager {
    private Canvas hudCanvas;
    private GraphicsContext gc;
    private double scanAngle = 0; // 雷達掃描動畫角度

    public HUDManager(Group uiRoot) {
        // 建立全螢幕 Canvas (假設寬 1280, 高 720)
        hudCanvas = new Canvas(1280, 720);
        gc = hudCanvas.getGraphicsContext2D();
        uiRoot.getChildren().add(hudCanvas);
    }
    
    public void setVisible(boolean visible) {
        hudCanvas.setVisible(visible);
    }

    public void update(Player player, double dt) {
        gc.clearRect(0, 0, hudCanvas.getWidth(), hudCanvas.getHeight());
        if (player == null) return;

        drawRadar(dt);
        drawPlayerStatus(player);
    }

    private void drawRadar(double dt) {
        double radarX = 120;
        double radarY = 600;
        double radius = 80;

        SignalManager signal = SignalManager.getInstance();
        double strength = signal.getSignalStrength();
        double dirAngle = signal.getSignalDirection();
        double glitch = signal.getGlitchLevel();

        // 隨機干擾位移 (Glitch)
        double ox = (Math.random() - 0.5) * glitch * 10;
        double oy = (Math.random() - 0.5) * glitch * 10;
        
        gc.save();
        gc.translate(radarX + ox, radarY + oy);

        // 畫雷達外框
        gc.setStroke(Color.web("#00ffcc", 0.5));
        gc.setLineWidth(2);
        gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
        
        // 畫同心圓與十字線
        gc.setStroke(Color.web("#00ffcc", 0.2));
        gc.strokeOval(-radius/2, -radius/2, radius, radius);
        gc.strokeLine(-radius, 0, radius, 0);
        gc.strokeLine(0, -radius, 0, radius);

        // 畫動態掃描波
        scanAngle += 180 * dt; // 每秒 180 度
        gc.setFill(Color.web("#00ffcc", 0.15));
        gc.fillArc(-radius, -radius, radius * 2, radius * 2, -scanAngle, 60, ArcType.ROUND);

        // 畫訊號指示標籤 (若有強度)
        if (strength > 0.05) {
            double rad = Math.toRadians(dirAngle);
            double targetX = Math.sin(rad) * radius * 0.8;
            double targetY = -Math.cos(rad) * radius * 0.8;

            // 隨機閃爍
            double alpha = Math.max(0.2, strength - (Math.random() * glitch));
            gc.setFill(Color.web("#ff3366", alpha));
            gc.fillOval(targetX - 4, targetY - 4, 8, 8);
            
            // 畫一條微弱的連線
            gc.setStroke(Color.web("#ff3366", alpha * 0.5));
            gc.strokeLine(0, 0, targetX, targetY);
        }
        
        // 畫中心點 (玩家)
        gc.setFill(Color.WHITE);
        gc.fillOval(-3, -3, 6, 6);

        gc.restore();
        
        // 畫雜訊文字
        if (glitch > 0.2) {
            gc.setFill(Color.web("#ff0000", Math.random() * 0.8));
            gc.setFont(new Font("Consolas", 16));
            gc.fillText("SIGNAL LOST // INTERFERENCE", radarX - 100, radarY - 100);
        }
    }

    private void drawPlayerStatus(Player player) {
        // 畫速度與座標
        gc.setFill(Color.CYAN);
        gc.setFont(new Font("Consolas", 18));
        double speed = player.getVelocity().magnitude();
        gc.fillText(String.format("VELOCITY: %04d m/s", (int)speed), 30, 40);
        
        gc.setFill(Color.LIGHTGREEN);
        gc.setFont(new Font("Consolas", 14));
        gc.fillText(String.format("POS: [%.0f, %.0f]", player.getPosition().x, player.getPosition().y), 30, 65);

        // 畫 Boost 能量條
        gc.setFill(Color.WHITE);
        gc.fillText("BOOST", 30, 95);
        gc.setStroke(Color.GRAY);
        gc.strokeRect(90, 82, 150, 15);
        
        double energy = player.getBoostEnergy();
        if (energy > 20) gc.setFill(Color.CYAN);
        else gc.setFill(Color.RED);
        gc.fillRect(92, 84, (energy / 100.0) * 146, 11);

        // 畫過熱/危險警告
        if (player.isOverheating()) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("Consolas", 24));
            // 閃爍效果
            if (System.currentTimeMillis() % 200 > 100) {
                gc.fillText("WARNING: DANGER ZONE // OVERHEATING", 30, 140);
            }
        } else if (speed > 1200) {
            gc.setFill(Color.YELLOW);
            gc.setFont(new Font("Consolas", 18));
            gc.fillText("HIGH SPEED DRIFT", 30, 140);
        }
    }
}
