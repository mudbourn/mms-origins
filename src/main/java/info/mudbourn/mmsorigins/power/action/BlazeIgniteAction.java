package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.BlazebornFire;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;

/**
 * Sets the entity alight with a Blazeborn's flame, which burns without building Scorchful heat.
 */
public final class BlazeIgniteAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("duration", SerializableDataTypes.INT, 6);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!entity.level().isClientSide()) {
            BlazebornFire.ignite(entity, data.getInt("duration"));
        }
    }

    private BlazeIgniteAction() {
    }
}
