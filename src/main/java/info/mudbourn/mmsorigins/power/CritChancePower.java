package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Adds to the holder's Critical Strike crit chance. Chances from every active
 * instance add together.
 */
public final class CritChancePower extends Power {

    private final float chance;

    public CritChancePower(PowerType<?> type, LivingEntity entity, float chance) {
        super(type, entity);
        this.chance = chance;
    }

    public float getChance() {
        return chance;
    }

    /**
     * Sums the chance of every active crit chance power on the entity.
     */
    public static float totalChance(LivingEntity entity) {
        float total = 0.0F;
        for (CritChancePower power : PowerHolderComponent.getPowers(entity, CritChancePower.class)) {
            total += power.getChance();
        }
        return total;
    }
}
