package dev.elliectron.ellierailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("DuplicatedCode")
public class Class1Rail extends RailBlock {
    public static final float SPEED_WET_FREIGHT = 2f;
    public static final float SPEED_DRY_FREIGHT = 3f;
    public static final float SPEED_WET_NORMAL = 3f;
    public static final float SPEED_DRY_NORMAL = 4f;

    public Class1Rail(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        // Default vanilla rail speed is 8.0f (m/s) / 20 (tps) = 0.4f (speed value)
        if (cart instanceof MinecartChest && level.isRaining()) {
            return SPEED_WET_FREIGHT/20; // Slowest when freight carts is on wet tracks
        }

        if (cart instanceof MinecartChest) {
            return SPEED_DRY_FREIGHT/20; // Slow for freight carts
        }

        if (level.isRaining()) {
            return SPEED_WET_NORMAL/20; // Slower when normal carts are on wet tracks
        }

        return SPEED_DRY_NORMAL/20; // Default speed for normal carts
    }
}