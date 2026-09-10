package info.mudbourn.mmsorigins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;

/**
 * Shared test for whether a piglin should leave a kinsman alone.
 *
 * <p>Kinship makes piglins neutral, not passive, but striking one does not brand
 * the kinsman a traitor to the whole pack. A struck piglin holds a private grudge
 * and answers only that kinsman, coming until it lands its own hit and then
 * settling. Every other piglin, even one caught by vanilla's shared anger, still
 * counts the kinsman as family and stands down. A zombified kinsman is never
 * spared, since their own kind turn on them regardless.
 */
public final class PiglinKinship {

    public static boolean isSpared(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof AbstractPiglin piglin)) {
            return false;
        }
        if (!(target instanceof Player player)) {
            return false;
        }
        if (!MmsOriginsPowers.KINSMEN.isActive(player) || MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            return false;
        }
        return !(piglin instanceof GrudgeHolder holder) || !player.getUUID().equals(holder.mmsOrigins$getGrudge());
    }

    /**
     * Whether a piglin leaves a zombified player be. Piglins and brutes both shun
     * the zombified as their own kind shun a zombified piglin, so neither picks one
     * as prey. That truce breaks the instant the zombified player draws a weapon:
     * a piglin already angered at them is provoked, not sparing, and hunts.
     */
    public static boolean sparesZombified(AbstractPiglin piglin, Player player) {
        if (!MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            return false;
        }
        return !isAngryAt(piglin, player);
    }

    /** Whether the piglin currently holds vanilla anger toward this player. */
    private static boolean isAngryAt(AbstractPiglin piglin, Player player) {
        return piglin.getBrain().getMemory(MemoryModuleType.ANGRY_AT)
            .map(uuid -> uuid.equals(player.getUUID()))
            .orElse(false);
    }

    private PiglinKinship() {
    }
}
