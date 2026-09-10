package info.mudbourn.mmsorigins.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * The caustic spores damage-over-time effect. It mirrors vanilla poison's feel,
 * dealing one magic point per tick and never dropping the victim below one
 * heart, but it is the mod's own effect rather than {@code minecraft:poison}, so
 * undead mobs receive it instead of being immune. The fixed tick interval, paired
 * with the application duration set in {@link MmsMobEffects}, delivers exactly
 * three ticks.
 */
public final class CausticSporesMobEffect extends MobEffect {

    public CausticSporesMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.hurtServer(level, entity.damageSources().magic(), MmsMobEffects.TICK_DAMAGE);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % MmsMobEffects.TICK_INTERVAL == 0;
    }
}
