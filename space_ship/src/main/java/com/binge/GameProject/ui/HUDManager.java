package com.binge.GameProject.ui;

import com.binge.GameProject.engine.GameManager;
import com.binge.GameProject.engine.GameState;
import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Vector2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

public class HUDManager {
    private final Canvas hudCanvas;
    private final GraphicsContext gc;
    private double scanAngle = 0;
    private double pulsePhase = 0;

    public HUDManager(Group uiRoot) {
        hudCanvas = new Canvas(1280, 720);
        gc = hudCanvas.getGraphicsContext2D();
        uiRoot.getChildren().add(hudCanvas);
    }

    public void setVisible(boolean visible) {
        hudCanvas.setVisible(visible);
    }

    public void update(GameManager gameManager, double dt) {
        gc.clearRect(0, 0, hudCanvas.getWidth(), hudCanvas.getHeight());
        Player player = gameManager.getPlayer();
        if (player == null) return;

        drawTopStatus(gameManager, player);
        drawBoost(player);
        drawRadar(gameManager, player, dt);
        drawSoundWave(gameManager, player, dt);
        drawPortraits(player);
        drawWarnings(gameManager);
        drawBulletTime(gameManager);
    }

    private void drawTopStatus(GameManager gameManager, Player player) {
        gc.setFont(new Font("Consolas", 22));
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("TIME %05.2f", gameManager.getRemainingTime()), 32, 42);

        for (int i = 0; i < player.getMaxHp(); i++) {
            gc.setFill(i < player.getHp() ? Color.web("#ff4f77") : Color.web("#35131d"));
            gc.fillText("♥", 32 + i * 28, 78);
        }

        gc.setFill(Color.web("#00ffcc"));
        gc.setFont(new Font("Consolas", 18));
        gc.fillText(String.format("RESCUE %d / %d", gameManager.getRescuedCount(), gameManager.getOriginalTotalIdolCount()), 32, 112);
    }

    private void drawBoost(Player player) {
        gc.setFont(new Font("Consolas", 14));
        gc.setFill(Color.web("#bdefff"));
        gc.fillText("BOOST", 32, 144);
        gc.setStroke(Color.web("#7ee7ff", 0.7));
        gc.strokeRect(96, 132, 170, 14);
        gc.setFill(player.getBoostEnergy() > 20 ? Color.web("#00d8ff") : Color.web("#ff334d"));
        gc.fillRect(98, 134, 166 * player.getBoostEnergy() / 100.0, 10);
    }

    private void drawRadar(GameManager gameManager, Player player, double dt) {
        double cx = 1130;
        double cy = 575;
        double inner = 54;
        double outer = 88;
        scanAngle += dt * 220;

        gc.save();
        gc.translate(cx, cy);
        gc.setStroke(Color.web("#00ffcc", 0.38));
        gc.setLineWidth(2);
        gc.strokeOval(-inner, -inner, inner * 2, inner * 2);
        gc.strokeOval(-outer, -outer, outer * 2, outer * 2);
        gc.setStroke(Color.web("#00ffcc", 0.16));
        gc.strokeLine(-outer, 0, outer, 0);
        gc.strokeLine(0, -outer, 0, outer);
        gc.setFill(Color.web("#00ffcc", 0.10));
        gc.fillArc(-inner, -inner, inner * 2, inner * 2, -scanAngle, 52, ArcType.ROUND);

        for (Enemy enemy : gameManager.getEnemies()) {
            drawDirectionalBlip(player, enemy.getPosition(), inner, Color.web("#ff315d", 0.9), 5);
        }
        for (GameObject obj : gameManager.getDynamicObjects()) {
            if (obj.getClass().getSimpleName().contains("EnemyBullet")) {
                drawDirectionalBlip(player, obj.getPosition(), inner, Color.web("#ff315d", 0.7), 3);
            }
        }

        Idol nearest = gameManager.getNearestAvailableIdol();
        if (nearest != null) {
            double distance = nearest.getPosition().distance(player.getPosition());
            double closeness = Math.max(0.15, 1.0 - Math.min(1.0, distance / 4500.0));
            pulsePhase += dt * (1.5 + closeness * 5.5);
            double pulseRadius = outer * (0.55 + (pulsePhase % 1.0) * 0.45);
            gc.setStroke(Color.web("#c8ff4d", 0.18 + closeness * 0.42));
            gc.setLineWidth(2 + closeness * 3);
            gc.strokeOval(-pulseRadius, -pulseRadius, pulseRadius * 2, pulseRadius * 2);
            drawDirectionalBlip(player, nearest.getPosition(), outer, Color.web("#d6ff54", 0.95), 6 + closeness * 5);
        }

        gc.setFill(Color.WHITE);
        gc.fillOval(-3, -3, 6, 6);

        // 繪製雷達正上方 (North) 藍色三角形，表示面朝方向
        gc.setFill(Color.web("#00bfff")); // 美觀的亮藍色
        gc.fillPolygon(new double[]{0, -6, 6}, new double[]{-outer - 8, -outer, -outer}, 3);

        gc.restore();

        gc.setFill(Color.web("#9efcff"));
        gc.setFont(new Font("Consolas", 13));
        gc.fillText("DANGER / IDOL RADAR", cx - 82, cy + 116);
    }

    private void drawDirectionalBlip(Player player, Vector2D target, double radius, Color color, double size) {
        Vector2D diff = target.subtract(player.getPosition());
        double angle = Math.atan2(diff.x, diff.y) - Math.toRadians(player.getRotationAngle());
        double x = Math.sin(angle) * radius * 0.82;
        double y = -Math.cos(angle) * radius * 0.82;
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private void drawSoundWave(GameManager gameManager, Player player, double dt) {
        Idol nearest = gameManager.getNearestAvailableIdol();
        if (nearest == null) return;
        double distance = nearest.getPosition().distance(player.getPosition());
        double strength = Math.max(0, 1.0 - Math.min(1.0, distance / 2400.0));
        if (strength <= 0.03) return;

        double baseX = 640;
        double baseY = 642;
        gc.setStroke(Color.web("#d6ff54", 0.3 + strength * 0.6));
        gc.setLineWidth(2);
        for (int i = 0; i < 5; i++) {
            double h = Math.sin((pulsePhase + i * 0.18) * Math.PI * 2) * 18 * strength;
            gc.strokeLine(baseX - 80 + i * 40, baseY - h, baseX - 64 + i * 40, baseY + h);
        }
        gc.setFill(Color.web("#e8ff9d"));
        gc.setFont(new Font("Consolas", 14));
        gc.fillText("VOICE LOCK: " + nearest.getDisplayName(), baseX - 96, baseY + 34);
    }

    private void drawPortraits(Player player) {
        double x = 32;
        double y = 672;
        int i = 0;
        for (Idol idol : player.getRescueGroup().getRescuedIdols()) {
            double px = x + i * 46;
            gc.setFill(idol.getState().name().equals("SINGING") ? Color.web("#fff58a") : Color.web("#55e6ff"));
            gc.fillOval(px, y - 28, 32, 32);
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Consolas", 12));
            gc.fillText(idol.getDisplayName().substring(0, 1), px + 11, y - 8);
            i++;
        }
    }

    private void drawWarnings(GameManager gameManager) {
        String warning = gameManager.getCombatWarningText();
        if (warning.isEmpty()) warning = gameManager.getRescueEventText();
        if (warning.isEmpty() && gameManager.isPlayerOutOfBounds()) warning = "你已脫離航道！";
        if (warning.isEmpty()) return;

        gc.setFill(Color.web("#ff315d"));
        gc.setFont(new Font("Consolas", 28));
        gc.fillText(warning, 430, 84);
    }

    private void drawBulletTime(GameManager gameManager) {
        if (gameManager.getCurrentState() != GameState.BULLET_TIME) return;
        gc.setFill(Color.web("#ffffff", 0.12));
        gc.fillRect(0, 0, hudCanvas.getWidth(), hudCanvas.getHeight());
        gc.setFill(Color.web("#d6ff54"));
        gc.setFont(new Font("Consolas", 34));
        gc.fillText("BULLET TIME // FINAL RESCUE", 390, 350);
    }
}
