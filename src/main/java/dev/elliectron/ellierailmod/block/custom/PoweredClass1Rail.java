package dev.elliectron.ellierailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("DuplicatedCode")
public class PoweredClass1Rail extends PoweredRailBlock {

    public PoweredClass1Rail(BlockBehaviour.Properties properties) {
        super(properties, true);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        // Default vanilla rail speed is 8.0f (m/s) / 20 (tps) = 0.4f (speed value)
        if (state.getValue(POWERED)) {
            if (holdingOverrideSignal(cart)) {
                return 0f; // Stop when powered but holding override signal
            }
            return class1maxSpd(level, cart);
        } else {
            if (holdingOverrideSignal(cart)) {
                return class1maxSpd(level, cart); // Proceed when UNpowered but holding override signal
            }
            return 0f;
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractMinecart cart) {
            boolean isPowered = state.getValue(POWERED);
            boolean hasOverrideSignal = holdingOverrideSignal(cart);

            // Your custom logic for when to accelerate/decelerate
            if (isPowered && !hasOverrideSignal) {
                // Normal powered behavior - accelerate
                super.entityInside(state, level, pos, entity);
            } else if (!isPowered && hasOverrideSignal) {
                // Override signal on unpowered rail - accelerate
                super.entityInside(state, level, pos, entity);
            } else if (isPowered && hasOverrideSignal) {
                // Powered + override signal = stop/brake
                cart.setDeltaMovement(cart.getDeltaMovement().multiply(0.5, 1.0, 0.5)); // Brake
            } else {
                // Unpowered without override = stop/brake
                cart.setDeltaMovement(cart.getDeltaMovement().multiply(0.5, 1.0, 0.5)); // Brake
            }
        } else {
            // For non-minecart entities, use normal behavior
            super.entityInside(state, level, pos, entity);
        }
    }

    private float class1maxSpd(Level level, AbstractMinecart cart) {
        if (cart instanceof MinecartChest && level.isRaining()) {
            return 2f/20; // Slowest when freight carts is on wet tracks
        }

        if (cart instanceof MinecartChest) {
            return 3f/20; // Slow for freight carts
        }

        if (level.isRaining()) {
            return 3f/20; // Slower when normal carts are on wet tracks
        }

        return 4f/20; // Default speed for normal carts
    }

    private boolean holdingOverrideSignal(AbstractMinecart cart) {
        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                if (isHoldingOverrideSignal(player)) return true;
            }
        }
        return false;
    }

    private boolean isHoldingOverrideSignal(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ResourceLocation overrideSignalId = ResourceLocation.parse("elliesrailimprovements:signal_switch_override");
        Item overrideItem = BuiltInRegistries.ITEM.get(overrideSignalId);
        return mainHand.is(overrideItem) || offHand.is(overrideItem);
    }
}