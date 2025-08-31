package dev.elliectron.elliesrailmod.block.custom;

public class Speeds {
    // DRY_PAX_LIMITS_MPH is based on the CROR "Rules Respecting Track Safety Part II - Track Safety Rules",
    // adapted/scaled for Minecraft due to a ~32 m/s speed limit imposed by the game (class 5+ track uses custom movement logic and therefore is not affected by said limits)
    //TODO: figure out how to bypass the aforementioned speed limit without implementing custom physics (e.g. in class 5)
    public static final float[] DRY_PAX_LIMITS_MPH = new float[] { 12.5f, 30f, 45f, 65f, 100f, 149.129f };
    public static final float[] DRY_FREIGHT_LIMITS_MPH = new float[] { 8f, 20f, 35f, 50f, 65f, 65f }; // amount to reduce speed limit by when treight train is present
    public static final float WET_TRACK_PAX_PENALTY = 0.70f; // amount to reduce (multiply) speed limit by when tracks are wet
    public static final float WET_TRACK_FREIGHT_PENALTY = 0.67f; // amount to reduce speed limit by when tracks are wet and freight train is present
    public static final float LINEAR_INDUCTION_SPEED_LIMIT_KMH = 95f;
    public static final float WET_LINEAR_TRACK_PENALTY = 0.95f; // only reduce max speed by a minor amount when a train is on wet LIM tracks,
    // as braking is mostly unaffected by wheel-to-rail adhesion for LIM-powered trains,
    // but interference when the centre reaction rail is significantly contaminated can still cause decreases in performance

    public static final float[] CLASS5PLUS_LIMITS_MPT = new float[] { 2.2352f, 3.333335f };
    public static final float[] CLASS5PLUS_NATURAL_DECEL_MPT = new float[] { 0.0009f, 0.0007f };

    public static final double[] SIGNAL_STRENGTH_TO_SPEED_MPH = new double[] { 0, 0, 3, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 90, 100 };
    // _______________________________ CORRESPONDING REDSTONE SIGNAL STRENGTH: 0, 1, 2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,  15 :ARRAY INDEX

    @SuppressWarnings("SpellCheckingInspection")
    public static float[] GetConventionalSpdLimsMps(int trackClass) {
        --trackClass;
        return new float[] {
                (WET_TRACK_FREIGHT_PENALTY*DRY_FREIGHT_LIMITS_MPH[trackClass])/2.237f, // [0] = wet, freight
                (WET_TRACK_PAX_PENALTY*DRY_PAX_LIMITS_MPH[trackClass])/2.237f, // [1] = wet, pax
                DRY_FREIGHT_LIMITS_MPH[trackClass]/2.237f, // [2] = dry, freight
                DRY_PAX_LIMITS_MPH[trackClass]/2.237f, // [3] = dry, pax
        };
    }

    public static float[] GetLinearInductionSpdLimsMps() {
        return new float[] {
                (WET_LINEAR_TRACK_PENALTY*LINEAR_INDUCTION_SPEED_LIMIT_KMH)/3.6f, // [0] = wet (pax)
                LINEAR_INDUCTION_SPEED_LIMIT_KMH/3.6f // [1] = dry (pax)
        };
    }

    public static int SpeedMptToSignalStr(double spdMpt) {
        if (spdMpt <= 0) return 0;
        spdMpt *= 20.0;
        spdMpt *= 2.237;
        for (var i = 0; i < 15; ++i) {
            if (spdMpt < SIGNAL_STRENGTH_TO_SPEED_MPH[i]) return i;
        }
        return 15;
    }

    public static double SignalStrToSpeedMpt(int signal) {
        return (SIGNAL_STRENGTH_TO_SPEED_MPH[signal]*0.44704)/20.0;
    }
}
