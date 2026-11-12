package info.mudbourn.mmsorigins;

import net.minecraft.world.entity.LivingEntity;

/**
 * The mod-wide rule for what counts as a boss: any living entity whose max
 * health is above {@link #MAX_HEALTH}. It covers vanilla bosses, minibosses and
 * modded bosses alike without relying on any tag.
 */
public final class Bosses {

    public static final float MAX_HEALTH = 50.0F;

    private Bosses() {
    }

    /**
     * Whether the entity counts as a boss.
     */
    public static boolean isBoss(LivingEntity entity) {
        return entity.getMaxHealth() > MAX_HEALTH;
    }
}
