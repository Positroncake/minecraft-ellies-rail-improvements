package dev.elliectron.elliesrailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("DuplicatedCode")
public class Class5Rail extends RailBlock {
    private static final int TRACK_CLASS = 5;
    private static final double NATURAL_FRICTION_DECEL = 0.0012/20.0;

    public Class5Rail(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Custom movement logic
    @SuppressWarnings("ConstantValue")
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractMinecart cart) {
            CompoundTag nbt = cart.getPersistentData();
            double debugAccel = 0.0;
            double debugSpd = 0.0;

            // note: vv = velocity vector
            // set/reset direction of minecart's speed
            Vec3 rawMvmnt = cart.getDeltaMovement();
        //    printDebug(cart, "x: " + rawMvmnt.x);
        //    printDebug(cart, "z: " + rawMvmnt.z);
            if (rawMvmnt.x > 0.005) {
                nbt.putInt("vv", 1);
            } else if (rawMvmnt.x < -0.005) {
                nbt.putInt("vv", -1);
            } else if (rawMvmnt.z > 0.005) {
        //        printDebug(cart, "vv 2 placed");
                nbt.putInt("vv", 2);
            } else if (rawMvmnt.z < -0.005) {
        //        printDebug(cart, "vv M2 placed");
                nbt.putInt("vv", -2);
            }

            // set magnitude of minecart's speed to current speed in the level, if not already set
            // this most commonly occurs when going from class 4- track to class 5+ track
            if (!nbt.contains("spd")) {
                double spd = Math.sqrt(rawMvmnt.x * rawMvmnt.x + rawMvmnt.z * rawMvmnt.z);
                nbt.putDouble("spd", spd);
                cart.setDeltaMovement(0, 0, 0);
            }
       //     printDebug(cart, "SPD NBT DATA " + nbt.getDouble("spd"));

            CompoundTag tag = cart.getPersistentData();
            var vv = tag.getInt("vv");
            var spdMpt = tag.getDouble("spd");
            spdMpt = Math.max(spdMpt-NATURAL_FRICTION_DECEL, 0);
            nbt.putDouble("spd", spdMpt);
            if (vv == 1) {
                cart.setPos(cart.position().add(spdMpt/4.0, 0, 0));
            } else if (vv == -1) {
                cart.setPos(cart.position().add(-spdMpt/4.0, 0, 0));
            } else if (vv == 2) {
                cart.setPos(cart.position().add(0, 0, spdMpt/4.0));
            } else if (vv == -2) {
                cart.setPos(cart.position().add(0, 0, -spdMpt/4.0));
            }

            printDebug(cart, String.format("%06.3f", nbt.getDouble("spd")*20) + " [" + nbt.getInt("vv") + "] " + String.format("%06.3f", debugSpd) + " + " + String.format("%.5f", debugAccel));
        } else {
            super.entityInside(state, level, pos, entity);
        }
    }

    private void printDebug(AbstractMinecart cart, String output) {
        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendSystemMessage(Component.literal(output));
            }
        }
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float[] spdLimsMps = Speeds.GetSpdLimsMps(TRACK_CLASS);

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
}