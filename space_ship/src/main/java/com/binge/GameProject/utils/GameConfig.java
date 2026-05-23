package com.binge.GameProject.utils;

public final class GameConfig {
    private GameConfig() {}

    public static final double WORLD_BOUNDARY_LIMIT = 15300.0;
    public static final double PLAYER_START_X = 0.0;
    public static final double PLAYER_START_Y = -13200.0;
    public static final double PLAYER_INVINCIBLE_SECONDS = 1.25;
    public static final double PLAYER_BASE_THRUST = 600.0;
    public static final double PLAYER_BOOST_MULTIPLIER = 1.66;
    public static final double PLAYER_MIN_FLIGHT_SPEED = PLAYER_BASE_THRUST;
    public static final double PLAYER_MAX_FLIGHT_SPEED = PLAYER_MIN_FLIGHT_SPEED + PLAYER_BASE_THRUST * PLAYER_BOOST_MULTIPLIER;

    public static final double TURRET_SHOOT_COOLDOWN = 1.85;
    public static final double MOVING_ENEMY_SHOOT_COOLDOWN = 2.25 / 1.5;
    public static final double ENEMY_BULLET_SPEED = 950.0;
    public static final double ENEMY_BULLET_RADIUS = 6.0;

    public static final int MOVING_ENEMY_COUNT = 8;
    public static final double MOVING_ENEMY_MOVE_SPEED = 285.0;
    public static final double MOVING_ENEMY_PATROL_RADIUS = 720.0;
    public static final double MOVING_ENEMY_PREFERRED_DISTANCE = 900.0;
    public static final double MOVING_ENEMY_CHASE_RADIUS = 9900.0;
}
