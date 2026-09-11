package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Charges a blazeborn's ability cooldowns faster while it burns, driven by the
 * {@code originstweaks:molten_core} marker.
 *
 * <p>Apoli-Legacy has no cooldown-modifier power and its cooldowns are keyed to
 * world time, so recovery is hurried here: each tick a marked bearer standing in
 * lava banks a second tick of recovery (twice as fast), and one merely on fire
 * banks a tick every other tick (half as fast again). Lava wins when both hold.
 */
public final class MoltenChargeCharger {

    private static final Identifier MARKER =
        Identifier.fromNamespaceAndPath("originstweaks", "molten_core");

    private MoltenChargeCharger() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MoltenChargeCharger::onEndWorldTick);
    }

    private static void onEndWorldTick(ServerLevel level) {
        boolean fireTickHalf = level.getGameTime() % 2 == 0;
        for (ServerPlayer player : level.players()) {
            if (!isMoltenCharged(player)) {
                continue;
            }
            long extraTicks;
            if (player.isInLava()) {
                extraTicks = 1;
            } else if (player.isOnFire()) {
                extraTicks = fireTickHalf ? 1 : 0;
            } else {
                continue;
            }
            if (extraTicks == 0) {
                continue;
            }
            for (CooldownPower cooldown : PowerHolderComponent.getPowers(player, CooldownPower.class)) {
                ((MoltenCharge) cooldown).mmsOrigins$accelerate(extraTicks);
            }
        }
    }

    private static boolean isMoltenCharged(ServerPlayer player) {
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
