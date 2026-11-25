package info.mudbourn.mmsorigins.power.condition;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** Passes while a sun reserve sits below the share the hour allows, so intake never overshoots the cap. */
public final class SunCapCondition {

    private static final double DAY_LENGTH = 24000.0;

    private static final double NOON = 6000.0;

    private static final double HALF_DAY = 6000.0;

    public static SerializableData data() {
        return new SerializableData()
            .add("resource", SerializableDataTypes.IDENTIFIER)
            .add("night_capacity", SerializableDataTypes.DOUBLE, 0.5);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        Identifier id = data.getId("resource");
        for (VariableIntPower power : PowerHolderComponent.getPowers(entity, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(id)) {
                double night = data.getDouble("night_capacity");
                double share = night + (1.0 - night) * daylight(entity);
                return power.getValue() < (int) Math.round(power.getMax() * share);
            }
        }
        return false;
    }

    private static double daylight(Entity entity) {
        double time = entity.level().getOverworldClockTime() % DAY_LENGTH;
        return Math.max(0.0, 1.0 - Math.abs(time - NOON) / HALF_DAY);
    }

    private SunCapCondition() {
    }
}
