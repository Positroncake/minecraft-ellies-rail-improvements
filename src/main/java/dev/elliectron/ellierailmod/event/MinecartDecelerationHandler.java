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

@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartDecelerationHandler {

    private static final double DECELERATION_RATE = -0.1 / 20;
    private static final double MIN_SPEED = 0.00;

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

        // Check if the player is holding the signal_stop item
        if (isHoldingSignalStop(player)) {
            decelerateMinecart(minecart);
        }
    }

    private static boolean isHoldingSignalStop(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        return mainHand.is(ModItems.SIGNAL_STOP.get()) ||
                offHand.is(ModItems.SIGNAL_STOP.get());
    }

    private static void decelerateMinecart(AbstractMinecart minecart) {
        Vec3 motion = minecart.getDeltaMovement();

        // If the speed is already very low, stop the minecart
        if (motion.x < MIN_SPEED & motion.z < MIN_SPEED) {
            minecart.setDeltaMovement(0, motion.y, 0);
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