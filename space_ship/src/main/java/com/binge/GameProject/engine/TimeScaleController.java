package com.binge.GameProject.engine;

public class TimeScaleController {
    private double timeScale = 1.0;
    private double bulletTimeTimer = 0.0;
    private boolean bulletTimeFinished = false;

    public void startBulletTime(double duration) {
        timeScale = 0.1;
        bulletTimeTimer = duration;
        bulletTimeFinished = false;
    }

    public void update(double dt) {
        if (bulletTimeTimer > 0) {
            bulletTimeTimer -= dt;
            if (bulletTimeTimer <= 0) {
                timeScale = 1.0;
                bulletTimeFinished = true;
            }
        }
    }

    public void reset() {
        timeScale = 1.0;
        bulletTimeTimer = 0.0;
        bulletTimeFinished = false;
    }

    public double getTimeScale() {
        return timeScale;
    }

    public boolean isBulletTimeFinished() {
        return bulletTimeFinished;
    }
}
