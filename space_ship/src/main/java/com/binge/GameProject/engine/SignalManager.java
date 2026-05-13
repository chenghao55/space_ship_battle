package com.binge.GameProject.engine;

import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.OrbitalBase;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.Vector2D;

import java.util.List;

// SignalManager 計算並管理玩家到敵方據點的雷達訊號強度與方向
public class SignalManager {
    // 單例模式 (Singleton)
    private static SignalManager instance = new SignalManager();
    public static SignalManager getInstance() { return instance; }

    private double signalStrength = 0.0; // 訊號強度 0.0 ~ 1.0
    private double signalDirection = 0.0; // 目標方向 (角度，相對於玩家機頭)
    private double glitchLevel = 0.0; // 雜訊干擾程度 0.0 ~ 1.0

    // 更新訊號
    public void updateSignals(Player player, List<GameObject> staticObjects) {
        if (player == null) return;

        OrbitalBase targetBase = null;
        for (GameObject obj : staticObjects) {
            if (obj instanceof OrbitalBase) {
                targetBase = (OrbitalBase) obj;
                break; // 為了簡化，先追蹤第一個找到的據點
            }
        }

        if (targetBase == null) {
            signalStrength = 0;
            glitchLevel = 0;
            return;
        }

        // 計算距離
        Vector2D diff = targetBase.getPosition().subtract(player.getPosition());
        double distSq = diff.magnitudeSquared();
        double dist = Math.sqrt(distSq);

        // 1. 距離衰減 (距離越近，訊號越強)
        double maxDist = 25000.0; // 雷達最大偵測距離
        double baseStrength = 1.0 - Math.min(dist / maxDist, 1.0);

        // 2. 方向衰減 (機頭對準目標時訊號最強)
        Vector2D dirToBase = diff.normalize();
        double rad = Math.toRadians(player.getRotationAngle());
        Vector2D playerForward = new Vector2D(Math.sin(rad), Math.cos(rad));
        
        // Dot product 計算夾角: 1 (完全對齊), 0 (垂直), -1 (背對)
        double dot = playerForward.dot(dirToBase);
        // 將 -1 ~ 1 映射到 0.2 ~ 1.0，即使背對也有微弱訊號
        double alignmentFactor = 0.2 + 0.8 * ((dot + 1.0) / 2.0);
        
        signalStrength = baseStrength * alignmentFactor;

        // 3. 計算目標相對於畫面的方向 (用於雷達指標)
        // atan2 取得絕對角度
        double targetAngle = Math.toDegrees(Math.atan2(dirToBase.x, dirToBase.y));
        signalDirection = targetAngle - player.getRotationAngle();
        while (signalDirection < -180) signalDirection += 360;
        while (signalDirection > 180) signalDirection -= 360;

        // 4. 干擾與雜訊 (靠近行星或電磁異常區)
        glitchLevel = 0;
        for (GameObject obj : staticObjects) {
            if (obj instanceof Planet) {
                Planet p = (Planet) obj;
                double pDist = player.getPosition().subtract(p.getPosition()).magnitude();
                if (pDist < p.getRadius() + 1500) { // 靠近行星時產生干擾
                    double intensity = 1.0 - ((pDist - p.getRadius()) / 1500.0);
                    if (intensity > glitchLevel) glitchLevel = intensity;
                }
            }
        }
        
        // 極度靠近據點時，也會因為能量場產生強烈干擾
        if (dist < 2000) {
            double baseGlitch = 1.0 - (dist / 2000.0);
            if (baseGlitch > glitchLevel) glitchLevel = baseGlitch;
        }
    }

    public double getSignalStrength() { return signalStrength; }
    public double getSignalDirection() { return signalDirection; }
    public double getGlitchLevel() { return glitchLevel; }
}
