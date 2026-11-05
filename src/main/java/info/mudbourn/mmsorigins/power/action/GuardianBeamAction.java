package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.GuardianBeam;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Advances the caster's guardian beam by one held tick; see {@link GuardianBeam}.
 */
public final class GuardianBeamAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("range", SerializableDataTypes.DOUBLE, 16.0)
            .add("cone_degrees", SerializableDataTypes.DOUBLE, 15.0)
            .add("charge_ticks", SerializableDataTypes.INT, 30)
            .add("damage", SerializableDataTypes.FLOAT, 6.0F)
            .add("on_fire", ApoliDataTypes.ENTITY_ACTION, null);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof LivingEntity caster) {
            GuardianBeam.channel(
                caster,
                data.getDouble("range"),
                data.getDouble("cone_degrees"),
                data.getInt("charge_ticks"),
                data.getFloat("damage"),
                data.get("on_fire"));
        }
    }

    private GuardianBeamAction() {
    }
}
