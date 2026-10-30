package info.mudbourn.mmsorigins;

import com.github.thedeathlycow.thermoo.api.ThermooAttributes;
import com.github.thedeathlycow.thermoo.api.temperature.TemperatureAware;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Softens temperature for every player and gives the waterskin a real cooling
 * sip.
 *
 * <p>Each point of Thermoo heat or frost resistance slows temperature change by
 * 10%, so the shared baseline halves both for every origin. Origin powers stack
 * their own resistance on top: sensitive origins carry -5 to land back at the
 * unsoftened pace, and adapted origins carry +3.5 to keep their edge over the
 * new baseline.
 */
public final class TemperatureBaseline {

    public static final double PLAYER_RESISTANCE = 5.0;

    public static final float WATERSKIN_MIN_COOLING = 0.45f;

    public static final float WATERSKIN_MAX_COOLING = 0.60f;

    public static void register() {
        ThermooAttributes.baseValueEvent(ThermooAttributes.HEAT_RESISTANCE)
            .register((entity, base) -> entity instanceof Player ? base + PLAYER_RESISTANCE : base);
        ThermooAttributes.baseValueEvent(ThermooAttributes.FROST_RESISTANCE)
            .register((entity, base) -> entity instanceof Player ? base + PLAYER_RESISTANCE : base);
    }

    /**
     * Sheds 45-60% of a player's built-up heat; does nothing when they are not
     * warm.
     */
    public static void coolFromWaterskin(Player player) {
        TemperatureAware temperature = TemperatureAware.get(player);
        int current = temperature.thermoo$getTemperature();
        if (current <= 0) {
            return;
        }
        RandomSource random = player.getRandom();
        float cooling = WATERSKIN_MIN_COOLING
            + random.nextFloat() * (WATERSKIN_MAX_COOLING - WATERSKIN_MIN_COOLING);
        temperature.thermoo$setTemperature(Math.round(current * (1.0f - cooling)));
    }

    private TemperatureBaseline() {
    }
}
