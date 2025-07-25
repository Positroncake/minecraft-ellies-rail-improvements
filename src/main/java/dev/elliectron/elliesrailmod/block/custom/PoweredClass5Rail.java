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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("DuplicatedCode")
public class PoweredClass5Rail extends RailBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int TRACK_CLASS = 5;
    // Custom max speed for the minecart as the SpeedLimits.[...] is irrelevant given the custom acceleration logic
    // Note: Empirically determined scaling factors due to Minecraft's internal movement handling:
    // - Position movement: divide by 4
    // - Acceleration: divide by 10
    // - Speed display: multiply by 20 for m/s
    // The exact cause is unknown but these ratios are consistent
    private static final double MAX_SPEED_MPT = 2.2352;
    private static final double NATURAL_FRICTION_DECEL = 0.0012/20.0;

    public PoweredClass5Rail(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    // Add redstone power detection (UPDATED VERSION)
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean isPowered = isRailPowered(level, pos);

        if (state.getValue(POWERED) != isPowered) {
            level.setBlock(pos, state.setValue(POWERED, isPowered), 3);
        }
    }

    /**
     * Check if this rail should be powered, including redstone wire detection
     */
    private boolean isRailPowered(Level level, BlockPos pos) {
        // Check for direct redstone power first
        if (level.hasNeighborSignal(pos)) {
            return true;
        }

        // Check for redstone wire on adjacent blocks (the common case)
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // Check if there's powered vanilla redstone wire on an adjacent block
            if (adjacentState.is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }

            // Check if there's powered custom rail redstone wire on an adjacent block
            if (adjacentState.getBlock() instanceof Electrification25kV) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // Custom acceleration logic
    @SuppressWarnings("ConstantValue")
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof AbstractMinecart cart) {
            boolean isPowered = state.getValue(POWERED);
            boolean hasProceedSignal = holdingProceedSignal(cart);
            boolean hasOverrideSignal = holdingOverrideSignal(cart);
            CompoundTag nbt = cart.getPersistentData();
            double a = 0.0;
            double s = 0.0;

            // note: vv = velocity vector
            // set/reset direction of minecart's speed
            Vec3 rawMvmnt = cart.getDeltaMovement();
            if (rawMvmnt.x > 0.005) {
                nbt.putInt("vv", 1);
            } else if (rawMvmnt.x < -0.005) {
                nbt.putInt("vv", -1);
            } else if (rawMvmnt.z > 0.005) {
                nbt.putInt("vv", 2);
            } else if (rawMvmnt.z < -0.005) {
                nbt.putInt("vv", -2);
            }

            // set magnitude of minecart's speed to current speed in the level, if not already set
            // this most commonly occurs when going from class 4- track to class 5+ track
            if (!nbt.contains("spd")) {
                double spd = Math.sqrt(rawMvmnt.x * rawMvmnt.x + rawMvmnt.z * rawMvmnt.z);
                nbt.putDouble("spd", spd);
                cart.setDeltaMovement(0, 0, 0);
            }

            boolean holdingStoppingSignal = holdingStoppingSignal(cart);
            if (!holdingStoppingSignal) {
                // power available + proceed signal held => accelerate
                // power available + override signal held => also accelerate
                if ((isPowered && hasProceedSignal) || (isPowered && hasOverrideSignal)) {
                    int vv = nbt.getInt("vv"); // get direction of minecart's speed
                    double currSpdMpt = nbt.getDouble("spd"); // get magnitude of minecart's speed

                    // calculate amount to accelerate minecart by
                    double newSpdMpt = currSpdMpt;
                    if (vv != 0 && currSpdMpt < MAX_SPEED_MPT) {
                        double accelMpt = Acceleration.Calc25kVAccelMagnitude(currSpdMpt*20)/2.0; // I have no idea why I need to do /2.0 but if I don't, the acceleration ends up being too quick
                        newSpdMpt += accelMpt;
                        a = accelMpt*20;
                        s = currSpdMpt*20;
                        if (newSpdMpt > MAX_SPEED_MPT) newSpdMpt = MAX_SPEED_MPT;
                        nbt.putDouble("spd", newSpdMpt);
                    }

                    // move minecart
                    if (vv == 1) {
                        cart.setPos(cart.position().add(newSpdMpt/4.0, 0, 0));
                    } else if (vv == -1) {
                        cart.setPos(cart.position().add(-newSpdMpt/4.0, 0, 0));
                    } else if (vv == 2) {
                        cart.setPos(cart.position().add(0, 0, newSpdMpt/4.0));
                    } else if (vv == -2) {
                        cart.setPos(cart.position().add(0, 0, -newSpdMpt/4.0));
                    }
                }
                // power available + NO proceed signal held => coast
                // power available + NO override signal held => also coast
                else if ((isPowered && !hasProceedSignal) && (isPowered && !hasOverrideSignal)) {
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
                }
                // power NOT available + NO override signal held => treat as danger (red) signal aspect
                else if (!isPowered && !hasOverrideSignal) {
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

    private RailShape getRailShape(BlockState state) {
        if (state.hasProperty(SHAPE)) return state.getValue(SHAPE);
        return RailShape.EAST_WEST;
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

    private boolean holdingOverrideSignal(AbstractMinecart cart) {
        ResourceLocation overrideSignalId = ResourceLocation.parse("elliesrailmod:signal_override");
        Item overrideItem = BuiltInRegistries.ITEM.get(overrideSignalId);

        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(overrideItem) || offHand.is(overrideItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean holdingProceedSignal(AbstractMinecart cart) {
        ResourceLocation proceedSignalId = ResourceLocation.parse("elliesrailmod:signal_proceed");
        Item proceedItem = BuiltInRegistries.ITEM.get(proceedSignalId);

        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(proceedItem) || offHand.is(proceedItem)) {
                    return true;
                }
            }
        }
        return false;
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
}