package dev.elliectron.ellierailmod.block.custom;

public class SpeedLimits {
    public static final float[] DRY_PAX_LIMITS_MPH = new float[] { 45f, 25f, 45f, 60f, 90f }; // based on the CROR "Rules Respecting Track Safety Part II - Track Safety Rules"
    public static final float[] DRY_FREIGHT_LIMITS_MPH = new float[] { 8f, 20f, 30f, 40f, 60f };
    public static final float WET_TRACK_PAX_PENALTY = 0.70f; // amount to reduce (multiply) speed limit by when tracks are wet
    public static final float WET_TRACK_FREIGHT_PENALTY = 0.67f;

    public static float[] GetSpdLimsMps(int trackClass) {
        --trackClass;
        return new float[] {
                (WET_TRACK_FREIGHT_PENALTY*DRY_FREIGHT_LIMITS_MPH[trackClass])/2.237f, // [0] = wet, freight
                (WET_TRACK_PAX_PENALTY*DRY_PAX_LIMITS_MPH[trackClass])/2.237f, // [1] = wet, pax
                DRY_FREIGHT_LIMITS_MPH[trackClass]/2.237f, // [2] = dry, freight
                DRY_PAX_LIMITS_MPH[trackClass]/2.237f, // [3] = dry, pax
        };
    }
}
