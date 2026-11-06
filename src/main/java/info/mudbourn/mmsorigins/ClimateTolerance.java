package info.mudbourn.mmsorigins;

import com.github.thedeathlycow.thermoo.api.temperature.TemperatureAware;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Caps Thermoo temperature for origins adapted to a climate.
 *
 * <p>Resistance attributes only slow temperature change, so a long swim or a stretch on
 * fire still drives an adapted origin to the extreme. Each tick an immune origin has
 * that side of its temperature held at neutral, and a resistant origin is held within
 * {@link #TOLERANCE} of its scale, so it feels the climate without its severe effects.
 */
public final class ClimateTolerance {

    private static final float TOLERANCE = 0.5F;

    private static final List<PowerType<?>> COLD_IMMUNE = List.of(
        power("mms_origins", "cold_blooded"));

    private static final List<PowerType<?>> HEAT_IMMUNE = List.of(
        power("originstweaks", "fire_immunity"));

    private static final List<PowerType<?>> COLD_RESISTANT = List.of(
        power("originstweaks", "cold_resistance"),
        power("originstweaks", "predators_vigor"));

    private static final List<PowerType<?>> HEAT_RESISTANT = List.of(
        power("mms_origins", "heat_adaptation"),
        power("mms_origins", "nether_heat_immunity"),
        power("originstweaks", "predators_vigor"));

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                clamp(player);
            }
        });
    }

    private static void clamp(ServerPlayer player) {
        TemperatureAware temperature = TemperatureAware.get(player);
        int current = temperature.thermoo$getTemperature();
        int limit = current;
        if (current < 0) {
            if (holdsAny(player, COLD_IMMUNE)) {
                limit = 0;
            } else if (holdsAny(player, COLD_RESISTANT)) {
                limit = Math.max(current, Math.round(temperature.thermoo$getMinTemperature() * TOLERANCE));
            }
        } else if (current > 0) {
            if (holdsAny(player, HEAT_IMMUNE)) {
                limit = 0;
            } else if (holdsAny(player, HEAT_RESISTANT)) {
                limit = Math.min(current, Math.round(temperature.thermoo$getMaxTemperature() * TOLERANCE));
            }
        }
        if (limit != current) {
            temperature.thermoo$setTemperature(limit);
        }
    }

    private static boolean holdsAny(ServerPlayer player, List<PowerType<?>> powers) {
        for (PowerType<?> power : powers) {
            if (MmsOriginsPowers.holds(player, power)) {
                return true;
            }
        }
        return false;
    }

    private static PowerType<?> power(String namespace, String path) {
        return new PowerTypeReference<>(Identifier.fromNamespaceAndPath(namespace, path));
    }

    private ClimateTolerance() {
    }
}
