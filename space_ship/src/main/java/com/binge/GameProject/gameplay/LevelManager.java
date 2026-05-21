package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.Planet;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private static final double PLANET_ORBIT_RADIUS_SCALE = 0.6;
    private static final double PLANET_ORBIT_PAUSE_DISTANCE = 3600.0;
    private static final double PLANET_ORBIT_RESUME_DISTANCE = 4300.0;
    private static final int ENEMIES_PER_PLANET = 4;
    private static final double ENEMY_CLUSTER_BASE_DISTANCE = 1250.0;
    private static final double ENEMY_CLUSTER_DISTANCE_STEP = 230.0;
    private final List<Planet> planets = new ArrayList<>();
    private final List<IdolGroup> idolGroups = new ArrayList<>();
    private final List<Idol> idols = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    public void buildDemoLevel(ObjectConsumer consumer) {
        planets.clear();
        idolGroups.clear();
        idols.clear();
        enemies.clear();

        Planet sun = new Planet(0, 0, 1000, 200000, Color.web("#ffaa00"));
        makeStarBrightAndOpaque(sun);
        addPlanet(sun, consumer);

        java.util.Random rand = new java.util.Random();
        double[][] coords = new double[3][2];
        boolean configOk = false;
        int configAttempts = 0;

        while (!configOk && configAttempts < 5000) {
            configAttempts++;
            // 隨機產生 3 個點
            for (int i = 0; i < 3; i++) {
                coords[i][0] = -8500.0 + rand.nextDouble() * 17000.0;
                coords[i][1] = -8500.0 + rand.nextDouble() * 17000.0;
            }

            // 檢查是否皆距離太陽 >= 4000.0
            boolean sunDistOk = true;
            for (int i = 0; i < 3; i++) {
                double d = Math.sqrt(coords[i][0] * coords[i][0] + coords[i][1] * coords[i][1]);
                if (d < 4000.0) {
                    sunDistOk = false;
                    break;
                }
            }
            if (!sunDistOk) continue;

            // 計算兩兩之間的距離
            double d01 = Math.sqrt(Math.pow(coords[0][0] - coords[1][0], 2) + Math.pow(coords[0][1] - coords[1][1], 2));
            double d02 = Math.sqrt(Math.pow(coords[0][0] - coords[2][0], 2) + Math.pow(coords[0][1] - coords[2][1], 2));
            double d12 = Math.sqrt(Math.pow(coords[1][0] - coords[2][0], 2) + Math.pow(coords[1][1] - coords[2][1], 2));

            double minDist = Math.min(d01, Math.min(d02, d12));

            // 距離限制：兩兩之間最少 6750.0 距離，且最近的兩個行星距離在 6750 到 8500 之間
            if (minDist >= 6750.0 && minDist <= 8500.0) {
                configOk = true;
            }
        }

        // 防呆回退預設值
        if (!configOk) {
            coords[0][0] = 5000; coords[0][1] = 0;
            coords[1][0] = -2000; coords[1][1] = 5000;
            coords[2][0] = -3000; coords[2][1] = -3000;
        }
        scalePlanetOrbitRadii(coords, PLANET_ORBIT_RADIUS_SCALE);

        Planet aurora = new Planet(coords[0][0], coords[0][1], 267, 500000, Color.web("#3ec7ff"));
        Planet neon = new Planet(coords[1][0], coords[1][1], 210, 300000, Color.web("#ff4f9a"));
        Planet lunar = new Planet(coords[2][0], coords[2][1], 160, 120000, Color.web("#d8d8e8"));
        aurora.enableOrbitAround(sun, 0.70);
        neon.enableOrbitAround(sun, -0.52);
        lunar.enableOrbitAround(sun, 0.43);

        addPlanet(aurora, consumer);
        addPlanet(neon, consumer);
        addPlanet(lunar, consumer);

        createGroup("AURORA", "aurora-hook", aurora, Color.web("#54f4ff"),
                new String[]{"Mina", "Sora", "Lumi"},
                new double[][]{{380, 42, 11}, {470, 156, -8}, {560, 285, 6}},
                "/photo/a.jpg", "/pop_musics/supernova.mp3", consumer);
        createGroup("NEON", "neon-chorus", neon, Color.web("#ff72bb"),
                new String[]{"Rin", "Nana", "Yuki"},
                new double[][]{{330, 22, -10}, {420, 135, 7}, {510, 264, -5}},
                "/photo/b.jpg", "/pop_musics/supernova.mp3", consumer);
        createGroup("LUNAR", "lunar-bridge", lunar, Color.web("#d7ff7a"),
                new String[]{"Hana", "Mei"},
                new double[][]{{280, 65, 9}, {360, 236, -7}},
                "/photo/c.jpg", "/pop_musics/supernova.mp3", consumer);

        // 為每顆行星建立多座外側衛星砲塔，讓救援路線穿過更密集的防線。
        for (int i = 0; i < 3; i++) {
            addEnemyClusterAroundPlanet(coords[i][0], coords[i][1], rand, consumer);
        }
    }

    private void addEnemyClusterAroundPlanet(double planetX, double planetY, java.util.Random rand, ObjectConsumer consumer) {
        double outwardAngle = Math.atan2(planetY, planetX);
        double[] fanOffsets = {-72.0, -24.0, 24.0, 72.0};

        for (int i = 0; i < ENEMIES_PER_PLANET; i++) {
            double angle = outwardAngle + Math.toRadians(fanOffsets[i]) + Math.toRadians(rand.nextDouble() * 12.0 - 6.0);
            double enemyDist = ENEMY_CLUSTER_BASE_DISTANCE + ENEMY_CLUSTER_DISTANCE_STEP * i + rand.nextDouble() * 90.0;
            double ex = planetX + Math.cos(angle) * enemyDist;
            double ey = planetY + Math.sin(angle) * enemyDist;
            addEnemy(new Enemy(ex, ey), consumer);
        }
    }

    private void addPlanet(Planet planet, ObjectConsumer consumer) {
        planets.add(planet);
        consumer.add(planet);
    }

    private void addEnemy(Enemy enemy, ObjectConsumer consumer) {
        enemies.add(enemy);
        consumer.add(enemy);
    }

    public void updatePlanetOrbitAvoidance() {
        List<Planet> pausedPlanets = new ArrayList<>();

        for (int i = 0; i < planets.size(); i++) {
            Planet a = planets.get(i);
            if (!a.hasOrbit()) continue;
            for (int j = i + 1; j < planets.size(); j++) {
                Planet b = planets.get(j);
                if (!b.hasOrbit()) continue;
                double distance = a.getPosition().distance(b.getPosition());
                if (distance < PLANET_ORBIT_PAUSE_DISTANCE) {
                    markPlanetPaused(pausedPlanets, chooseBehindPlanet(a, b));
                } else if (distance < PLANET_ORBIT_RESUME_DISTANCE) {
                    if (a.isOrbitPaused()) markPlanetPaused(pausedPlanets, a);
                    if (b.isOrbitPaused()) markPlanetPaused(pausedPlanets, b);
                }
            }
        }

        for (Planet planet : planets) {
            if (planet.hasOrbit()) {
                planet.setOrbitPaused(pausedPlanets.contains(planet));
            }
        }
    }

    private void markPlanetPaused(List<Planet> pausedPlanets, Planet planet) {
        if (!pausedPlanets.contains(planet)) {
            pausedPlanets.add(planet);
        }
    }

    private Planet chooseBehindPlanet(Planet a, Planet b) {
        double aProgress = normalizedOrbitProgress(a);
        double bProgress = normalizedOrbitProgress(b);
        return aProgress <= bProgress ? a : b;
    }

    private double normalizedOrbitProgress(Planet planet) {
        double angle = planet.getOrbitAngle() % 360.0;
        if (angle < 0) angle += 360.0;
        return planet.getOrbitSpeed() >= 0 ? angle : 360.0 - angle;
    }

    private void scalePlanetOrbitRadii(double[][] coords, double scale) {
        for (double[] coord : coords) {
            coord[0] *= scale;
            coord[1] *= scale;
        }
    }

    private void makeStarBrightAndOpaque(Planet star) {
        if (!(star.getView() instanceof Sphere sphere)) return;
        Color warmOrange = Color.web("#ff8a00");
        PhongMaterial material = new PhongMaterial(warmOrange);
        material.setSpecularColor(Color.web("#fff1b8"));
        material.setSelfIlluminationMap(createSolidImage(warmOrange));
        sphere.setMaterial(material);
        sphere.setOpacity(1.0);
    }

    private WritableImage createSolidImage(Color color) {
        WritableImage image = new WritableImage(2, 2);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                image.getPixelWriter().setColor(x, y, color);
            }
        }
        return image;
    }

    private void createGroup(String groupId, String songId, Planet planet, Color color, String[] names,
                             double[][] orbitData, String texturePath, String songPath, ObjectConsumer consumer) {
        IdolGroup group = new IdolGroup(groupId, songId, texturePath, songPath);
        for (int i = 0; i < names.length; i++) {
            Idol idol = new Idol(groupId + "-" + i, groupId, names[i], planet,
                    orbitData[i][0], orbitData[i][1], orbitData[i][2], color, texturePath);
            group.add(idol);
            idols.add(idol);
            consumer.add(idol);
        }
        idolGroups.add(group);
    }

    public List<Planet> getPlanets() { return planets; }
    public List<IdolGroup> getIdolGroups() { return idolGroups; }
    public List<Idol> getIdols() { return idols; }
    public List<Enemy> getEnemies() { return enemies; }
    public int getOriginalTotalIdolCount() { return idols.size(); }

    public interface ObjectConsumer {
        void add(com.binge.GameProject.model.GameObject object);
    }
}
