package info.mudbourn.mmsorigins.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The webbed slow from the arachnid web shot. It carries no tick behaviour; the
 * movement penalty is an attribute modifier attached in {@link MmsMobEffects}.
 */
public final class WebbedMobEffect extends MobEffect {

    public WebbedMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
