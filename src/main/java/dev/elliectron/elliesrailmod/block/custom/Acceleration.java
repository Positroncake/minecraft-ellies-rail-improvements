package dev.elliectron.elliesrailmod.block.custom;

import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class Acceleration {
    public static final double MAX_ACCEL_600V = 0.0330;
    public static final double MAX_ACCEL_750V = 0.0370;
    public static final double MAX_ACCEL_25KV = 0.0200;

    // for tweaking acceleration/deceleration values as you see fit
    public static double AccelMultiplier = 1;

    // vvMpt = velocity vector, metres per tick
    // speedMps = speed, metres per second
    // accelMpt = amount to accelerate this tick, metres per tick
    private static final double var601 = 3.0, var602 = 7.5;
    public static Vec3 Calc600VAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < var601) {
            accelMpt = 0.010 + 0.00766667*speedMps;
        } else if (var601 <= speedMps && speedMps < var602) {
            accelMpt = MAX_ACCEL_600V;
        } else if (var602 <= speedMps) {
            accelMpt = MAX_ACCEL_600V*Math.pow(var602 /speedMps, 0.600);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    private static final double var751 = 4.0, var752 = 10.0;
    public static Vec3 Calc750VAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < var751) {
            accelMpt = 0.010 + 0.00675*speedMps;
        } else if (var751 <= speedMps && speedMps < var752) {
            accelMpt = MAX_ACCEL_750V;
        } else if (var752 <= speedMps) {
            accelMpt = MAX_ACCEL_750V*Math.pow(var752/speedMps, 0.430);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    private static final double var25k1 = 4.0, var25k2 = 15.0;
    public static double Calc25kVAccelMagnitude(double spdMps) {
        double accelMpt = 0.0;
        if (spdMps < var25k1) {
            accelMpt = 0.0070 + 0.00325*spdMps;
        } else if (var25k1 <= spdMps && spdMps < var25k2) {
            accelMpt = MAX_ACCEL_25KV;
        } else if (var25k2 <= spdMps) {
            accelMpt = MAX_ACCEL_25KV*Math.pow(var25k2/ spdMps, 0.390);
        }
        accelMpt *= AccelMultiplier;
        return accelMpt/10.0;
    }

    private static int calcSlope(double x, double z, RailShape railShape) {
        if (railShape == RailShape.ASCENDING_NORTH) {
            if (z < -0.01) return 1;
            else if (z > 0.01) return -1;
        } else if (railShape == RailShape.ASCENDING_EAST) {
            if (x > 0.01) return 1;
            else if (x < -0.01) return -1;
        } else if (railShape == RailShape.ASCENDING_SOUTH) {
            if (z > 0.01) return 1;
            else if (z < -0.01) return -1;
        }  else if (railShape == RailShape.ASCENDING_WEST) {
            if (x < -0.01) return 1;
            else if (x > 0.01) return -1;
        } return 0;
    }

    private static Vec3 calcNewVvMpt(double x, double y, double z, double accelMpt, int slope) {
        accelMpt *= AccelMultiplier;
        //TODO: on diagonal track sections, the acceleration will apply on the x-axis and not the z-axis - fix later
        double xAccelMpt = 0.0, zAccelMpt = 0.0;
        double bonusAccel = 0.0;
        if (slope == 1) bonusAccel = 0.1132;
        else if (slope == -1) bonusAccel = -0.1132;

        if (Math.abs(x) > 0.01) {
            if (x > 0) {
                xAccelMpt = accelMpt + bonusAccel;
        //        System.out.println("speed " + 20*x + " m/s, +x accel " + 20* xAccelMpt + " m/s");
            }
            else if (x < 0) {
                xAccelMpt = -1 * (accelMpt + bonusAccel);
        //        System.out.println("speed " + -20*x + " m/s, -x accel " + -20* xAccelMpt + " m/s");
            }
        }
        if (Math.abs(z) > 0.01) {
            if (z > 0) {
                zAccelMpt = accelMpt + bonusAccel;
        //        System.out.println("speed " + 20*z + " m/s, +z accel " + 20* zAccelMpt + " m/s");
            } else if (z < 0) {
                zAccelMpt = -1 * (accelMpt + bonusAccel);
        //        System.out.println("speed " + -20*z + " m/s, -z accel " + -20* zAccelMpt + " m/s");
            }
        }
        return new Vec3(xAccelMpt /20, y, zAccelMpt /20);
    }
}
