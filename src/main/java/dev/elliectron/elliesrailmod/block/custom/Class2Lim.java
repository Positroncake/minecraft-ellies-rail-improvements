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
public class Class2Lim extends RailBlock {
    private static final int LIM_TRACK_CLASS = 2;

    public Class2Lim(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float spdLimMps = Speeds.GetLinearSpdLimsMps(LIM_TRACK_CLASS);
        return spdLimMps / 20f;
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