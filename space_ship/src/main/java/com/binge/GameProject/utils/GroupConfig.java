package com.binge.GameProject.utils;

import java.util.List;

public record GroupConfig(
        String groupId,
        String musicFile,
        String planetTexture,
        int memberCount,
        String memberPortraitPrefix
) {
    public String musicPath() {
        return "/pop_musics/" + musicFile;
    }

    public String planetTexturePath() {
        return "/photo/" + planetTexture;
    }

    public static List<GroupConfig> defaults() {
        return List.of(
                new GroupConfig("nmixx", "bluevalentine.MP3", "nmixx.jpg", 6, "nmixx"),
                new GroupConfig("itzy", "cake.MP3", "e.jpg", 4, "itzy"),
                new GroupConfig("ive", "iam.MP3", "ive.jpg", 2, "ive"),
                new GroupConfig("aespa", "supernova.mp3", "a.jpg", 4, "aespa"),
                new GroupConfig("twice", "thisisfor.MP3", "twice.jpg", 4, "twice"),
                new GroupConfig("lesserafim", "spaghetti.MP3", "lesserafim.jpg", 6, "lesserafim"),
                new GroupConfig("babymonster", "drip.MP3", "babymonster.jpg", 5, "babymonster"),
                new GroupConfig("blackpink", "dududu.MP3", "blackpink.jpg", 4, "blackpink")
        );
    }
}
