package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.player.Player;

/**
 * Fishman riptide on dry land. A fishman still carrying wetness may launch a
 * riptide trident away from water or rain, but every dry launch spends the whole
 * wetness reserve, so the ability costs the water power that grants it.
 */
public final class FishmanRiptide {

    private static final String WETNESS_RESOURCE_PATH = "wetness_resource";

    public static boolean isWet(Player player) {
        VariableIntPower wetness = findWetness(player);
        return wetness != null && wetness.getValue() > 0;
    }

    public static void drainWetness(Player player) {
        VariableIntPower wetness = findWetness(player);
        if (wetness != null && wetness.getValue() > wetness.getMin()) {
            wetness.setValue(wetness.getMin());
            PowerHolderComponent.syncPower(player, wetness.getType());
        }
    }

    private static VariableIntPower findWetness(Player player) {
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().getPath().endsWith(WETNESS_RESOURCE_PATH)) {
                return power;
            }
        }
        return null;
    }

    private FishmanRiptide() {
    }
}
