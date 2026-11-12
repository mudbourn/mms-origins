package info.mudbourn.mmsorigins.power.condition;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;

/**
 * Compares a resource against a fraction of its own maximum, so a threshold such as
 * "more than a third" follows the bar if its maximum is rebalanced.
 */
public final class ResourceFractionCondition {

    public static SerializableData data() {
        return new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.DOUBLE, 0.5);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        PowerType<?> resource = data.get("resource");
        Comparison comparison = data.get("comparison");
        for (VariableIntPower power : PowerHolderComponent.getPowers(entity, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(resource.getIdentifier())) {
                return comparison.compare(power.getValue(), power.getMax() * data.getDouble("compare_to"));
            }
        }
        return false;
    }

    private ResourceFractionCondition() {
    }
}
