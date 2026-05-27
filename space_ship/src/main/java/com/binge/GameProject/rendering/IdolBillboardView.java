package com.binge.GameProject.rendering;

import com.binge.GameProject.model.IdolState;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class IdolBillboardView {
    private static final double VIEW_SCALE = 1.14;
    private static final double FIGURE_MAX_WIDTH = 132.0;
    private static final double FIGURE_MAX_HEIGHT = 250.0;
    private static final double BADGE_SIZE = 34.0;

    private final Group root = new Group();
    private final Group figureGroup = new Group();
    private final Group logoBadge = new Group();
    private final Group trailGroup = new Group();
    private final List<Node> trailNodes = new ArrayList<>();
    private final Rotate faceCameraRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Color groupColor;
    private final ImageView glowSilhouette;
    private final ImageView portraitView;
    private final ImageView badgeView;
    private final Circle badgeHalo;
    private final DropShadow portraitGlow;
    private final double figureWidth;
    private final double figureHeight;
    private IdolState currentState = IdolState.AVAILABLE;
    private double phase;

    public IdolBillboardView(Image portrait, Image groupLogo, Color groupColor) {
        this.groupColor = groupColor;
        double[] size = fitSize(portrait, FIGURE_MAX_WIDTH, FIGURE_MAX_HEIGHT);
        this.figureWidth = size[0];
        this.figureHeight = size[1];

        root.setDepthTest(DepthTest.ENABLE);
        root.setMouseTransparent(true);
        root.getTransforms().add(faceCameraRotate);

        figureGroup.setDepthTest(DepthTest.ENABLE);

        glowSilhouette = createFigureImage(portrait, figureWidth, figureHeight);
        glowSilhouette.setOpacity(0.22);
        glowSilhouette.setEffect(new GaussianBlur(16));

        portraitGlow = new DropShadow(
                BlurType.GAUSSIAN,
                groupColor.deriveColor(0, 0.95, 1.65, 0.95),
                22,
                0.45,
                0,
                0
        );
        portraitView = createFigureImage(portrait, figureWidth, figureHeight);
        portraitView.setEffect(portraitGlow);

        badgeHalo = new Circle(BADGE_SIZE * 0.56);
        badgeHalo.setFill(groupColor.deriveColor(0, 0.78, 1.8, 0.58));
        badgeHalo.setStroke(Color.web("#dffcff", 0.72));
        badgeHalo.setStrokeWidth(2.0);

        badgeView = createBadgeImage(groupLogo);
        badgeView.setTranslateX(-BADGE_SIZE / 2.0);
        badgeView.setTranslateY(-BADGE_SIZE / 2.0);
        badgeView.setClip(new Circle(BADGE_SIZE / 2.0, BADGE_SIZE / 2.0, BADGE_SIZE / 2.0));
        logoBadge.getChildren().addAll(badgeHalo, badgeView);
        logoBadge.setTranslateX(figureWidth * 0.30);
        logoBadge.setTranslateY(figureHeight * 0.34);

        createRescuedTrail();

        figureGroup.getChildren().addAll(glowSilhouette, portraitView, logoBadge, trailGroup);
        root.getChildren().add(figureGroup);
        applyState(IdolState.AVAILABLE);
    }

    public Group getRoot() {
        return root;
    }

    public void update(IdolState state, double dt) {
        phase += dt;
        if (state != currentState) {
            applyState(state);
        }

        double floatY = Math.sin(phase * 2.0) * 10.0;
        figureGroup.setTranslateY(floatY);

        if (state == IdolState.AVAILABLE) {
            double pulse = 0.5 + 0.5 * Math.sin(phase * 3.0);
            glowSilhouette.setOpacity(0.16 + pulse * 0.12);
            badgeHalo.setOpacity(0.46 + pulse * 0.20);
            portraitGlow.setRadius(18 + pulse * 10);
        } else if (state == IdolState.RESCUED) {
            double trailPulse = 0.5 + 0.5 * Math.sin(phase * 5.0);
            for (int i = 0; i < trailNodes.size(); i++) {
                trailNodes.get(i).setOpacity(0.16 + trailPulse * (0.18 - i * 0.028));
            }
        } else if (state == IdolState.SINGING) {
            double pulse = 0.5 + 0.5 * Math.sin(phase * 9.0);
            double scale = 0.84 + pulse * 0.08;
            figureGroup.setScaleX(scale);
            figureGroup.setScaleY(scale);
            figureGroup.setScaleZ(scale);
            glowSilhouette.setOpacity(0.38 + pulse * 0.24);
            badgeHalo.setOpacity(0.76 + pulse * 0.20);
            portraitView.setOpacity(0.90 + pulse * 0.10);
            portraitGlow.setRadius(28 + pulse * 18);
            portraitGlow.setSpread(0.42 + pulse * 0.18);
        } else if (state == IdolState.LOST) {
            root.setOpacity(Math.max(0, root.getOpacity() - dt * 2.6));
            double spread = 1.0 + dt * 1.25;
            root.setScaleX(root.getScaleX() * spread);
            root.setScaleY(root.getScaleY() * spread);
            root.setScaleZ(root.getScaleZ() * spread);
        }
    }

    public void faceCamera(double playerRotationAngle) {
        faceCameraRotate.setAngle(playerRotationAngle);
    }

    private void applyState(IdolState state) {
        currentState = state;
        root.setVisible(true);
        root.setOpacity(1.0);
        root.setScaleX(VIEW_SCALE);
        root.setScaleY(VIEW_SCALE);
        root.setScaleZ(VIEW_SCALE);
        figureGroup.setVisible(true);
        figureGroup.setOpacity(1.0);
        figureGroup.setScaleX(1.0);
        figureGroup.setScaleY(1.0);
        figureGroup.setScaleZ(1.0);
        portraitView.setOpacity(1.0);
        glowSilhouette.setOpacity(0.22);
        badgeHalo.setOpacity(0.58);
        portraitGlow.setRadius(22);
        portraitGlow.setSpread(0.45);

        switch (state) {
            case AVAILABLE -> {
                trailGroup.setVisible(false);
                logoBadge.setOpacity(0.94);
            }
            case RESCUED -> {
                figureGroup.setOpacity(0.96);
                figureGroup.setScaleX(0.82);
                figureGroup.setScaleY(0.82);
                figureGroup.setScaleZ(0.82);
                glowSilhouette.setOpacity(0.28);
                logoBadge.setOpacity(0.82);
                trailGroup.setVisible(true);
            }
            case SINGING -> {
                figureGroup.setOpacity(1.0);
                logoBadge.setOpacity(1.0);
                glowSilhouette.setOpacity(0.46);
                trailGroup.setVisible(true);
            }
            case LOST -> {
                figureGroup.setOpacity(0.68);
                portraitView.setOpacity(0.48);
                glowSilhouette.setOpacity(0.54);
                logoBadge.setOpacity(0.42);
                trailGroup.setVisible(false);
            }
        }
    }

    private void createRescuedTrail() {
        trailGroup.setTranslateZ(34);
        trailGroup.setTranslateY(figureHeight * 0.22);
        for (int i = 0; i < 4; i++) {
            Sphere spark = new Sphere(8 - i * 1.2);
            spark.setTranslateX((i % 2 == 0 ? -1 : 1) * (18 + i * 8));
            spark.setTranslateY(i * 24);
            spark.setTranslateZ(i * 22);
            spark.setMaterial(material(groupColor.deriveColor(0, 0.8, 2.0, 1.0)));
            spark.setOpacity(0.18);
            trailNodes.add(spark);
            trailGroup.getChildren().add(spark);
        }
        trailGroup.setVisible(false);
    }

    private ImageView createFigureImage(Image image, double width, double height) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setTranslateX(-width / 2.0);
        view.setTranslateY(-height / 2.0);
        view.setDepthTest(DepthTest.ENABLE);
        return view;
    }

    private PhongMaterial material(Color color) {
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(40);
        return material;
    }

    private ImageView createBadgeImage(Image image) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);
        view.setFitWidth(BADGE_SIZE);
        view.setFitHeight(BADGE_SIZE);
        view.setDepthTest(DepthTest.ENABLE);
        return view;
    }

    private double[] fitSize(Image image, double maxWidth, double maxHeight) {
        double sourceWidth = image == null || image.getWidth() <= 1 ? 96.0 : image.getWidth();
        double sourceHeight = image == null || image.getHeight() <= 1 ? 192.0 : image.getHeight();
        double scale = Math.min(maxWidth / sourceWidth, maxHeight / sourceHeight);
        return new double[]{
                Math.max(36.0, sourceWidth * scale),
                Math.max(86.0, sourceHeight * scale)
        };
    }
}
