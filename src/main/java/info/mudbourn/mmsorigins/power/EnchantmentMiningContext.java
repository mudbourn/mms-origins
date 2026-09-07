package info.mudbourn.mmsorigins.power;

import net.minecraft.world.entity.player.Player;

/**
 * Holds the player currently breaking a block so that the stateless
 * {@code EnchantmentHelper.getItemEnchantmentLevel} lookup can find their
 * {@link ModifyEnchantmentLevelPower}s. Set around block breaking and cleared
 * after, mirroring how Apoli threads a miner through its own enchantment hooks.
 */
public final class EnchantmentMiningContext {

    private static final ThreadLocal<Player> MINER = new ThreadLocal<>();

    public static void set(Player player) {
        MINER.set(player);
    }

    public static void clear() {
        MINER.remove();
    }

    public static Player get() {
        return MINER.get();
    }

    private EnchantmentMiningContext() {
    }
}
