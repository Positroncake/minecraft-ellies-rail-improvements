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
            double a = 0.0;
            double s = 0.0;

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

            boolean holdingStoppingSignal = holdingStoppingSignal(cart);
            if (!holdingStoppingSignal) {
                // coast on unpowered track unless decelerating
                // coasting involves slowing down slightly due to air resistance/friction and leaving the velocity otherwise unchanged
                int vv = nbt.getInt("vv");
                double spd = nbt.getDouble("spd");
                spd = Math.max(spd-NATURAL_FRICTION_DECEL, 0);
                nbt.putDouble("spd", spd);
                if (vv == 1) {
                    cart.setPos(cart.position().add(spd/4.0, 0, 0));
                } else if (vv == -1) {
                    cart.setPos(cart.position().add(-spd/4.0, 0, 0));
                } else if (vv == 2) {
                    cart.setPos(cart.position().add(0, 0, spd/4.0));
                } else if (vv == -2) {
                    cart.setPos(cart.position().add(0, 0, -spd/4.0));
                }
            } else {
                // deceleration is handled in MinecartStoppingHandler.java, where the 'spd' NBT tag has its data updated
                // here, we just need to let the minecart keep moving at the constantly-decelerating 'spd' NBT value
                CompoundTag tag = cart.getPersistentData();
                var spdMpt = tag.getDouble("spd");
                var vv = tag.getInt("vv");
                if (vv == 1) {
                    cart.setPos(cart.position().add(spdMpt/4.0, 0, 0));
                } else if (vv == -1) {
                    cart.setPos(cart.position().add(-spdMpt/4.0, 0, 0));
                } else if (vv == 2) {
                    cart.setPos(cart.position().add(0, 0, spdMpt/4.0));
                } else if (vv == -2) {
                    cart.setPos(cart.position().add(0, 0, -spdMpt/4.0));
                }
            }

            printDebug(cart, String.format("%06.3f", nbt.getDouble("spd")*20) + " [" + nbt.getInt("vv") + "] " + String.format("%06.3f", s) + " + " + String.format("%.5f", a));
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

    private boolean holdingStoppingSignal(AbstractMinecart cart) {
        ResourceLocation stopSignalId = ResourceLocation.parse("elliesrailmod:signal_stop");
        Item stopSignalItem = BuiltInRegistries.ITEM.get(stopSignalId);
        ResourceLocation eStopSignalId = ResourceLocation.parse("elliesrailmod:signal_e_stop");
        Item eStopSignalItem = BuiltInRegistries.ITEM.get(eStopSignalId);

        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(stopSignalItem) || offHand.is(stopSignalItem)) return true;
                if (mainHand.is(eStopSignalItem) || offHand.is(eStopSignalItem)) return true;
            }
        }
        return false;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float[] spdLimsMps = SpeedLimits.GetSpdLimsMps(TRACK_CLASS);

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