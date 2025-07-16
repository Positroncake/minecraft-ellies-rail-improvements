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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("DuplicatedCode")
public class PoweredClass1Rail extends RailBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final double ACCEL_AMOUNT = 0.03/20;
    public static final double DECEL_AMOUNT = -0.005/20;
    public static final double MAX_SPD_THRES_ROUND = 2 * ACCEL_AMOUNT;
    public static final double STATIONARY_THRES_ROUND = 2 * DECEL_AMOUNT;
    public static final double UPHILL_ACCEL_BONUS = 0.1132/20;

    public static final float SPEED_WET_FREIGHT = 2f;
    public static final float SPEED_DRY_FREIGHT = 3f;
    public static final float SPEED_WET_NORMAL = 3f;
    public static final float SPEED_DRY_NORMAL = 4f;

    public PoweredClass1Rail(BlockBehaviour.Properties properties) {
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
            if (adjacentState.getBlock() instanceof Electrification750V) {
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
        if (entity instanceof AbstractMinecart cart) {
            boolean isPowered = state.getValue(POWERED);
            boolean hasOverrideSignal = holdingOverrideSignal(cart);

            if (!holdingStopSignal(cart)) {
                if (isPowered && !hasOverrideSignal) { // power and no override => accelerate as normal
                    Vec3 motion = cart.getDeltaMovement();
                    float maxSpd = getRailMaxSpeed(state, level, pos, cart);

                    // Check if this is an ascending rail (going uphill)
                    boolean isAscending = isAscendingRail(state);
                    double accelAmount = isAscending ? ACCEL_AMOUNT + UPHILL_ACCEL_BONUS : ACCEL_AMOUNT; // extra acceleration for going uphill

                    if (motion.x >= maxSpd - MAX_SPD_THRES_ROUND && motion.z < MAX_SPD_THRES_ROUND) cart.setDeltaMovement(maxSpd, motion.y, 0);
                    else if (motion.x < MAX_SPD_THRES_ROUND && motion.z >= maxSpd - MAX_SPD_THRES_ROUND) cart.setDeltaMovement(0, motion.y, maxSpd);
                    else { Vec3 accelAmount3 = CalcAccelAmount(motion.x, motion.z, accelAmount); cart.setDeltaMovement(motion.add(accelAmount3)); }
                }
                else if (!isPowered && !hasOverrideSignal) { // no power and no override => decelerate as normal
                    Vec3 motion = cart.getDeltaMovement();
                    if (motion.x < STATIONARY_THRES_ROUND && motion.z < STATIONARY_THRES_ROUND) cart.setDeltaMovement(0, 0, 0);
                    else { Vec3 accelAmount3 = CalcAccelAmount(motion.x, motion.z, DECEL_AMOUNT); cart.setDeltaMovement(motion.add(accelAmount3)); }
                }
            }
        } else {
            super.entityInside(state, level, pos, entity);
        }
    }

    private Vec3 CalcAccelAmount(double x, double z, double accelValue) {
        double xAccel = 0, zAccel = 0;
        if (x < 0) xAccel = -accelValue;
        else if (x > 0) xAccel = accelValue;
        if (z < 0) zAccel = -accelValue;
        else if (z > 0) zAccel = accelValue;
        return new Vec3(xAccel, 0, zAccel);
    }

    /**
     * Check if this rail is ascending (going uphill)
     */
    private boolean isAscendingRail(BlockState state) {
        if (!state.hasProperty(SHAPE)) {
            return false;
        }

        net.minecraft.world.level.block.state.properties.RailShape shape = state.getValue(SHAPE);
        return shape == net.minecraft.world.level.block.state.properties.RailShape.ASCENDING_EAST ||
                shape == net.minecraft.world.level.block.state.properties.RailShape.ASCENDING_WEST ||
                shape == net.minecraft.world.level.block.state.properties.RailShape.ASCENDING_NORTH ||
                shape == net.minecraft.world.level.block.state.properties.RailShape.ASCENDING_SOUTH;
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

    private boolean holdingOverrideSignal(AbstractMinecart cart) {
        ResourceLocation overrideSignalId = ResourceLocation.parse("elliesrailimprovements:signal_switch_override");
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

    private boolean holdingStopSignal(AbstractMinecart cart) {
        ResourceLocation overrideSignalId = ResourceLocation.parse("elliesrailimprovements:signal_stop");
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
}