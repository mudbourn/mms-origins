package info.mudbourn.mmsorigins.power.condition;

import info.mudbourn.mmsorigins.effect.MmsMobEffects;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * True while the entity is inside its caustic spores no-stacking window. Combat
 * Spores tests the target with this so a marked mob costs the truffle nothing and
 * takes no fresh dose until the window lapses.
 */
public final class CausticMarkedCondition {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity instanceof LivingEntity target && MmsMobEffects.isMarked(target);
    }

    private CausticMarkedCondition() {
    }
}
