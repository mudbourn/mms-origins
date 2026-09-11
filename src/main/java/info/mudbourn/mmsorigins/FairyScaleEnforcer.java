package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Holds a fairy at its diminutive size, driven by the
 * {@code mms_origins:diminutive} marker.
 *
 * <p>Apoli's {@code origins:attribute} applies its scale modifier once, when the
 * power is gained, and that single apply can lose the race on a fresh world join
 * so a fairy spawns full size. The scale is enforced here instead: every server
 * tick each diminutive bearer is checked, and the scale modifier is re-added the
 * moment it is missing, so a mis-timed join self-corrects within a tick.
 */
public final class FairyScaleEnforcer {

    private static final Identifier MARKER =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "diminutive");
    private static final Identifier SCALE_MODIFIER_ID =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "fairy_scale");
    private static final double SCALE_MULTIPLIER = -0.6;

    private FairyScaleEnforcer() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(FairyScaleEnforcer::onEndWorldTick);
    }

    private static void onEndWorldTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            AttributeInstance scale = player.getAttribute(Attributes.SCALE);
            if (scale == null) {
                continue;
            }
            boolean fairy = isDiminutive(player);
            boolean applied = scale.getModifier(SCALE_MODIFIER_ID) != null;
            if (fairy && !applied) {
                scale.addTransientModifier(new AttributeModifier(
                    SCALE_MODIFIER_ID,
                    SCALE_MULTIPLIER,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (!fairy && applied) {
                scale.removeModifier(SCALE_MODIFIER_ID);
            }
        }
    }

    private static boolean isDiminutive(ServerPlayer player) {
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return false;
        }
        for (PowerType<?> type : component.getPowerTypes(true)) {
            if (MARKER.equals(type.getIdentifier())) {
                return true;
            }
        }
        return false;
    }
}
