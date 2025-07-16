package dev.elliectron.ellierailmod.event;

import dev.elliectron.ellierailmod.ElliesRailImprovements;
import dev.elliectron.ellierailmod.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@SuppressWarnings("DuplicatedCode")
@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartSpeedLimiter {

    // Speed limits in blocks per tick (1 block = 1 meter, 20 ticks = 1 second)
    // Formula: (m/s) / 20 = blocks per tick
    private static final double SPEED_LIMITED = 20.0 / 20.0;    // 20 m/s = 1.0 blocks/tick
    private static final double SPEED_MEDIUM = 12.0 / 20.0;     // 12 m/s = 0.6 blocks/tick
    private static final double SPEED_DIVERGING = 8.0 / 20.0;   // 8 m/s = 0.4 blocks/tick
    private static final double SPEED_RESTRICTED = 4.0 / 20.0;  // 4 m/s = 0.2 blocks/tick

    // Fixed deceleration rate: 1 block/s² = 1/20 blocks/tick²
    private static final double DECELERATION_RATE = -0.1 / 20;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        // Check if the entity is a minecart
        if (!(entity instanceof AbstractMinecart minecart)) {
            return;
        }

        // Check if there's a player passenger
        if (minecart.getPassengers().isEmpty()) {
            return;
        }

        // Find the first player passenger
        Player player = null;
        for (Entity passenger : minecart.getPassengers()) {
            if (passenger instanceof Player) {
                player = (Player) passenger;
                break;
            }
        }

        if (player == null) {
            return;
        }

        // Check for speed limiting items and apply appropriate speed limit
        double speedLimit = getSpeedLimit(player);
        if (speedLimit > 0) {
            limitMinecartSpeed(minecart, speedLimit);
        }
    }

    private static double getSpeedLimit(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Check main hand first, then offhand
        if (mainHand.is(ModItems.SIGNAL_SPEED_LIMITED.get()) ||
                offHand.is(ModItems.SIGNAL_SPEED_LIMITED.get())) {
            return SPEED_LIMITED;
        }

        if (mainHand.is(ModItems.SIGNAL_SPEED_MEDIUM.get()) ||
                offHand.is(ModItems.SIGNAL_SPEED_MEDIUM.get())) {
            return SPEED_MEDIUM;
        }

        if (mainHand.is(ModItems.SIGNAL_SPEED_DIVERGING.get()) ||
                offHand.is(ModItems.SIGNAL_SPEED_DIVERGING.get())) {
            return SPEED_DIVERGING;
        }

        if (mainHand.is(ModItems.SIGNAL_SPEED_RESTRICTED.get()) ||
                offHand.is(ModItems.SIGNAL_SPEED_RESTRICTED.get())) {
            return SPEED_RESTRICTED;
        }

        return 0; // No speed limit
    }

    private static void limitMinecartSpeed(AbstractMinecart minecart, double maxSpeed) {
        Vec3 motion = minecart.getDeltaMovement();

        // Calculate current horizontal speed
        double currentSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        // If current speed is within the limit, no action needed
        if (currentSpeed <= maxSpeed) {
            return;
        }

        Vec3 decelAmount = CalcDecelAmount(motion.x, motion.y, motion.z);
        minecart.setDeltaMovement(motion.add(decelAmount));
    }

    private static Vec3 CalcDecelAmount(double x, double y, double z) {
        double xAccel = 0, zAccel = 0;
        if (x < 0) xAccel = -DECELERATION_RATE;
        else if (x > 0) xAccel = DECELERATION_RATE;
        if (z < 0) zAccel = -DECELERATION_RATE;
        else if (z > 0) zAccel = DECELERATION_RATE;
        return new Vec3(xAccel, y, zAccel);
    }
}