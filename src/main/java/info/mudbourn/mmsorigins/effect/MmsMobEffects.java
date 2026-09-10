package info.mudbourn.mmsorigins.effect;

import info.mudbourn.mmsorigins.MmsOrigins;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Registers the caustic spores effect and owns the per-mob mark that stops
 * combat spores from stacking. A mob struck by caustic spores is marked for
 * {@link #MARK_TICKS} ticks; while marked it takes no fresh dose, so a truffle
 * cannot pile ticks onto one target by swinging repeatedly. The mark lives only
 * in server memory and is keyed weakly, so unloaded mobs drop out on their own.
 */
public final class MmsMobEffects {

    public static final int TICK_INTERVAL = 20;
    public static final int TICK_COUNT = 3;
    public static final int EFFECT_DURATION = TICK_INTERVAL * TICK_COUNT;
    public static final int MARK_TICKS = 240;
    public static final float TICK_DAMAGE = 1.0F;

    private static final int COLOR = 0x4E9331;
    private static final Map<LivingEntity, Long> MARKS = new WeakHashMap<>();

    public static final Holder<MobEffect> CAUSTIC_SPORES = create();

    private MmsMobEffects() {
    }

    public static void register() {
    }

    /** True while the mob is still inside its no-stacking window. */
    public static boolean isMarked(LivingEntity entity) {
        Long until = MARKS.get(entity);
        if (until == null) {
            return false;
        }
        if (entity.level().getGameTime() >= until) {
            MARKS.remove(entity);
            return false;
        }
        return true;
    }

    /** Doses the mob with a fresh three-tick round and opens its mark window. */
    public static void applyTo(LivingEntity entity) {
        entity.addEffect(
            new MobEffectInstance(CAUSTIC_SPORES, EFFECT_DURATION, 0, false, true, true),
            null);
        MARKS.put(entity, entity.level().getGameTime() + MARK_TICKS);
    }

    private static Holder<MobEffect> create() {
        Identifier id = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "caustic_spores");
        return Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            id,
            new CausticSporesMobEffect(MobEffectCategory.HARMFUL, COLOR));
    }
}
