package info.mudbourn.mmsorigins.power.condition;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;

/**
 * Passes at random with a chance that falls from 1 at no strain to 0 at full
 * strain, so sun gains gated on it slow down while the strain resource is high.
 */
public final class SunStrainCondition {

    public static SerializableData data() {
        return new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        PowerType<?> type = data.get("resource");
        for (VariableIntPower power : PowerHolderComponent.getPowers(entity, VariableIntPower.class)) {
            if (!power.getType().getIdentifier().equals(type.getIdentifier())) {
                continue;
            }
            int span = power.getMax() - power.getMin();
            if (span <= 0) {
                return true;
            }
            double strain = (double) (power.getValue() - power.getMin()) / span;
            return entity.getRandom().nextDouble() >= strain;
        }
        return true;
    }

    private SunStrainCondition() {
    }
}
