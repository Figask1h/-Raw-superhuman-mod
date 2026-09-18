package dev.ashu.rsm;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * All tunable numbers. Speeds are in blocks per second, times in seconds, damage in half-hearts.
 */
public final class RsmConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // --- Flight ---
    public static final ModConfigSpec.DoubleValue MAX_SPEED;
    public static final ModConfigSpec.DoubleValue ACCELERATION_TIME;
    public static final ModConfigSpec.DoubleValue DECELERATION_TIME;
    public static final ModConfigSpec.DoubleValue TURN_RATE;
    public static final ModConfigSpec.DoubleValue TURN_LOSS_MIN_ANGLE;
    public static final ModConfigSpec.DoubleValue TURN_SPEED_LOSS;

    // --- Rebound ---
    public static final ModConfigSpec.DoubleValue REBOUND_THRESHOLD;
    public static final ModConfigSpec.DoubleValue REBOUND_DISTANCE;

    // --- Dash ---
    public static final ModConfigSpec.DoubleValue DASH_DISTANCE;
    public static final ModConfigSpec.DoubleValue DASH_COOLDOWN;
    public static final ModConfigSpec.DoubleValue DASH_FLIGHT_BOOST;

    // --- Defense ---
    public static final ModConfigSpec.DoubleValue PHYSICAL_DAMAGE_MULTIPLIER;

    // --- Attacks ---
    public static final ModConfigSpec.DoubleValue SLASH_DAMAGE;
    public static final ModConfigSpec.DoubleValue SLASH_COOLDOWN;
    public static final ModConfigSpec.DoubleValue SLASH_RANGE;
    public static final ModConfigSpec.DoubleValue SLASH_ARC_DEGREES;
    public static final ModConfigSpec.DoubleValue STRIKE_DAMAGE;
    public static final ModConfigSpec.DoubleValue STRIKE_COOLDOWN;
    public static final ModConfigSpec.DoubleValue STRIKE_RANGE;
    public static final ModConfigSpec.DoubleValue STRIKE_KNOCKBACK;

    // --- Space (Ad Astra) ---
    public static final ModConfigSpec.BooleanValue SPACE_ENABLED;

    // --- Client ---
    public static final ModConfigSpec.DoubleValue FOV_BOOST_DEGREES;
    public static final ModConfigSpec.BooleanValue SHOW_HUD;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("flight");
        MAX_SPEED = b.comment("Top speed reached while boosting, blocks/second.")
            .defineInRange("maxSpeed", 150.0, 1.0, 1000.0);
        ACCELERATION_TIME = b.comment("Seconds of continuous boost needed to go from hover to maxSpeed (linear).")
            .defineInRange("accelerationTime", 7.0, 0.1, 60.0);
        DECELERATION_TIME = b.comment("Seconds to slow from any speed back to hover after boost/cruise ends.")
            .defineInRange("decelerationTime", 0.5, 0.0, 10.0);
        TURN_RATE = b.comment("How fast the flight direction follows the camera in fast flight, degrees per tick.")
            .defineInRange("turnRateDegreesPerTick", 20.0, 1.0, 180.0);
        TURN_LOSS_MIN_ANGLE = b.comment("Turns sharper than this angle (degrees between flight direction and camera) start costing speed; gentler steering is free.")
            .defineInRange("turnLossMinAngle", 70.0, 0.0, 180.0);
        TURN_SPEED_LOSS = b.comment("Fraction of speed lost per degree turned while above turnLossMinAngle (0.003 = a full 180 degree reversal costs about 40%).")
            .defineInRange("turnSpeedLossPerDegree", 0.003, 0.0, 0.1);
        b.pop();

        b.push("rebound");
        REBOUND_THRESHOLD = b.comment("Speed (blocks/second) from which hitting a block throws the bearer back instead of just stopping.")
            .defineInRange("threshold", 50.0, 0.0, 1000.0);
        REBOUND_DISTANCE = b.comment("How far back (blocks) the bearer is thrown on rebound.")
            .defineInRange("distance", 4.0, 0.0, 64.0);
        b.pop();

        b.push("dash");
        DASH_DISTANCE = b.comment("Blocks covered by a dash on foot or in hover.")
            .defineInRange("distance", 8.0, 0.5, 64.0);
        DASH_COOLDOWN = b.comment("Seconds.").defineInRange("cooldown", 1.5, 0.0, 600.0);
        DASH_FLIGHT_BOOST = b.comment("In fast flight a dash instead adds this many blocks/second (capped at maxSpeed) and snaps the direction to the camera.")
            .defineInRange("flightSpeedBoost", 40.0, 0.0, 1000.0);
        b.pop();

        b.push("defense");
        PHYSICAL_DAMAGE_MULTIPLIER = b.comment("Fraction of physical damage the bearer actually takes (0.07 = 93% absorbed). Non-physical damage is always nullified.")
            .defineInRange("physicalDamageMultiplier", 0.07, 0.0, 1.0);
        b.pop();

        b.push("slash");
        SLASH_DAMAGE = b.defineInRange("damage", 175.0, 0.0, 10000.0);
        SLASH_COOLDOWN = b.comment("Seconds.").defineInRange("cooldown", 0.5, 0.0, 600.0);
        SLASH_RANGE = b.comment("Reach in blocks (sword-like).").defineInRange("range", 3.0, 0.5, 32.0);
        SLASH_ARC_DEGREES = b.comment("Total width of the arc in front of the bearer that is hit.")
            .defineInRange("arcDegrees", 120.0, 1.0, 360.0);
        b.pop();

        b.push("strike");
        STRIKE_DAMAGE = b.defineInRange("damage", 500.0, 0.0, 10000.0);
        STRIKE_COOLDOWN = b.comment("Seconds. Missing still triggers the cooldown.").defineInRange("cooldown", 5.0, 0.0, 600.0);
        STRIKE_RANGE = b.comment("Reach in blocks along the crosshair ray.").defineInRange("range", 4.0, 0.5, 32.0);
        STRIKE_KNOCKBACK = b.comment("Knockback strength applied to the target (vanilla melee is about 0.4).")
            .defineInRange("knockback", 2.5, 0.0, 20.0);
        b.pop();

        b.push("space");
        SPACE_ENABLED = b.comment("Open the Ad Astra planet selection when flying above the atmosphere. Requires Ad Astra.")
            .define("enabled", true);
        b.pop();

        SERVER_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("client");
        FOV_BOOST_DEGREES = c.comment("Extra field of view (degrees) at maxSpeed; scales linearly with speed.")
            .defineInRange("fovBoostDegrees", 30.0, 0.0, 90.0);
        SHOW_HUD = c.comment("Show speed / cruise / attack cooldown overlay.").define("showHud", true);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private RsmConfig() {}
}
