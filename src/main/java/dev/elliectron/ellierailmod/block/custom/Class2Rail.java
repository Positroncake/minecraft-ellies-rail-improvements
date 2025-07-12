package dev.elliectron.ellierailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Class2Rail extends RailBlock {

    public Class2Rail(Properties properties) {
        super(properties);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        // Default vanilla rail speed is 8.0f (m/s) / 20 (tps) = 0.4f (speed value)
        if (cart instanceof MinecartChest && level.isRaining()) {
            return 5f/20; // Slowest when freight carts is on wet tracks
        }

        if (cart instanceof MinecartChest) {
            return 6f/20; // Slow for freight carts
        }

        if (level.isRaining()) {
            return 7f/20; // Slower when normal carts are on wet tracks
        }

        return 8f/20; // Default speed for normal carts
    }
}