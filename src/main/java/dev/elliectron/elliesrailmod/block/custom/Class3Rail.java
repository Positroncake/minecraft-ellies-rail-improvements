package dev.elliectron.elliesrailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("DuplicatedCode")
public class Class3Rail extends RailBlock {
    private static final int TRACK_CLASS = 3;

    public Class3Rail(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float[] spdLimsMps = Speeds.GetConventionalSpdLimsMps(TRACK_CLASS);

        if (level.isRaining()) {
            if (cart instanceof MinecartChest || cart instanceof MinecartFurnace || cart instanceof MinecartHopper
                    || cart instanceof MinecartTNT || cart instanceof MinecartCommandBlock || cart instanceof MinecartSpawner) {
                return spdLimsMps[0] / 20f; // Slowest when freight carts are on wet tracks
            }
            return spdLimsMps[1] / 20f; // Reduced speed for freight carts on dry tracks
        }
        if (cart instanceof MinecartChest || cart instanceof MinecartFurnace || cart instanceof MinecartHopper
                || cart instanceof MinecartTNT || cart instanceof MinecartCommandBlock || cart instanceof MinecartSpawner) {
            return spdLimsMps[2] / 20f; // Slower speed when passenger carts are on wet tracks
        }
        return spdLimsMps[3] / 20f; // Full speed for passenger carts on dry tracks
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractMinecart cart) {
            // check to see the minecart came from a class 5+ rail, which is signified by the presence of a 'spd' NBT tag
            // , as class 4- rails do not rely on the custom speed system
            CompoundTag nbt = cart.getPersistentData();
            if (nbt.contains("spd")) {
                double spd = nbt.getDouble("spd");
                int vv = nbt.getInt("vv");
                if (vv == 1) {
                    cart.setDeltaMovement(spd, 0, 0);
                } else if (vv == -1) {
                    cart.setDeltaMovement(-spd, 0, 0);
                } else if (vv == 2) {
                    cart.setDeltaMovement(0, 0, spd);
                } else if (vv == -2) {
                    cart.setDeltaMovement(0, 0, -spd);
                } else cart.setDeltaMovement(0, 0, 0);
                nbt.remove("spd");
                nbt.remove("vv");
            }
        }
    }
}