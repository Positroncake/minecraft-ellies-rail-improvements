package dev.elliectron.ellierailmod.event;

import dev.elliectron.ellierailmod.ElliesRailImprovements;
import dev.elliectron.ellierailmod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartStoppingHandler {

    // Base deceleration rates
    private static final double BASE_DECELERATION_RATE = -0.085/20;
    private static final double RAIN_DECELERATION_MULTIPLIER = 0.7; // 20% less deceleration in rain
    private static final double ATTACHED_PAX_DECEL_FACTOR = 0.955; // Reduced deceleration per attached minecart (player)
    private static final double ATTACHED_FREIGHT_DECEL_FACTOR = 0.930; // Reduced deceleration per attached minecart (chest/hopper/etc)
    private static final double MIN_SPEED = 0.01;

    // Brake thermals values
    private static final double ESTOP_DECEL_BONUS = 1.50;
    private static final double DEFAULT_BRAKE_TEMP = 293.0; // Kelvin
    private static final double EMER_BRAKE_HEATING_RATE = 0.400; // K per tick when e-braking
    private static final double NORM_BRAKE_HEATING_RATE = 0.063;
    private static final double BRAKE_COOLING_RATE = 0.04; // K per tick when cooling
    private static final double MAX_BRAKE_TEMP = 1000.0; // Kelvin - maximum overheating
    private static final double BRAKE_EFFECTIVENESS_LOSS_PER_K = 0.0015; // 0.15% per Kelvin above default
    private static final double MIN_BRAKE_EFFECTIVENESS = 0.65; // 65% minimum effectiveness

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof AbstractMinecart minecart)) return;
        if (minecart.getPassengers().isEmpty()) return;

        Player player = null;
        for (Entity passenger : minecart.getPassengers())
            if (passenger instanceof Player) {
                player = (Player) passenger;
                break;
            }
        if (player == null) return;

        // Update brake temperature every tick
        updateBrakeTemperature(minecart, player);

        // Apply braking based on signals
        if (isHoldingSignalStop(player)) {
            Vec3 vv = minecart.getDeltaMovement();
            double spd = Math.sqrt(vv.x*vv.x + vv.z*vv.z);
            if (spd < 0.01) minecart.setDeltaMovement(0, 0, 0);
            else DecelerateMinecart(minecart, false);
        } else if (isHoldingEstopSignal(player)) {
            Vec3 vv = minecart.getDeltaMovement();
            double spd = Math.sqrt(vv.x*vv.x + vv.z*vv.z);
            if (spd < 0.01) minecart.setDeltaMovement(0, 0, 0);
            else DecelerateMinecart(minecart, true);
        }
    }

    private static void updateBrakeTemperature(AbstractMinecart minecart, Player player) {
        var nbt = minecart.getPersistentData();

        // Get current brake temperature (default to 293K if not set)
        double currentTemp = nbt.getDouble("brake_temperature");
        if (currentTemp == 0.0) {
            currentTemp = DEFAULT_BRAKE_TEMP;
        }

        boolean isEbraking = isHoldingEstopSignal(player);
        boolean isNbraking = isHoldingSignalStop(player);
        Vec3 vv = minecart.getDeltaMovement();
        double spd = Math.sqrt(vv.x*vv.x + vv.z*vv.z);

        if (isEbraking && spd > 0.01) {
            // Square law heating: 2x rate at 90 mph (2.01 blocks/tick)
            double speedMultiplier = Math.max(0.1, Math.pow(spd / 1.42, 2));
            currentTemp = Math.min(MAX_BRAKE_TEMP, currentTemp + EMER_BRAKE_HEATING_RATE * speedMultiplier);
        } else if (isNbraking && spd > 0.01) {
            // Square law heating: 2x rate at 90 mph (2.01 blocks/tick)
            double speedMultiplier = Math.max(0.1, Math.pow(spd / 1.42, 2));
            currentTemp = Math.min(MAX_BRAKE_TEMP, currentTemp + NORM_BRAKE_HEATING_RATE * speedMultiplier);
        } else {
            // Cool down brakes when not braking
            currentTemp = Math.max(DEFAULT_BRAKE_TEMP, currentTemp - BRAKE_COOLING_RATE);
        }

        // Store updated temperature
        nbt.putDouble("brake_temperature", currentTemp);

        // Optional: Send temperature warnings to player
        sendTemperatureWarnings(player, currentTemp);
    }

    public static double getBrakeEffectiveness(AbstractMinecart minecart) {
        var nbt = minecart.getPersistentData();
        double currentTemp = nbt.getDouble("brake_temperature");
        if (currentTemp == 0.0) currentTemp = DEFAULT_BRAKE_TEMP;

        double tempAboveDefault = Math.max(0, currentTemp - DEFAULT_BRAKE_TEMP);
        double effectivenessLoss = tempAboveDefault * BRAKE_EFFECTIVENESS_LOSS_PER_K;
        double effectiveness = 1.0 - effectivenessLoss;
        return Math.max(MIN_BRAKE_EFFECTIVENESS, effectiveness);
    }

    private static void sendTemperatureWarnings(Player player, double currentTemp) {
        // Only send warnings occasionally to avoid spam
        if (player.level().getGameTime() % 2 != 0) return; // Every 3 seconds

        String tempString = currentTemp + "";
        player.sendSystemMessage(Component.literal(tempString));

        // warn every 50 *C
        double tempCDeg = currentTemp - 273;
        if (tempCDeg % 50 == 0) player.sendSystemMessage(Component.literal(
                "§e§l[CAUTION] BRAKE TEMP HIGH: " + String.format("%.0f°C", tempCDeg)
                        + " - Braking reduced by " + ((currentTemp-DEFAULT_BRAKE_TEMP) * BRAKE_EFFECTIVENESS_LOSS_PER_K * 100) + "%"));
    }

    private static boolean isHoldingSignalStop(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.is(ModItems.SIGNAL_STOP.get()) || offHand.is(ModItems.SIGNAL_STOP.get());
    }

    private static boolean isHoldingEstopSignal(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.is(ModItems.SIGNAL_E_STOP.get()) || offHand.is(ModItems.SIGNAL_E_STOP.get());
    }

    public static void DecelerateMinecart(AbstractMinecart minecart, boolean isEstop) {
        Vec3 motion = minecart.getDeltaMovement();
        if (20 * Math.sqrt(motion.x * motion.x + motion.z * motion.z) < MIN_SPEED) {
            minecart.setDeltaMovement(0, motion.y, 0);
            return;
        }

        double dynamicDecelerationRate = calculateDynamicDecelerationRate(minecart, isEstop);
        Vec3 decelAmount = calcDecelAmount(motion.x, motion.y, motion.z, dynamicDecelerationRate);
        minecart.setDeltaMovement(motion.add(decelAmount));
    }

    private static double calculateDynamicDecelerationRate(AbstractMinecart minecart, boolean isEstop) {
        Level level = minecart.level();
        double decelRate = BASE_DECELERATION_RATE;
        if (isRainingAtPosition(level, minecart)) decelRate *= RAIN_DECELERATION_MULTIPLIER;

        int[] attachedCount = countAttachedMinecarts(minecart);
        int paxMinecartsNum = attachedCount[0];
        int freightMinecartsNum = attachedCount[1];
        decelRate *= Math.pow(ATTACHED_PAX_DECEL_FACTOR, paxMinecartsNum);
        decelRate *= Math.pow(ATTACHED_FREIGHT_DECEL_FACTOR, freightMinecartsNum);

        if (isEstop) decelRate *= ESTOP_DECEL_BONUS;

        double brakeEffectiveness = getBrakeEffectiveness(minecart);
        decelRate *= brakeEffectiveness;

        return decelRate;
    }

    private static boolean isRainingAtPosition(Level level, AbstractMinecart minecart) {
        // Check if it's raining in the biome and if the minecart is exposed to sky
        return level.isRainingAt(minecart.blockPosition()) || (level.isRaining() && level.canSeeSky(minecart.blockPosition()));
    }

    private static int[] countAttachedMinecarts(AbstractMinecart leadMinecart) {
        int countPax = 0;
        int countFreight = 0;
        Level level = leadMinecart.level();

        for (Entity entity : level.getEntitiesOfClass(AbstractMinecart.class, leadMinecart.getBoundingBox().inflate(9.0))) {
            if (entity != leadMinecart && entity instanceof AbstractMinecart otherCart) {
                double distance = leadMinecart.distanceTo(entity);
                if (distance <= 9.0) {
                    if (entity instanceof Minecart) ++countPax;
                    else ++countFreight;
                }
            }
        }

        return new int[] { countPax, countFreight } ;
    }

    private static Vec3 calcDecelAmount(double x, double y, double z, double decelerationRate) {
        double xAccel = 0, zAccel = 0;

        if (x < 0) xAccel = -decelerationRate;
        else if (x > 0) xAccel = decelerationRate;

        if (z < 0) zAccel = -decelerationRate;
        else if (z > 0) zAccel = decelerationRate;

        return new Vec3(xAccel, 0, zAccel);
    }
}