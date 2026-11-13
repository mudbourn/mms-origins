package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Hold-jump climbing for gliding Avians. The holder's client eases the body into a
 * steady climb while the jump key is down, and the server charges stamina from the
 * same player's last input.
 */
public final class AvianClimb {

    private static final int CHARGE_INTERVAL = 4;
    // 10 stamina per second
    private static final int CHARGE = 2;
    // 15 stamina per second
    private static final int HEAVY_CHARGE = 3;
    private static final int HEAVY_ARMOR = 13;
    private static final int GROUNDING_ARMOR = 20;
    private static final String STAMINA_PATH = "gift_of_the_skies_stamina";

    private static BooleanSupplier localJump = () -> false;

    private AvianClimb() {
    }

    public static void setLocalJump(BooleanSupplier supplier) {
        localJump = supplier;
    }

    /** Whether the player is holding jump, read from the keyboard on the client and the last input on the server. */
    public static boolean holdsJump(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getLastClientInput().jump();
        }
        return player.level().isClientSide() && localJump.getAsBoolean();
    }

    /** Whether the player is a gliding Avian holding jump with stamina to spend and light enough armor. */
    public static boolean climbing(Player player) {
        if (!player.isFallFlying() || !MmsOriginsPowers.holds(player, MmsOriginsPowers.AVIAN_PEACE)) {
            return false;
        }
        if (player.getArmorValue() >= GROUNDING_ARMOR || !holdsJump(player)) {
            return false;
        }
        VariableIntPower stamina = stamina(player);
        return stamina != null && stamina.getValue() > stamina.getMin();
    }

    /** Spends the climb's stamina on the server while the player climbs. */
    public static void charge(ServerPlayer player) {
        if (player.tickCount % CHARGE_INTERVAL != 0 || !climbing(player)) {
            return;
        }
        VariableIntPower stamina = stamina(player);
        int cost = player.getArmorValue() >= HEAVY_ARMOR ? HEAVY_CHARGE : CHARGE;
        stamina.setValue(Math.max(stamina.getMin(), stamina.getValue() - cost));
        PowerHolderComponent.syncPower(player, stamina.getType());
    }

    private static VariableIntPower stamina(Player player) {
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().getPath().equals(STAMINA_PATH)) {
                return power;
            }
        }
        return null;
    }
}
