package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.effect.MmsMobEffects;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Doses the target with caustic spores. Run as a target action, so the entity is
 * the mob that was hit. A mob already inside its no-stacking window is left
 * alone; otherwise it takes a fresh three-tick round and is marked.
 */
public final class CausticSporesAction {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity target) || !(target.level() instanceof ServerLevel)) {
            return;
        }
        if (MmsMobEffects.isMarked(target)) {
            return;
        }
        MmsMobEffects.applyTo(target);
    }

    private CausticSporesAction() {
    }
}
