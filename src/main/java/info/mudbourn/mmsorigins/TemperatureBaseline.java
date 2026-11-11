package info.mudbourn.mmsorigins;

import com.github.thedeathlycow.thermoo.api.ThermooAttributes;
import com.github.thedeathlycow.thermoo.api.temperature.TemperatureAware;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * Softens temperature for every player and gives the waterskin a real cooling
 * sip.
 *
 * <p>Each point of Thermoo heat or frost resistance slows temperature change by
 * 10%, so the shared baseline halves both for every origin. Origin powers stack
 * their own resistance on top: sensitive origins carry -5 to land back at the
 * unsoftened pace, and adapted origins carry +3.5 to keep their edge over the
 * new baseline. A waterskin sip also leaves {@link #WATERSKIN_WARD_RESISTANCE}
 * extra heat resistance for {@link #WATERSKIN_WARD_TICKS}, refreshed on each sip.
 */
public final class TemperatureBaseline {

    public static final double PLAYER_RESISTANCE = 5.0;

    public static final float WATERSKIN_MIN_COOLING = 0.45f;

    public static final float WATERSKIN_MAX_COOLING = 0.60f;

    public static final double WATERSKIN_WARD_RESISTANCE = 4.0;

    public static final int WATERSKIN_WARD_TICKS = 20 * 120;

    private static final Identifier WATERSKIN_WARD_ID =
        Identifier.fromNamespaceAndPath("mms_origins", "waterskin_ward");

    private static final Map<UUID, Long> WARD_EXPIRY = new HashMap<>();

    public static void register() {
        ThermooAttributes.baseValueEvent(ThermooAttributes.HEAT_RESISTANCE)
            .register((entity, base) -> entity instanceof Player ? base + PLAYER_RESISTANCE : base);
        ThermooAttributes.baseValueEvent(ThermooAttributes.FROST_RESISTANCE)
            .register((entity, base) -> entity instanceof Player ? base + PLAYER_RESISTANCE : base);
        ServerTickEvents.END_SERVER_TICK.register(TemperatureBaseline::expireWards);
    }

    /**
     * Sheds 45-60% of a player's built-up heat; does nothing when they are not
     * warm.
     */
    public static void coolFromWaterskin(Player player) {
        wardFromWaterskin(player);
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

    private static void wardFromWaterskin(Player player) {
        AttributeInstance resistance = player.getAttribute(ThermooAttributes.HEAT_RESISTANCE);
        if (resistance == null) {
            return;
        }
        if (!resistance.hasModifier(WATERSKIN_WARD_ID)) {
            resistance.addTransientModifier(new AttributeModifier(
                WATERSKIN_WARD_ID,
                WATERSKIN_WARD_RESISTANCE,
                AttributeModifier.Operation.ADD_VALUE));
        }
        WARD_EXPIRY.put(player.getUUID(), player.level().getGameTime() + WATERSKIN_WARD_TICKS);
    }

    private static void expireWards(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Long>> entries = WARD_EXPIRY.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<UUID, Long> entry = entries.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null && player.level().getGameTime() < entry.getValue()) {
                continue;
            }
            if (player != null) {
                AttributeInstance resistance = player.getAttribute(ThermooAttributes.HEAT_RESISTANCE);
                if (resistance != null) {
                    resistance.removeModifier(WATERSKIN_WARD_ID);
                }
            }
            entries.remove();
        }
    }

    private TemperatureBaseline() {
    }
}
