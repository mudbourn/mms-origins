package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.VariableIntPower;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

/**
 * Clears a piglin's zombification when they die and respawn.
 *
 * <p>Death is meant to burn the sickness out, but the meter is a power resource
 * that Apoli copies onto the respawned player, so setting it to zero on the dying
 * entity is undone the moment they come back. Resetting it on the fresh player,
 * after the copy, is the one place the wipe survives. A player merely returning
 * from the End is alive and left untouched.
 */
public final class ZombieMeterDeathReset {

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) {
                return;
            }
            Power power = MmsOriginsPowers.ZOMBIE_METER.get(newPlayer);
            if (power instanceof VariableIntPower meter && meter.getValue() != 0) {
                meter.setValue(0);
                PowerHolderComponent.syncPower(newPlayer, MmsOriginsPowers.ZOMBIE_METER);
            }
        });
    }

    private ZombieMeterDeathReset() {
    }
}
