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
        Planet aurora = new Planet(3600, 0, 267, 500000, Color.web("#3ec7ff"));
        Planet neon = new Planet(-3000, 3000, 210, 300000, Color.web("#ff4f9a"));
        Planet lunar = new Planet(0, 5400, 160, 120000, Color.web("#d8d8e8"));
        addPlanet(sun, consumer);
        addPlanet(aurora, consumer);
        addPlanet(neon, consumer);
        addPlanet(lunar, consumer);

        createGroup("AURORA", "aurora-hook", aurora, Color.web("#54f4ff"),
                new String[]{"Mina", "Sora", "Lumi"},
                new double[][]{{380, 42, 11}, {470, 156, -8}, {560, 285, 6}},
                "/photo/a.jpg", "/music/song.mp3", consumer);
        createGroup("NEON", "neon-chorus", neon, Color.web("#ff72bb"),
                new String[]{"Rin", "Nana", "Yuki"},
                new double[][]{{330, 22, -10}, {420, 135, 7}, {510, 264, -5}},
                "/photo/b.jpg", "/music/song.mp3", consumer);
        createGroup("LUNAR", "lunar-bridge", lunar, Color.web("#d7ff7a"),
                new String[]{"Hana", "Mei"},
                new double[][]{{280, 65, 9}, {360, 236, -7}},
                "/photo/c.jpg", "/music/song.mp3", consumer);

        addEnemy(new Enemy(1200, 1100), consumer);
        addEnemy(new Enemy(-1600, 2100), consumer);
        addEnemy(new Enemy(2500, 4300), consumer);
    }

    private void addPlanet(Planet planet, ObjectConsumer consumer) {
        planets.add(planet);
        consumer.add(planet);
    }

    private void addEnemy(Enemy enemy, ObjectConsumer consumer) {
        enemies.add(enemy);
        consumer.add(enemy);
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
