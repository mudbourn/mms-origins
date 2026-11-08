package info.mudbourn.mmsorigins.compat;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.SelfActionWhenHitPower;
import info.mudbourn.mmscombat.revive.SelfReviveEvents;

// Voices mms-combat's self-revive strain through the player's origin sounds power, since that power only fires on a real hit.
public final class SelfReviveStrainSoundBridge {

    private SelfReviveStrainSoundBridge() {
    }

    public static void register() {
        SelfReviveEvents.STRAIN.register((player, source) -> {
            boolean voiced = false;
            for (SelfActionWhenHitPower power : PowerHolderComponent.getPowers(player, SelfActionWhenHitPower.class)) {
                if (power.getType().getIdentifier().getPath().contains("_sounds")) {
                    power.whenHit(source, 0.0F);
                    voiced = true;
                }
            }
            return voiced;
        });
    }
}
