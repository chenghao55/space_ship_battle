package com.binge.GameProject.physics;

import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.Bullet;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.model.Player;

import java.util.List;

// PhysicsEngine 負責計算遊戲中所有的引力與移動
public class PhysicsEngine {
    // 引力常數：數字越大，星球的吸力就越強 (可以隨時調整來改變遊戲手感)
    public static final double GRAVITATIONAL_CONSTANT = 2500.0; // 引力大幅調小
    
    // 宇宙阻力 (摩擦力)：因為太空是真空的，所以我們設定一個非常接近 1 的數字
    // 0.995 代表每幀會保留 99.5% 的速度，讓飛船能有「慢慢滑行、很難剎車」的漂移感
    public static final double DRAG = 0.995; 

    // 每幀由 GameManager 呼叫，傳入所有的物體以及經過的時間 (dt)
    public void updatePhysics(List<GameObject> dynamicObjects, List<GameObject> staticObjects, double dt) {
        // 針對每一個會動的物體 (例如飛船) 計算物理
        for (GameObject obj : dynamicObjects) {
            if (obj.isDead()) continue;

            if (obj instanceof Bullet bullet) {
                updateBulletWithoutPlanetForces(bullet, staticObjects, dt);
                continue;
            }
            
            // 用來收集「所有星球」加總起來的引力總和
            Vector2D totalGravity = new Vector2D(0, 0);
            
            // 檢查每一個靜止的物體 (例如星球)
            for (GameObject staticObj : staticObjects) {
                // 如果這個靜止物體是一顆星球
                if (staticObj instanceof Planet) {
                    Planet p = (Planet) staticObj;
                    
                    // 計算星球和飛船之間的距離向量 (星球位置 減去 飛船位置，方向朝向飛船)
                    // 等等，我們把 diff 變成從星球指向飛船的向量，也就是 diff = obj.getPosition() - p.getPosition()
                    Vector2D diff = obj.getPosition().subtract(p.getPosition());
                    double distSq = diff.magnitudeSquared();
                    double dist = Math.sqrt(distSq);

                    // 設定一個危險邊界距離 (力場半徑縮小為現在的一半，也就是星球半徑 + 50)
                    double dangerRadius = p.getRadius() + 50;
                    
                    // 為了引力方向正確，我們需要「朝向星球」的向量 (也就是 -diff.normalize())
                    Vector2D dirToPlanet = diff.normalize().multiply(-1);
                    
                    // 1. 分層引力區塊：計算引力與極限封頂 (Clamp Max Gravity)
                    double calcDist = Math.max(dist, dangerRadius);
                    double force = (GRAVITATIONAL_CONSTANT * p.getMass()) / (calcDist * calcDist);
                    
                    // 封頂最大引力，確保玩家永遠可以用 Boost 逃脫
                    double maxForce = 150.0; // 根據手感調整 (減半)
                    if (force > maxForce) force = maxForce;
                    
                    // (a) 傳統向心引力
                    Vector2D gravityAccel = dirToPlanet.multiply(force);
                    totalGravity.addMut(gravityAccel);
                    
                    // (b) 街機風重力彈弓效應 (Arcade Gravity Slingshot)
                    // 計算垂直於質心連線的切線向量
                    Vector2D tangent = new Vector2D(-dirToPlanet.y, dirToPlanet.x);
                    
                    // 確保切線向量的方向與飛船當前速度方向大致相同 (內積 > 0)
                    if (tangent.dot(obj.getVelocity()) < 0) {
                        tangent = tangent.multiply(-1);
                    }
                    
                    // 當飛船處於移動狀態時，給予與引力量質成正比的固定切線速度加成
                    if (obj.getVelocity().magnitudeSquared() > 1.0) {
                        double slingshotFactor = 1.5; // 切線加速倍率
                        Vector2D slingshotAccel = tangent.multiply(force * slingshotFactor);
                        totalGravity.addMut(slingshotAccel);
                    }
                    
                    // 2. 危險掠過區 (Danger Zone) 或 排斥力場 (Repulsive Force Field)
                    if (dist < dangerRadius) {
                        // 強制把飛船的位置拉回 dangerRadius 的邊界，這樣飛船就「絕對無法再靠近」
                        Vector2D dirFromPlanet = diff.normalize();
                        obj.getPosition().set(p.getPosition().add(dirFromPlanet.multiply(dangerRadius)));
                        
                        // 將朝向星球內部的速度分量反轉 (彈回)，或者直接消除
                        Vector2D vel = obj.getVelocity();
                        double dotProduct = vel.dot(dirFromPlanet);
                        if (dotProduct < 0) {
                            // 如果速度是朝向星球的，就消除這個法線分量 (沿著力場表面滑行)
                            vel.subtractMut(dirFromPlanet.multiply(dotProduct));
                        }
                        
                        // (c) 觸發過熱或受損狀態
                        if (obj instanceof Player player) {
                            player.takeDangerDamage(dt);
                            if (dist < p.getRadius() + 40) {
                                player.takePlanetCollisionDamage();
                            }
                        }
                    }
                }
            }

            // 將所有星球的總引力，加到飛船的加速度上 (飛船自己的引擎推力已經先加在 acceleration 裡面了)
            obj.getAcceleration().addMut(totalGravity);
            
            // 根據國中物理：速度 = 原速度 + 加速度 * 時間 (v = v0 + a * dt)
            obj.getVelocity().addMut(obj.getAcceleration().multiply(dt));
            
            // 模擬宇宙漂移的些微阻力，讓飛船不會無限加速到飛出宇宙
            obj.getVelocity().multiplyMut(DRAG); 
            
            // 根據國中物理：位置 = 原位置 + 速度 * 時間 (p = p0 + v * dt)
            obj.getPosition().addMut(obj.getVelocity().multiply(dt));
            
            // 每一幀的最後，必須把加速度歸零，否則上一幀的推力和引力會一直殘留到下一幀
            obj.getAcceleration().set(0, 0);
            
            // 同步將剛算好的物理位置，更新到 JavaFX 3D 畫面的模型上
            obj.updateView();
        }
    }

    private void updateBulletWithoutPlanetForces(Bullet bullet, List<GameObject> staticObjects, double dt) {
        Vector2D start = new Vector2D(bullet.getPosition().x, bullet.getPosition().y);
        Vector2D end = start.add(bullet.getVelocity().multiply(dt));

        for (GameObject staticObj : staticObjects) {
            if (staticObj instanceof Planet planet && segmentIntersectsCircle(start, end, planet.getPosition(), planet.getRadius() + bullet.getRadius())) {
                bullet.setDead(true);
                return;
            }
        }

        bullet.getPosition().set(end);
        bullet.getAcceleration().set(0, 0);
        bullet.updateView();
    }

    private boolean segmentIntersectsCircle(Vector2D start, Vector2D end, Vector2D center, double radius) {
        Vector2D segment = end.subtract(start);
        double lengthSq = segment.magnitudeSquared();
        if (lengthSq == 0) {
            return start.distance(center) <= radius;
        }

        double t = center.subtract(start).dot(segment) / lengthSq;
        t = Math.max(0, Math.min(1, t));
        Vector2D closest = start.add(segment.multiply(t));
        return closest.distance(center) <= radius;
    }
}
