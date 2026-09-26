package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Hard-flushes a player's origin state so only what they currently hold remains.
 *
 * <p>Runs after every origin assignment and on join. Any layer whose held origin
 * no longer passes that layer's condition (a beastfolk collar kept after leaving
 * beastfolk) is cleared, and every power granted by an origin the player no longer
 * holds in any layer is revoked.
 */
public final class OriginFlush {

    private static final Identifier EMPTY = Identifier.fromNamespaceAndPath("origins", "empty");

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> flush(handler.getPlayer()));
    }

    public static void flush(ServerPlayer player) {
        OriginComponent origins = ModComponents.ORIGIN.maybeGet(player).orElse(null);
        PowerHolderComponent powers = PowerHolderComponent.KEY.getNullable(player);
        if (origins == null || powers == null) {
            return;
        }
        boolean changed = dropInvalidLayers(player, origins, powers);
        Set<Identifier> held = new HashSet<>();
        for (Origin origin : origins.getOrigins().values()) {
            if (origin != null) {
                held.add(origin.getIdentifier());
            }
        }
        List<PowerType<?>> staleTypes = new ArrayList<>();
        List<Identifier> staleSources = new ArrayList<>();
        for (PowerType<?> type : powers.getPowerTypes(true)) {
            for (Identifier source : powers.getSources(type)) {
                if (OriginRegistry.contains(source) && !EMPTY.equals(source) && !held.contains(source)) {
                    staleTypes.add(type);
                    staleSources.add(source);
                }
            }
        }
        for (int i = 0; i < staleTypes.size(); i++) {
            powers.removePower(staleTypes.get(i), staleSources.get(i));
        }
        if (changed || !staleTypes.isEmpty()) {
            origins.sync();
            powers.sync();
        }
    }

    // Clears every layer whose held origin its condition no longer offers to this player.
    private static boolean dropInvalidLayers(ServerPlayer player, OriginComponent origins, PowerHolderComponent powers) {
        boolean changed = false;
        boolean removed;
        do {
            removed = false;
            Iterator<Map.Entry<OriginLayer, Origin>> it = origins.getOrigins().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<OriginLayer, Origin> entry = it.next();
                Origin origin = entry.getValue();
                if (origin == null
                        || EMPTY.equals(origin.getIdentifier())
                        || !entry.getKey().isEnabled()
                        || entry.getKey().getOrigins(player).contains(origin.getIdentifier())) {
                    continue;
                }
                it.remove();
                powers.removeAllPowersFromSource(origin.getIdentifier());
                removed = true;
                changed = true;
            }
        } while (removed);
        return changed;
    }

    private OriginFlush() {
    }
}
