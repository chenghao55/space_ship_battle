package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.MobileEnemy;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.utils.GameConfig;
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
    private static final int ENEMIES_PER_PLANET = 3;
    private static final int MOBILE_ENEMY_COUNT = GameConfig.MOVING_ENEMY_COUNT;
    private static final double ENEMY_CLUSTER_BASE_DISTANCE = 2600.0;
    private static final double ENEMY_CLUSTER_DISTANCE_STEP = 900.0;
    private static final String DEMO_SONG_PATH = "/pop_musics/supernova.mp3";
    private static final double PLAYER_SAFE_START_X = GameConfig.PLAYER_START_X;
    private static final double PLAYER_SAFE_START_Y = GameConfig.PLAYER_START_Y;
    private static final double PLAYER_SAFE_ENEMY_RADIUS = 2300.0;
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
        double[][] positions = createEvenlyDistributedPlanetPositions();

        Planet aurora = new Planet(positions[0][0], positions[0][1], 267, 500000, Color.web("#3ec7ff"));
        Planet neon = new Planet(positions[1][0], positions[1][1], 210, 300000, Color.web("#ff4f9a"));
        Planet lunar = new Planet(positions[2][0], positions[2][1], 160, 120000, Color.web("#d8d8e8"));
        Planet verdant = new Planet(positions[3][0], positions[3][1], 320, 520000, Color.web("#2ee66b"));
        Planet violet = new Planet(positions[4][0], positions[4][1], 360, 560000, Color.web("#8f5cff"));
        Planet ember = new Planet(positions[5][0], positions[5][1], 410, 610000, Color.web("#f2552c"));
        Planet cobalt = new Planet(positions[6][0], positions[6][1], 470, 680000, Color.web("#3b5bff"));
        aurora.enableOrbitAround(sun, 0.70);
        neon.enableOrbitAround(sun, -0.52);
        lunar.enableOrbitAround(sun, 0.43);
        verdant.enableOrbitAround(sun, -0.26);
        violet.enableOrbitAround(sun, 0.22);
        ember.enableOrbitAround(sun, -0.18);
        cobalt.enableOrbitAround(sun, 0.15);

        List<Planet> guardedPlanets = new ArrayList<>();

        addPlanet(aurora, consumer);
        addPlanet(neon, consumer);
        addPlanet(lunar, consumer);
        addPlanet(verdant, consumer);
        addPlanet(violet, consumer);
        addPlanet(ember, consumer);
        addPlanet(cobalt, consumer);
        guardedPlanets.add(aurora);
        guardedPlanets.add(neon);
        guardedPlanets.add(lunar);
        guardedPlanets.add(verdant);
        guardedPlanets.add(violet);
        guardedPlanets.add(ember);
        guardedPlanets.add(cobalt);

        createGroup("AURORA", "aurora-hook", aurora, Color.web("#54f4ff"),
                new String[]{"Mina", "Sora", "Lumi"},
                new double[][]{{380, 42, 11}, {470, 156, -8}, {560, 285, 6}},
                "/photo/a.jpg", DEMO_SONG_PATH, consumer);
        createGroup("NEON", "neon-chorus", neon, Color.web("#ff72bb"),
                new String[]{"Rin", "Nana", "Yuki"},
                new double[][]{{330, 22, -10}, {420, 135, 7}, {510, 264, -5}},
                "/photo/b.jpg", DEMO_SONG_PATH, consumer);
        createGroup("LUNAR", "lunar-bridge", lunar, Color.web("#d7ff7a"),
                new String[]{"Hana", "Mei"},
                new double[][]{{280, 65, 9}, {360, 236, -7}},
                "/photo/c.jpg", DEMO_SONG_PATH, consumer);
        createGroup("VERDANT", "verdant-rise", verdant, Color.web("#7dff96"),
                new String[]{"Aki", "Nori"},
                new double[][]{{520, 18, 5}, {650, 205, -4}},
                "/photo/d.jpg", DEMO_SONG_PATH, consumer);
        createGroup("VIOLET", "violet-wave", violet, Color.web("#c7a6ff"),
                new String[]{"Rika", "Ena"},
                new double[][]{{560, 76, -5}, {700, 248, 4}},
                "/photo/e.jpg", DEMO_SONG_PATH, consumer);
        createGroup("EMBER", "ember-spark", ember, Color.web("#ff9b6b"),
                new String[]{"Kira", "Noa"},
                new double[][]{{610, 116, 4}, {760, 302, -4}},
                "/photo/a.jpg", DEMO_SONG_PATH, consumer);
        createGroup("COBALT", "cobalt-drift", cobalt, Color.web("#8fb4ff"),
                new String[]{"Mio", "Rei"},
                new double[][]{{690, 34, -3}, {830, 221, 3}},
                "/photo/b.jpg", DEMO_SONG_PATH, consumer);

        // 為每顆非恆星行星建立多座外側衛星砲塔，讓救援路線穿過更密集的防線。
        for (Planet planet : guardedPlanets) {
            addEnemyClusterAroundPlanet(planet.getPosition().x, planet.getPosition().y, rand, consumer);
        }
        addMobileEnemies(guardedPlanets, consumer);
    }

    private void addEnemyClusterAroundPlanet(double planetX, double planetY, java.util.Random rand, ObjectConsumer consumer) {
        double outwardAngle = Math.atan2(planetY, planetX);
        double[] fanOffsets = {-140.0, 0.0, 140.0};

        for (int i = 0; i < ENEMIES_PER_PLANET; i++) {
            double angle = outwardAngle + Math.toRadians(fanOffsets[i]) + Math.toRadians(rand.nextDouble() * 12.0 - 6.0);
            double enemyDist = ENEMY_CLUSTER_BASE_DISTANCE + ENEMY_CLUSTER_DISTANCE_STEP * i + rand.nextDouble() * 90.0;
            double ex = planetX + Math.cos(angle) * enemyDist;
            double ey = planetY + Math.sin(angle) * enemyDist;
            if (distance(ex, ey, PLAYER_SAFE_START_X, PLAYER_SAFE_START_Y) < PLAYER_SAFE_ENEMY_RADIUS) {
                angle = outwardAngle + Math.PI + Math.toRadians(fanOffsets[i] * 0.35);
                ex = planetX + Math.cos(angle) * enemyDist;
                ey = planetY + Math.sin(angle) * enemyDist;
            }
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

    private void addMobileEnemies(List<Planet> guardedPlanets, ObjectConsumer consumer) {
        for (int added = 0; added < MOBILE_ENEMY_COUNT; added++) {
            Planet planet = guardedPlanets.get(added % guardedPlanets.size());
            double outwardAngle = Math.atan2(planet.getPosition().y, planet.getPosition().x);
            double spreadAngle = Math.toRadians((added * 137.5) % 360.0);
            double sideOffset = (added % 2 == 0 ? 1.0 : -1.0) * Math.PI * 0.34;
            double angle = outwardAngle + sideOffset + spreadAngle * 0.22;
            double distance = 2300.0 + (added % 4) * 720.0 + (added / guardedPlanets.size()) * 520.0;
            double x = planet.getPosition().x + Math.cos(angle) * distance;
            double y = planet.getPosition().y + Math.sin(angle) * distance;
            if (distance(x, y, PLAYER_SAFE_START_X, PLAYER_SAFE_START_Y) < PLAYER_SAFE_ENEMY_RADIUS) {
                angle += Math.PI;
                x = planet.getPosition().x + Math.cos(angle) * distance;
                y = planet.getPosition().y + Math.sin(angle) * distance;
            }
            addEnemy(new MobileEnemy(x, y), consumer);
        }
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

    private double[][] createEvenlyDistributedPlanetPositions() {
        double[] radii = {
                5600.0,
                6500.0,
                7500.0,
                8600.0,
                9800.0,
                11100.0,
                12500.0
        };
        double[] angles = {25.0, 78.0, 132.0, 186.0, 238.0, 294.0, 345.0};
        double[][] positions = new double[radii.length][2];

        for (int i = 0; i < radii.length; i++) {
            double rad = Math.toRadians(angles[i]);
            positions[i][0] = Math.cos(rad) * radii[i];
            positions[i][1] = Math.sin(rad) * radii[i];
        }
        return positions;
    }

    private double distance(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return Math.sqrt(dx * dx + dy * dy);
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
