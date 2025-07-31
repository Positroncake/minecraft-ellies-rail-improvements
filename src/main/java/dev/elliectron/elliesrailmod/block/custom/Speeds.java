package dev.elliectron.elliesrailmod.block.custom;

public class Speeds {
    // DRY_PAX_LIMITS_MPH is based on the CROR "Rules Respecting Track Safety Part II - Track Safety Rules",
    // adapted/scaled for Minecraft due to a ~32 m/s speed limit imposed by the game (class 5 track uses custom movement logic and therefore is not affected by said limits)
    //TODO: figure out how to bypass the aforementioned speed limit
    public static final float[] DRY_PAX_LIMITS_MPH = new float[] { 12.5f, 30f, 50f, 65f, 100f };
    public static final float[] DRY_FREIGHT_LIMITS_MPH = new float[] { 8f, 20f, 35f, 50f, 65f };
    public static final float ADVANCE_SPEED_MPH = 8f;
    public static final float WET_TRACK_PAX_PENALTY = 0.70f; // amount to reduce (multiply) speed limit by when tracks are wet
    public static final float WET_TRACK_FREIGHT_PENALTY = 0.67f;

    public static final double[] SPEED_MPT_TO_SIGNAL_STRENGTH = new double[] { 0, 2, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 90, 100 };
    // _______________________________ CORRESPONDING REDSTONE SIGNAL STRENGTH: 0, 1, 2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,  15 :ARRAY INDEX

    @SuppressWarnings("SpellCheckingInspection")
    public static float[] GetSpdLimsMps(int trackClass) {
        --trackClass;
        return new float[] {
                (WET_TRACK_FREIGHT_PENALTY*DRY_FREIGHT_LIMITS_MPH[trackClass])/2.237f, // [0] = wet, freight
                (WET_TRACK_PAX_PENALTY*DRY_PAX_LIMITS_MPH[trackClass])/2.237f, // [1] = wet, pax
                DRY_FREIGHT_LIMITS_MPH[trackClass]/2.237f, // [2] = dry, freight
                DRY_PAX_LIMITS_MPH[trackClass]/2.237f, // [3] = dry, pax
        };
    }

    public static int SpeedToSignalStrength(double spdMpt) {
        if (spdMpt <= 0) return 0;
        for (var i = 0; i < 15; ++i) {
            if (spdMpt < SPEED_MPT_TO_SIGNAL_STRENGTH[i]) return i;
        }
        return 15;
    }
}
