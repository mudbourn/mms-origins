package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.player.Player;

/**
 * Fishman riptide on dry land, paid for with tide magic and stored moisture.
 *
 * <p>Away from water or rain a riptide launch needs at least {@link #MIN_MOISTURE}
 * moisture and {@link #MAGIC_COST} tide magic, and spends {@link #MOISTURE_COST}
 * moisture and that magic, the same pool and gate the dash and beam use.
 */
public final class FishmanRiptide {

    private static final String MOISTURE_PATH = "wetness_moisture";
    private static final String MAGIC_PATH = "tide_magic_charge";
    private static final int MIN_MOISTURE = 210;
    private static final int MOISTURE_COST = 120;
    private static final int MAGIC_COST = 50;

    public static boolean isWet(Player player) {
        VariableIntPower moisture = find(player, MOISTURE_PATH);
        VariableIntPower magic = find(player, MAGIC_PATH);
        return moisture != null
            && magic != null
            && moisture.getValue() >= MIN_MOISTURE
            && magic.getValue() >= MAGIC_COST;
    }

    public static void drainWetness(Player player) {
        spend(player, find(player, MOISTURE_PATH), MOISTURE_COST);
        spend(player, find(player, MAGIC_PATH), MAGIC_COST);
    }

    private static void spend(Player player, VariableIntPower power, int amount) {
        if (power != null) {
            power.setValue(Math.max(power.getMin(), power.getValue() - amount));
            PowerHolderComponent.syncPower(player, power.getType());
        }
    }

    private static VariableIntPower find(Player player, String path) {
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().getNamespace().equals(MmsOrigins.MOD_ID)
                    && power.getType().getIdentifier().getPath().equals(path)) {
                return power;
            }
        }
        return null;
    }

    private FishmanRiptide() {
    }
}
