package dev.elliectron.elliesrailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
public class PoweredClass3Rail extends RailBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final double ACCEL_BUFFER = Acceleration.MAX_ACCEL_750V/20;
    private static final int TRACK_CLASS = 3;

    public PoweredClass3Rail(BlockBehaviour.Properties properties) {
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
        if (level.hasNeighborSignal(pos)) {
            return true;
        }

        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.getBlock() instanceof Elec750VBare) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }

            if (adjacentState.getBlock() instanceof Elec750VWalkway) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }

            if (adjacentState.getBlock() instanceof VvvfvcfGeneratorBare) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }

            if (adjacentState.getBlock() instanceof VvvfvcfGeneratorWalkway) {
                int power = adjacentState.getValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER);
                if (power > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // Custom acceleration logic
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        if (entity instanceof AbstractMinecart cart) {
            CompoundTag nbt = cart.getPersistentData();
            boolean isPowered = state.getValue(POWERED);
            boolean hasStoppingSignal = holdingStoppingSignal(cart);
            boolean hasProceedSignal = holdingSignalById(cart, "elliesrailmod:signal_proceed");
            boolean hasOverrideSignal = holdingSignalById(cart, "elliesrailmod:signal_override");
            boolean hasAtoAccelFlag = nbt.getInt("signal_aspect") == 3;

            // check to see the minecart came from a class 5+ rail, which is signified by the presence of a 'spd' NBT tag
            // , as class 4- rails do not rely on the custom speed system
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

            if (!hasStoppingSignal) {
                // power available + proceed signal held
                // **note: ATO mode (flag 3) will also allow it accel**
                // => accelerate to max allowed signalled speed limit, or track speed limit if no signalled speed limit
                if ((isPowered && hasProceedSignal) || (isPowered && hasAtoAccelFlag)) {
                    Vec3 motionMpt = cart.getDeltaMovement();
                    float trackSpdLim = getRailMaxSpeed(state, level, pos, cart);
                    float signalSpdLim = getSignalledMaxSpeed(cart);
                    float proceedSpdLim = signalSpdLim == -1f ? trackSpdLim : signalSpdLim;

                    double currSpd = Math.sqrt(motionMpt.x * motionMpt.x + motionMpt.z * motionMpt.z);
                    // System.out.println("current " + currSpd + " vs limit " + proceedSpdLim);
                    if (currSpd <= proceedSpdLim) {
                        Vec3 accelAmountT = Acceleration.Calc750VAccelMpt(motionMpt, getRailShape(state));
                        cart.setDeltaMovement(motionMpt.add(accelAmountT));
                    }
                }
                // power available + override signal held
                // => accelerate to max track speed limit, ignoring all other restrictions
                else if (isPowered && hasOverrideSignal) {
                    Vec3 motionMpt = cart.getDeltaMovement();
                    float ovrdSpdLim = getRailMaxSpeed(state, level, pos, cart);

                    double currSpd = Math.sqrt(motionMpt.x * motionMpt.x + motionMpt.z * motionMpt.z);
                    // ACCEL_BUFFER is to prevent over-acceleration (e.g. if maxSpd is 10.00 and the maximum possible acceleration is 0.03,
                    // then by having ACCEL_BUFFER as 0.03, the maximum currSpd can ever be at is 9.97 and therefore not over-accelerate,
                    // as a value of 9.98 would give the minecart extra momentum equal to 10.01 (and the if statement therefore fails such
                    // a condition (9.97 <= 10.00 - 0.03, but 9.98 </= 10.00 - 0.03))
                    if (currSpd <= ovrdSpdLim - ACCEL_BUFFER) {
                        Vec3 accelAmountT = Acceleration.Calc750VAccelMpt(motionMpt, getRailShape(state));
                        cart.setDeltaMovement(motionMpt.add(accelAmountT));
                    }
                }
                // no power and no override => treat as danger (red) signal aspect and trigger Estop
                else if (!isPowered && !hasOverrideSignal) {
                    nbt.putInt("signal_aspect", -2);
                }
            }
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

    private boolean holdingSignalById(AbstractMinecart cart, String id) {
        ResourceLocation signalId = ResourceLocation.parse(id);
        Item signalItem = BuiltInRegistries.ITEM.get(signalId);

        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(signalItem) || offHand.is(signalItem)) return true;
            }
        }
        return false;
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

    private float getSignalledMaxSpeed(AbstractMinecart cart) {
        CompoundTag nbt = cart.getPersistentData();
        if (nbt.contains("signal_spdlim")) {
            // System.out.println(20f*nbt.getFloat("signal_spdlim"));
            return nbt.getFloat("signal_spdlim");
        }
        return -1f;
    }
}