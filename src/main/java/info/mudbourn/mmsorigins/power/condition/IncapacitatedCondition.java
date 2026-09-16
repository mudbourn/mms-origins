package info.mudbourn.mmsorigins.power.condition;

import info.mudbourn.mmsorigins.AbsolutReviveState;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;

/**
 * True while AbsolutRevive holds the entity in its downed state. Resource-driven
 * abilities that keep firing on their own gate on this so they stop when the
 * bearer goes down; it reads false whenever AbsolutRevive is absent.
 */
public final class IncapacitatedCondition {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return AbsolutReviveState.isIncapacitated(entity);
    }

    private IncapacitatedCondition() {
    }
}
