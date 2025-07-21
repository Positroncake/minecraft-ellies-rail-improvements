package dev.elliectron.ellierailmod.event;

import dev.elliectron.ellierailmod.ElliesRailImprovements;
import dev.elliectron.ellierailmod.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@SuppressWarnings("DuplicatedCode")
@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartSpdLimHandler {

    // Based on CROR signal aspect speed limits, converted from mph to m/s
    private static final double SPEED_LIMITED = 20.1 / 20.0;
    private static final double SPEED_MEDIUM = 13.4 / 20.0;
    private static final double SPEED_DIVERGING = 11.2 / 20.0;
    private static final double SPEED_RESTRICTED = 6.7 / 20.0;

    // Base deceleration rate: 1 block/s² = 1/20 blocks/tick²
    private static final double BASE_DECELERATION_RATE = -0.053 / 20;
    private static final double RAIN_DECELERATION_MULTIPLIER = 0.7; // 30% less deceleration in rain
    private static final double ATTACHED_PAX_DECEL_FACTOR = 0.955; // Reduced deceleration per attached minecart (player)
    private static final double ATTACHED_FREIGHT_DECEL_FACTOR = 0.930; // Reduced deceleration per attached minecart (chest/hopper/etc)

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof AbstractMinecart minecart)) return;
        if (minecart.getPassengers().isEmpty()) return;
        Player player = null;
        for (Entity passenger : minecart.getPassengers())
            if (passenger instanceof Player) {
                player = (Player) passenger;
                break;
            }
        if (player == null) return;

        double speedLimit = getSpeedLimit(player);
        if (speedLimit > 0) limitMinecartSpeed(minecart, speedLimit);
    }

    private static double getSpeedLimit(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.is(ModItems.SIGNAL_SPEED_LIMITED.get()) || offHand.is(ModItems.SIGNAL_SPEED_LIMITED.get())) return SPEED_LIMITED;
        if (mainHand.is(ModItems.SIGNAL_SPEED_MEDIUM.get()) || offHand.is(ModItems.SIGNAL_SPEED_MEDIUM.get())) return SPEED_MEDIUM;
        if (mainHand.is(ModItems.SIGNAL_SPEED_DIVERGING.get()) || offHand.is(ModItems.SIGNAL_SPEED_DIVERGING.get())) return SPEED_DIVERGING;
        if (mainHand.is(ModItems.SIGNAL_SPEED_RESTRICTED.get()) || offHand.is(ModItems.SIGNAL_SPEED_RESTRICTED.get())) return SPEED_RESTRICTED;

        return 0; // No speed limit
    }

    private static void limitMinecartSpeed(AbstractMinecart minecart, double maxSpeed) {
        Vec3 motion = minecart.getDeltaMovement();
        double currSpd = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (currSpd <= maxSpeed) return;

        double dynamicDecelerationRate = calculateDynamicDecelerationRate(minecart);
        Vec3 decelAmount = calcDecelAmount(motion.x, motion.y, motion.z, dynamicDecelerationRate);
        minecart.setDeltaMovement(motion.add(decelAmount));
    }

    private static double calculateDynamicDecelerationRate(AbstractMinecart minecart) {
        Level level = minecart.level();
        double decelRate = BASE_DECELERATION_RATE;

        if (isRainingAtPosition(level, minecart)) decelRate *= RAIN_DECELERATION_MULTIPLIER;

        int[] attachedCount = countAttachedMinecarts(minecart);
        int paxMinecartsNum = attachedCount[0];
        int freightMinecartsNum = attachedCount[1];
        decelRate *= Math.pow(ATTACHED_PAX_DECEL_FACTOR, paxMinecartsNum);
        decelRate *= Math.pow(ATTACHED_FREIGHT_DECEL_FACTOR, freightMinecartsNum);
        return decelRate;
    }

    private static boolean isRainingAtPosition(Level level, AbstractMinecart minecart) {
        // Check if it's raining in the biome and if the minecart is exposed to sky
        return level.isRainingAt(minecart.blockPosition()) || (level.isRaining() && level.canSeeSky(minecart.blockPosition()));
    }

    private static int[] countAttachedMinecarts(AbstractMinecart leadMinecart) {
        int countPax = 0;
        int countFreight = 0;
        Level level = leadMinecart.level();

        for (Entity entity : level.getEntitiesOfClass(AbstractMinecart.class,
                leadMinecart.getBoundingBox().inflate(9.0))) {

            if (entity != leadMinecart && entity instanceof AbstractMinecart otherCart) {
                double distance = leadMinecart.distanceTo(entity);
                if (distance <= 9.0) {
                    if (entity instanceof Minecart) ++countPax;
                    else ++countFreight;
                }
            }
        }

        return new int[] { countPax, countFreight };
    }

    private static Vec3 calcDecelAmount(double x, double y, double z, double decelerationRate) {
        double xAccel = 0, zAccel = 0;

        if (x < 0) xAccel = -decelerationRate;
        else if (x > 0) xAccel = decelerationRate;

        if (z < 0) zAccel = -decelerationRate;
        else if (z > 0) zAccel = decelerationRate;

        return new Vec3(xAccel, 0, zAccel);
    }
}