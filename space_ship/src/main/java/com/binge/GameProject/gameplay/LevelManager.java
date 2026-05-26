package com.binge.GameProject.gameplay;

import com.binge.GameProject.model.Enemy;
import com.binge.GameProject.model.Idol;
import com.binge.GameProject.model.IdolGroup;
import com.binge.GameProject.model.MobileEnemy;
import com.binge.GameProject.model.Planet;
import com.binge.GameProject.utils.GameConfig;
import com.binge.GameProject.utils.GroupConfig;
import com.binge.GameProject.utils.TextureRegistry;
import javafx.scene.image.Image;
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
    private static final int[] TURRET_COUNT_PATTERN = {1, 2};
    private static final int MOBILE_ENEMY_COUNT = GameConfig.MOVING_ENEMY_COUNT;
    private static final double ENEMY_CLUSTER_BASE_DISTANCE = 2600.0;
    private static final double ENEMY_CLUSTER_DISTANCE_STEP = 900.0;
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
        makeStarBrightAndOpaque(sun, null);
        addPlanet(sun, consumer);

        java.util.Random rand = new java.util.Random();
        double[][] positions = createEvenlyDistributedPlanetPositions();

        List<Planet> guardedPlanets = new ArrayList<>();
        List<GroupConfig> groupConfigs = GroupConfig.defaults();
        double[] planetRadii = {267, 210, 160, 320, 360, 410, 470, 380};
        double[] planetMasses = {500000, 300000, 120000, 520000, 560000, 610000, 680000, 580000};
        Color[] groupColors = {
                Color.web("#54f4ff"),
                Color.web("#ff72bb"),
                Color.web("#d7ff7a"),
                Color.web("#7dff96"),
                Color.web("#c7a6ff"),
                Color.web("#ff9b6b"),
                Color.web("#8fb4ff"),
                Color.web("#ffb6c1")
        };
        double[] orbitSpeeds = {0.70, -0.52, 0.43, -0.26, 0.22, -0.18, 0.15, -0.12};

        for (int i = 0; i < groupConfigs.size(); i++) {
            GroupConfig config = groupConfigs.get(i);
            Planet planet = new Planet(positions[i][0], positions[i][1], planetRadii[i], planetMasses[i],
                    groupColors[i], config.planetTexturePath());
            planet.setGroupId(config.groupId());
            planet.enableOrbitAround(sun, orbitSpeeds[i]);
            addPlanet(planet, consumer);
            guardedPlanets.add(planet);
            createGroup(config, planet, groupColors[i], i, consumer);
        }

        // 為每顆非恆星行星建立多座外側衛星砲塔，讓救援路線穿過更密集的防線。
        for (int i = 0; i < guardedPlanets.size(); i++) {
            Planet planet = guardedPlanets.get(i);
            addEnemyClusterAroundPlanet(planet.getPosition().x, planet.getPosition().y,
                    turretCountForPlanet(i), rand, consumer);
        }
        addMobileEnemies(guardedPlanets, consumer);
    }

    private void addEnemyClusterAroundPlanet(double planetX, double planetY, int turretCount,
                                             java.util.Random rand, ObjectConsumer consumer) {
        double outwardAngle = Math.atan2(planetY, planetX);
        double[] fanOffsets = turretCount == 1 ? new double[]{0.0} : new double[]{-100.0, 100.0};

        for (int i = 0; i < turretCount; i++) {
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

    private int turretCountForPlanet(int planetIndex) {
        return TURRET_COUNT_PATTERN[planetIndex % TURRET_COUNT_PATTERN.length];
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
                12500.0,
                14000.0
        };
        double[] angles = {25.0, 78.0, 132.0, 186.0, 238.0, 294.0, 345.0, 45.0};
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

    private void makeStarBrightAndOpaque(Planet star, String texturePath) {
        if (!(star.getView() instanceof Sphere sphere)) return;
        Color warmOrange = Color.web("#ff8a00");
        PhongMaterial material = new PhongMaterial(warmOrange);
        material.setSpecularColor(Color.web("#fff1b8"));
        if (texturePath != null) {
            var imageUrl = LevelManager.class.getResource(texturePath);
            if (imageUrl != null) {
                Image img = new Image(imageUrl.toExternalForm());
                material.setDiffuseMap(img);
                material.setSelfIlluminationMap(img);
            } else {
                material.setSelfIlluminationMap(createSolidImage(warmOrange));
            }
        } else {
            material.setSelfIlluminationMap(createSolidImage(warmOrange));
        }
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

    private void createGroup(GroupConfig config, Planet planet, Color color, int groupIndex, ObjectConsumer consumer) {
        String logoPath = config.planetTexturePath();
        String songPath = config.musicPath();
        IdolGroup group = new IdolGroup(config.groupId(), config.groupId() + "-song", logoPath, songPath,
                config.memberPortraitPrefix(), config.memberCount());
        TextureRegistry textures = TextureRegistry.getInstance();

        for (int i = 1; i <= config.memberCount(); i++) {
            double[] orbitData = createMemberOrbit(planet, config.memberCount(), i, groupIndex);
            String portraitPath = textures.resolvePortraitPath(config.memberPortraitPrefix(), i, logoPath);
            Idol idol = new Idol(config.groupId() + "-" + i, config.groupId(), i,
                    config.groupId() + "-" + i, planet,
                    orbitData[0], orbitData[1], orbitData[2], color,
                    portraitPath, logoPath, config.musicFile(), config.memberCount());
            group.add(idol);
            planet.addIdol(idol);
            idols.add(idol);
            consumer.add(idol);
        }
        idolGroups.add(group);
    }

    private double[] createMemberOrbit(Planet planet, int memberCount, int memberIndex, int groupIndex) {
        double spacing = 360.0 / memberCount;
        double orbitRadius = planet.getRadius() + 260.0 + (memberIndex % 3) * 60.0 + (memberIndex / 4) * 35.0;
        double orbitAngle = groupIndex * 23.0 + (memberIndex - 1) * spacing;
        double direction = memberIndex % 2 == 0 ? -1.0 : 1.0;
        double orbitSpeed = direction * (5.0 + (memberIndex % 4) * 1.4);
        return new double[]{orbitRadius, orbitAngle, orbitSpeed};
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
