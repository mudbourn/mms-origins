package info.mudbourn.mmsorigins.power.condition;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;

/**
 * Passes at random with a chance that climbs smoothly from nothing at sunrise and sunset
 * to {@code peak} at noon, so a per-tick action gated on it gains on average that much per tick.
 */
public final class SunIntakeCondition {

    private static final double NOON = 6000.0;

    private static final double HALF_DAY = 6000.0;

    private static final double DEFAULT_PEAK = 0.4;

    private static final double DEFAULT_SHARPNESS = 3.0;

    /** Intake rate, one gain every six ticks, past which the sun counts as high. */
    public static final double MAELS_BLESSING_RATE = 1.0 / 6.0;

    public static SerializableData data() {
        return new SerializableData()
            .add("peak", SerializableDataTypes.DOUBLE, DEFAULT_PEAK)
            .add("sharpness", SerializableDataTypes.DOUBLE, DEFAULT_SHARPNESS);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        double chance = rate(entity, data.getDouble("peak"), data.getDouble("sharpness"));
        return entity.getRandom().nextDouble() < chance;
    }

    /** Whether the entity stands under open sky while the intake rate is at least {@link #MAELS_BLESSING_RATE}. */
    public static boolean maelsBlessing(Entity entity) {
        return entity.level().canSeeSky(entity.blockPosition().above())
                && rate(entity, DEFAULT_PEAK, DEFAULT_SHARPNESS) >= MAELS_BLESSING_RATE;
    }

    /** Mael's Blessing check usable as an entity condition. */
    public static boolean maelsBlessingCondition(SerializableData.Instance data, Entity entity) {
        return maelsBlessing(entity);
    }

    /** How strong the sun is on the intake curve, from 0 at sunrise and sunset to 1 at noon. */
    public static double strength(Entity entity) {
        return rate(entity, 1.0, DEFAULT_SHARPNESS);
    }

    private static double rate(Entity entity, double peak, double sharpness) {
        double time = entity.level().getDayTime() % 24000L;
        double height = 1.0 - Math.abs(time - NOON) / HALF_DAY;
        if (height <= 0.0) {
            return 0.0;
        }
        return peak * Math.pow(height, sharpness);
    }

    private SunIntakeCondition() {
    }
}
