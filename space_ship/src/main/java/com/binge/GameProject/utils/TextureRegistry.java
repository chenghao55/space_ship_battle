package com.binge.GameProject.utils;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TextureRegistry {
    private static final TextureRegistry INSTANCE = new TextureRegistry();
    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg"};

    private final Map<String, Image> cache = new HashMap<>();
    private final Image placeholder;

    private TextureRegistry() {
        placeholder = createPlaceholder();
    }

    public static TextureRegistry getInstance() {
        return INSTANCE;
    }

    public Image loadOrPlaceholder(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return placeholder;
        }
        return cache.computeIfAbsent(resourcePath, path -> {
            URL url = TextureRegistry.class.getResource(path);
            if (url == null) return placeholder;
            try {
                Image image = new Image(url.toExternalForm(), false);
                return image.isError() ? placeholder : image;
            } catch (RuntimeException ex) {
                return placeholder;
            }
        });
    }

    public String resolvePortraitPath(String memberPortraitPrefix, int memberIndex, String groupLogoPath) {
        for (String extension : IMAGE_EXTENSIONS) {
            String candidate = "/IdolPic/" + memberPortraitPrefix + memberIndex + "." + extension;
            if (TextureRegistry.class.getResource(candidate) != null) {
                return candidate;
            }
        }
        if (groupLogoPath != null && TextureRegistry.class.getResource(groupLogoPath) != null) {
            return groupLogoPath;
        }
        return null;
    }

    public Image resolvePortrait(String memberPortraitPrefix, int memberIndex, String groupLogoPath) {
        return loadOrPlaceholder(resolvePortraitPath(memberPortraitPrefix, memberIndex, groupLogoPath));
    }

    private Image createPlaceholder() {
        WritableImage image = new WritableImage(96, 192);
        for (int x = 0; x < 96; x++) {
            for (int y = 0; y < 192; y++) {
                double stripe = ((x / 12) + (y / 12)) % 2 == 0 ? 0.18 : 0.26;
                image.getPixelWriter().setColor(x, y, Color.color(0.05 + stripe, 0.10 + stripe, 0.16 + stripe, 1.0));
            }
        }
        return image;
    }
}
