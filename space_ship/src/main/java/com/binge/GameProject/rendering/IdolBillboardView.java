package com.binge.GameProject.rendering;

import com.binge.GameProject.model.IdolState;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class IdolBillboardView {
    private static final double VIEW_SCALE = 0.57;
    private static final double PANEL_WIDTH = 92.0;
    private static final double PANEL_HEIGHT = 230.0;
    private static final double PANEL_DEPTH = 8.0;
    private static final double PANEL_LIFT = 0.0;
    private static final int PORTRAIT_TEXTURE_WIDTH = 192;
    private static final int PORTRAIT_TEXTURE_HEIGHT = 480;
    private static final int BADGE_TEXTURE_SIZE = 96;

    private final Group root = new Group();
    private final Group panelGroup = new Group();
    private final Group trailGroup = new Group();
    private final List<Node> pulseNodes = new ArrayList<>();
    private final List<Node> trailNodes = new ArrayList<>();
    private final Rotate faceCameraRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Color groupColor;
    private final Box backGlow;
    private final Box body;
    private final Box portraitPanel;
    private final Box badgePanel;
    private IdolState currentState = IdolState.AVAILABLE;
    private double phase;

    public IdolBillboardView(Image portrait, Image groupLogo, Color groupColor) {
        this.groupColor = groupColor;
        root.setDepthTest(DepthTest.ENABLE);
        root.setMouseTransparent(true);
        root.getTransforms().add(faceCameraRotate);

        panelGroup.setTranslateY(PANEL_LIFT);
        panelGroup.setDepthTest(DepthTest.ENABLE);

        backGlow = new Box(PANEL_WIDTH + 76, PANEL_HEIGHT + 116, 3);
        backGlow.setCullFace(CullFace.NONE);
        backGlow.setMaterial(material(groupColor.deriveColor(0, 0.72, 1.8, 1.0)));
        backGlow.setOpacity(0.24);
        backGlow.setTranslateZ(20);

        body = new Box(PANEL_WIDTH, PANEL_HEIGHT, PANEL_DEPTH);
        body.setCullFace(CullFace.NONE);
        body.setMaterial(material(Color.web("#06131c")));
        body.setOpacity(0.86);

        portraitPanel = new Box(PANEL_WIDTH * 0.78, PANEL_HEIGHT * 0.82, 4);
        portraitPanel.setCullFace(CullFace.NONE);
        portraitPanel.setTranslateY(-PANEL_HEIGHT * 0.045);
        portraitPanel.setTranslateZ(-PANEL_DEPTH * 0.72);
        portraitPanel.setMaterial(textureMaterial(createPreservedRatioTexture(
                portrait, PORTRAIT_TEXTURE_WIDTH, PORTRAIT_TEXTURE_HEIGHT, Color.web("#071722"))));

        badgePanel = new Box(PANEL_WIDTH * 0.27, PANEL_WIDTH * 0.27, 5);
        badgePanel.setCullFace(CullFace.NONE);
        badgePanel.setTranslateX(PANEL_WIDTH * 0.28);
        badgePanel.setTranslateY(PANEL_HEIGHT * 0.36);
        badgePanel.setTranslateZ(-PANEL_DEPTH);
        badgePanel.setMaterial(textureMaterial(createPreservedRatioTexture(
                groupLogo, BADGE_TEXTURE_SIZE, BADGE_TEXTURE_SIZE, Color.web("#02070c"))));

        Box badgeFrame = new Box(PANEL_WIDTH * 0.31, PANEL_WIDTH * 0.31, 3);
        badgeFrame.setCullFace(CullFace.NONE);
        badgeFrame.setTranslateX(PANEL_WIDTH * 0.28);
        badgeFrame.setTranslateY(PANEL_HEIGHT * 0.36);
        badgeFrame.setTranslateZ(-PANEL_DEPTH + 2);
        badgeFrame.setMaterial(material(Color.web("#dffcff")));
        badgeFrame.setOpacity(0.36);

        addFrame(PANEL_WIDTH + 18, PANEL_HEIGHT + 18, -PANEL_DEPTH - 2);

        Box pedestal = new Box(PANEL_WIDTH * 0.66, 10, 28);
        pedestal.setCullFace(CullFace.NONE);
        pedestal.setTranslateY(PANEL_HEIGHT * 0.52);
        pedestal.setMaterial(material(groupColor.deriveColor(0, 0.9, 1.9, 1.0)));
        pedestal.setOpacity(0.76);
        pulseNodes.add(pedestal);

        createRescuedTrail();

        panelGroup.getChildren().addAll(backGlow, body, portraitPanel, badgeFrame, badgePanel, pedestal, trailGroup);
        root.getChildren().add(panelGroup);
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

        double floatY = Math.sin(phase * 2.0) * 12.0;
        panelGroup.setTranslateY(PANEL_LIFT + floatY);

        if (state == IdolState.AVAILABLE) {
            double idlePulse = 0.5 + 0.5 * Math.sin(phase * 3.0);
            backGlow.setOpacity(0.18 + idlePulse * 0.10);
        } else if (state == IdolState.RESCUED) {
            double trailPulse = 0.5 + 0.5 * Math.sin(phase * 5.0);
            for (int i = 0; i < trailNodes.size(); i++) {
                trailNodes.get(i).setOpacity(0.16 + trailPulse * (0.16 - i * 0.025));
            }
        } else if (state == IdolState.SINGING) {
            double pulse = 0.5 + 0.5 * Math.sin(phase * 9.0);
            double scale = 0.84 + pulse * 0.08;
            panelGroup.setScaleX(scale);
            panelGroup.setScaleY(scale);
            panelGroup.setScaleZ(scale);
            backGlow.setOpacity(0.42 + pulse * 0.22);
            portraitPanel.setOpacity(0.88 + pulse * 0.12);
            for (Node node : pulseNodes) {
                node.setOpacity(0.72 + pulse * 0.28);
            }
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
        panelGroup.setScaleX(1.0);
        panelGroup.setScaleY(1.0);
        panelGroup.setScaleZ(1.0);
        panelGroup.setVisible(true);
        portraitPanel.setOpacity(1.0);

        switch (state) {
            case AVAILABLE -> {
                panelGroup.setOpacity(1.0);
                body.setOpacity(0.88);
                backGlow.setOpacity(0.24);
                trailGroup.setVisible(false);
                for (Node node : pulseNodes) node.setOpacity(0.78);
            }
            case RESCUED -> {
                panelGroup.setOpacity(0.96);
                panelGroup.setScaleX(0.82);
                panelGroup.setScaleY(0.82);
                panelGroup.setScaleZ(0.82);
                body.setOpacity(0.76);
                backGlow.setOpacity(0.32);
                trailGroup.setVisible(true);
                for (Node node : pulseNodes) node.setOpacity(0.62);
            }
            case SINGING -> {
                panelGroup.setOpacity(1.0);
                body.setOpacity(0.88);
                backGlow.setOpacity(0.54);
                trailGroup.setVisible(true);
            }
            case LOST -> {
                panelGroup.setOpacity(0.72);
                body.setOpacity(0.32);
                backGlow.setOpacity(0.58);
                portraitPanel.setOpacity(0.48);
                trailGroup.setVisible(false);
            }
        }
    }

    private void addFrame(double width, double height, double z) {
        double sideThickness = 8.0;
        double capThickness = 10.0;

        Box left = frameBox(sideThickness, height, z);
        left.setTranslateX(-width / 2);
        Box right = frameBox(sideThickness, height, z);
        right.setTranslateX(width / 2);
        Box top = frameBox(width, capThickness, z);
        top.setTranslateY(-height / 2);
        Box bottom = frameBox(width, capThickness, z);
        bottom.setTranslateY(height / 2);

        panelGroup.getChildren().addAll(left, right, top, bottom);
        pulseNodes.add(left);
        pulseNodes.add(right);
        pulseNodes.add(top);
        pulseNodes.add(bottom);
    }

    private Box frameBox(double width, double height, double z) {
        Box box = new Box(width, height, 8);
        box.setCullFace(CullFace.NONE);
        box.setTranslateZ(z);
        box.setMaterial(material(groupColor.deriveColor(0, 0.82, 2.0, 1.0)));
        box.setOpacity(0.86);
        return box;
    }

    private void createRescuedTrail() {
        trailGroup.setTranslateZ(34);
        trailGroup.setTranslateY(PANEL_HEIGHT * 0.18);
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

    private PhongMaterial material(Color color) {
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(48);
        return material;
    }

    private PhongMaterial textureMaterial(Image image) {
        PhongMaterial material = new PhongMaterial(Color.WHITE);
        material.setDiffuseMap(image);
        material.setSelfIlluminationMap(image);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(28);
        return material;
    }

    private Image createPreservedRatioTexture(Image source, int targetWidth, int targetHeight, Color background) {
        WritableImage output = new WritableImage(targetWidth, targetHeight);
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                double vignette = 0.82 + 0.18 * (1.0 - Math.abs((x / (double) targetWidth) - 0.5) * 2.0);
                output.getPixelWriter().setColor(x, y, Color.color(
                        background.getRed() * vignette,
                        background.getGreen() * vignette,
                        background.getBlue() * vignette,
                        1.0
                ));
            }
        }

        if (source == null || source.isError() || source.getWidth() <= 1 || source.getHeight() <= 1) {
            return output;
        }

        PixelReader reader = source.getPixelReader();
        if (reader == null) {
            return output;
        }

        double scale = Math.min(targetWidth / source.getWidth(), targetHeight / source.getHeight());
        int drawWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int drawHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int offsetX = (targetWidth - drawWidth) / 2;
        int offsetY = (targetHeight - drawHeight) / 2;

        for (int y = 0; y < drawHeight; y++) {
            int sourceY = Math.min((int) (y / scale), (int) source.getHeight() - 1);
            for (int x = 0; x < drawWidth; x++) {
                int sourceX = Math.min((int) (x / scale), (int) source.getWidth() - 1);
                output.getPixelWriter().setColor(offsetX + x, offsetY + y, reader.getColor(sourceX, sourceY));
            }
        }
        return output;
    }
}
