package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.effect.Venom;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Poisons the target with arachnid venom. Run as a target action. The poison is
 * vanilla poison, but its ticks deal venom damage that skips armor and
 * Protection for as long as this dose lasts.
 */
public final class VenomPoisonAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("duration", SerializableDataTypes.INT, 100)
            .add("amplifier", SerializableDataTypes.INT, 1);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity target) || !(target.level() instanceof ServerLevel)) {
            return;
        }
        Venom.apply(target, data.getInt("duration"), data.getInt("amplifier"));
    }

    private VenomPoisonAction() {
    }
}
