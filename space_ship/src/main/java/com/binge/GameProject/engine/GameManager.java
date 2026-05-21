package com.binge.GameProject.engine;

import com.binge.GameProject.gameplay.CombatManager;
import com.binge.GameProject.gameplay.LevelManager;
import com.binge.GameProject.gameplay.MissionManager;
import com.binge.GameProject.gameplay.RescueManager;
import com.binge.GameProject.gameplay.ScoreManager;
import com.binge.GameProject.gameplay.ScoreResult;
import com.binge.GameProject.audio.AudioSystem;
import com.binge.GameProject.model.Bullet;
import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.GameObject;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.model.Player;
import com.binge.GameProject.physics.PhysicsEngine;
import com.binge.GameProject.physics.Vector2D;
import com.binge.GameProject.rendering.CameraManager;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameManager {
    private final List<GameObject> dynamicObjects = new ArrayList<>();
    private final List<GameObject> staticObjects = new ArrayList<>();
    private final List<GameObject> objectsToAdd = new ArrayList<>();

    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private final TimeScaleController timeScaleController = new TimeScaleController();
    private final LevelManager levelManager = new LevelManager();
    private final RescueManager rescueManager = new RescueManager();
    private final CombatManager combatManager = new CombatManager();
    private final MissionManager missionManager = new MissionManager();
    private final ScoreManager scoreManager = new ScoreManager();
    private final AudioSystem audioSystem = new AudioSystem();

    private GameState currentState = GameState.MAIN_MENU;
    private Player player;
    private final Group worldRoot;
    private final CameraManager cameraManager;
    private double remainingTime = 180.0;
    private double endingFreezeTimer = 0.0;
    private ScoreResult scoreResult;

    public GameManager(Group worldRoot, CameraManager cameraManager) {
        this.worldRoot = worldRoot;
        this.cameraManager = cameraManager;
        initWorld();
    }

    private void initWorld() {
        player = new Player(0, -9000, this);
        addGameObject(player);
        levelManager.buildDemoLevel(this::addGameObject);
        remainingTime = 180.0;
        missionManager.reset();
        timeScaleController.reset();
        scoreResult = null;
    }

    public void update(double dt) {
        if (dt > 0.05) dt = 0.05;
        flushObjectQueues();

        if (currentState == GameState.MAIN_MENU || currentState == GameState.STARTING_TRANSITION
                || currentState == GameState.MISSION_CLEAR || currentState == GameState.PAUSED) {
            updateVisualOnly(dt);
            return;
        }

        if (currentState == GameState.ENDING_FREEZE) {
            endingFreezeTimer -= dt;
            if (endingFreezeTimer <= 0) {
                finishMission();
            }
            return;
        }

        if (currentState == GameState.PLAYING) {
            remainingTime = Math.max(0, remainingTime - dt);
            if (remainingTime <= 0) {
                finishMission();
                return;
            }
        }

        timeScaleController.update(dt);
        double scaledDt = dt * timeScaleController.getTimeScale();

        levelManager.updatePlanetOrbitAvoidance();
        updateObjects(scaledDt);
        rescueManager.update(player, levelManager.getIdols(), scaledDt);
        audioSystem.updateIdolGroupVoices(levelManager.getIdolGroups(), player, scaledDt);

        // 更新推力音效
        boolean isAcc = InputManager.getInstance().isPressed("W") || InputManager.getInstance().isPressed("UP");
        audioSystem.updateEngineSound(isAcc, player.isBoosting());

        // 更新敵人接近 3D 環繞感應音
        audioSystem.updateEnemyAmbient(levelManager.getEnemies(), player);

        combatManager.update(player, levelManager.getEnemies(), dynamicObjects, staticObjects, scaledDt,
                this::addGameObject,
                () -> audioSystem.playLostVoice(rescueManager.loseLastRescued(player)),
                cameraManager);
        applyBoundarySystem(scaledDt);
        physicsEngine.updatePhysics(dynamicObjects, staticObjects, scaledDt);
        removeDeadObjects();

        if (!player.isAlive()) {
            audioSystem.playExplosion();
            currentState = GameState.GAME_OVER;
            finishMission();
            return;
        }

        if (currentState == GameState.PLAYING && missionManager.shouldStartBulletTime(levelManager.getIdols(), player.getHp())) {
            currentState = GameState.ENDING_FREEZE;
            endingFreezeTimer = 0.15;
            audioSystem.playEndingImpact();
            if (cameraManager != null) cameraManager.addCameraShake(8, 0.5);
        }

        if (currentState == GameState.BULLET_TIME && timeScaleController.isBulletTimeFinished()) {
            finishMission();
        }
    }

    private void flushObjectQueues() {
        for (GameObject obj : objectsToAdd) {
            if (obj instanceof Player || obj instanceof Bullet) {
                dynamicObjects.add(obj);
            } else {
                staticObjects.add(obj);
            }
            if (obj.getView() != null && !worldRoot.getChildren().contains(obj.getView())) {
                worldRoot.getChildren().add(obj.getView());
            }
        }
        objectsToAdd.clear();
    }

    private void updateVisualOnly(double dt) {
        for (GameObject obj : staticObjects) {
            if (obj instanceof Planet) obj.update(dt);
        }
    }

    private void updateObjects(double dt) {
        for (GameObject obj : dynamicObjects) {
            obj.update(dt);
        }
        for (GameObject obj : staticObjects) {
            obj.update(dt);
        }
    }

    private void removeDeadObjects() {
        removeDeadFrom(dynamicObjects);
        removeDeadFrom(staticObjects);
        levelManager.getEnemies().removeIf(enemy -> !enemy.isAlive());
    }

    private void removeDeadFrom(List<GameObject> objects) {
        List<GameObject> dead = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj.isDead()) dead.add(obj);
        }
        for (GameObject obj : dead) {
            objects.remove(obj);
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
    }

    private void applyBoundarySystem(double dt) {
        double limit = 10200.0;
        Vector2D pos = player.getPosition();
        if (Math.abs(pos.x) > limit || Math.abs(pos.y) > limit) {
            Vector2D pull = new Vector2D(-pos.x, -pos.y).normalize().multiply(600 * dt);
            player.getVelocity().addMut(pull);
            player.getVelocity().multiplyMut(0.985);
        }
    }

    private void finishMission() {
        scoreResult = scoreManager.calculate(getRescuedCount(), getOriginalTotalIdolCount(), getLostCount(), player.getHp());
        currentState = GameState.MISSION_CLEAR;
        audioSystem.stopAll();
    }

    public void addGameObject(GameObject obj) {
        objectsToAdd.add(obj);
    }

    public void resetGame() {
        for (GameObject obj : dynamicObjects) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        for (GameObject obj : staticObjects) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        for (GameObject obj : objectsToAdd) {
            if (obj.getView() != null) worldRoot.getChildren().remove(obj.getView());
        }
        dynamicObjects.clear();
        staticObjects.clear();
        objectsToAdd.clear();
        initWorld();
        flushObjectQueues();
        currentState = GameState.PLAYING;
    }

    public Idol getNearestAvailableIdol() {
        return levelManager.getIdols().stream()
                .filter(Idol::isAvailable)
                .min(Comparator.comparingDouble(idol -> idol.getPosition().distance(player.getPosition())))
                .orElse(null);
    }

    public List<GameObject> getStaticObjects() { return staticObjects; }
    public List<GameObject> getDynamicObjects() { return dynamicObjects; }
    public List<Planet> getPlanets() { return levelManager.getPlanets(); }
    public List<Idol> getAllIdols() { return levelManager.getIdols(); }
    public List<IdolGroup> getIdolGroups() { return levelManager.getIdolGroups(); }
    public List<Enemy> getEnemies() { return levelManager.getEnemies(); }
    public AudioSystem getAudioSystem() { return audioSystem; }
    public Player getPlayer() { return player; }
    public Group getWorldRoot() { return worldRoot; }
    public GameState getCurrentState() { return currentState; }
    public double getRemainingTime() { return remainingTime; }
    public double getTimeScale() { return timeScaleController.getTimeScale(); }
    public int getOriginalTotalIdolCount() { return levelManager.getOriginalTotalIdolCount(); }
    public int getRescuedCount() { return player.getRescueGroup().size(); }
    public int getLostCount() { return (int) levelManager.getIdols().stream().filter(Idol::isPermanentlyLost).count(); }
    public String getRescueEventText() { return rescueManager.getEventText(); }
    public String getCombatWarningText() { return combatManager.getWarningText(); }

    public boolean isPlayerOutOfBounds() {
        return Math.abs(player.getPosition().x) > 10200 || Math.abs(player.getPosition().y) > 10200;
    }

    public ScoreResult getScoreResult() {
        if (scoreResult == null) {
            scoreResult = scoreManager.calculate(getRescuedCount(), getOriginalTotalIdolCount(), getLostCount(), player.getHp());
        }
        return scoreResult;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
    }
}
