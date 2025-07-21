package dev.elliectron.ellierailmod.block.custom;

import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class Acceleration {
    public static final double MAX_ACCEL_600V = 0.030;
    public static final double MAX_ACCEL_750V = 0.037;
    public static final double MAX_ACCEL_25KV = 0.015;
    public static final double MAX_ACCEL_25KV_FAST = 0.025;
    public static final double MAX_ACCEL_750V_UNDERWATER = 0.010;

    // for tweaking acceleration/deceleration values as you see fit
    public static double AccelMultiplier = 1;

    // vvMpt = velocity vector, metres per tick
    // speedMps = speed, metres per second
    // accelMpt = amount to accelerate this tick, metres per tick
    public static Vec3 Calc600VAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < 1.5) {
            accelMpt = 0.012 + 0.012*speedMps;
        } else if (1.5 <= speedMps && speedMps < 5) {
            accelMpt = 0.030;
        } else if (5 <= speedMps) {
            accelMpt = 0.030*Math.pow(5.0/speedMps, 1.414);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    public static Vec3 Calc750VAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < 1.5) {
            accelMpt = 0.013 + 0.016*speedMps;
        } else if (1.5 <= speedMps && speedMps < 6) {
            accelMpt = 0.037;
        } else if (6 <= speedMps) {
            accelMpt = 0.037*Math.pow(6.0/speedMps, 1.270);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    public static Vec3 Calc25kVAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < 2.0) {
            accelMpt = 0.007 + 0.004*speedMps;
        } else if (2.0 <= speedMps && speedMps < 10) {
            accelMpt = 0.015;
        } else if (10 <= speedMps) {
            accelMpt = 0.015*Math.pow(10.0/speedMps, 0.823);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    public static Vec3 Calc25kVFastAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < 2.0) {
            accelMpt = 0.007 + 0.009*speedMps;
        } else if (2.0 <= speedMps && speedMps < 8) {
            accelMpt = 0.025;
        } else if (8 <= speedMps && speedMps < 20) {
            accelMpt = 0.025*Math.pow(8.0/speedMps, 1.179);
        } else if (20 <= speedMps) {
            accelMpt = 0.008479*(Math.pow(20.0/speedMps, 0.821));
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
    }

    public static Vec3 Calc750VUnderwaterAccelMpt(Vec3 vvMpt, RailShape railShape) {
        double speedMps = 20*Math.sqrt(vvMpt.x * vvMpt.x + vvMpt.z * vvMpt.z);
        double accelMpt = 0.0;
        if (speedMps < 2.0) {
            accelMpt = 0.0030 + 0.0035*speedMps;
        } else if (2.0 <= speedMps && speedMps < 5) {
            accelMpt = 0.010;
        } else if (5 <= speedMps) {
            accelMpt = 0.010*Math.pow(5.0/speedMps, 17.553);
        }
        int slope = calcSlope(vvMpt.x, vvMpt.z, railShape);
        return calcNewVvMpt(vvMpt.x, vvMpt.y, vvMpt.z, accelMpt, slope);
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
        else if (slope == -1) bonusAccel = -0.3210;

        if (Math.abs(x) > 0.01) {
            if (x > 0) {
                xAccelMpt = accelMpt + bonusAccel;
                System.out.println("speed " + 20*x + " m/s, +x accel " + 20* xAccelMpt + " m/s");
            }
            else if (x < 0) {
                xAccelMpt = -1 * (accelMpt + bonusAccel);
                System.out.println("speed " + -20*x + " m/s, -x accel " + -20* xAccelMpt + " m/s");
            }
        }
        if (Math.abs(z) > 0.01) {
            if (z > 0) {
                zAccelMpt = accelMpt + bonusAccel;
                System.out.println("speed " + 20*z + " m/s, +z accel " + 20* zAccelMpt + " m/s");
            } else if (z < 0) {
                zAccelMpt = -1 * (accelMpt + bonusAccel);
                System.out.println("speed " + -20*z + " m/s, -z accel " + -20* zAccelMpt + " m/s");
            }
        }
        return new Vec3(xAccelMpt /20, y, zAccelMpt /20);
    }
}
