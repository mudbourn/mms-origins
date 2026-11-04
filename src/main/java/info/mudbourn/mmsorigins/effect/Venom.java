package info.mudbourn.mmsorigins.effect;

import info.mudbourn.mmsorigins.MmsOrigins;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tracks mobs poisoned by arachnid venom. While a mob's venom window is open,
 * {@code PoisonMobEffectMixin} swaps its poison damage for the venom damage
 * type, which is tagged to bypass armor and enchantments. The window lives only
 * in server memory, as does the per-victim lockout that blocks back-to-back
 * doses from any arachnid, and both are keyed weakly, so unloaded mobs drop
 * out on their own.
 */
public final class Venom {

    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "venom"));

    public static final int LOCKOUT_TICKS = 140;

    private static final Map<LivingEntity, Long> WINDOWS = new WeakHashMap<>();
    private static final Map<LivingEntity, Long> LAST_DOSE = new WeakHashMap<>();

    private Venom() {
    }

    /** Gives the mob poison and opens its venom window, unless it was dosed within the lockout. */
    public static void apply(LivingEntity target, int duration, int amplifier) {
        long now = target.level().getGameTime();
        Long last = LAST_DOSE.get(target);
        if (last != null && now - last < LOCKOUT_TICKS) {
            return;
        }
        LAST_DOSE.put(target, now);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier), null);
        WINDOWS.put(target, target.level().getGameTime() + duration);
    }

    /** True while the mob's current poison came from venom. */
    public static boolean isVenomed(LivingEntity entity) {
        Long until = WINDOWS.get(entity);
        if (until == null) {
            return false;
        }
        if (entity.level().getGameTime() > until) {
            WINDOWS.remove(entity);
            return false;
        }
        return true;
    }

    /** Builds a venom damage source for the given level. */
    public static DamageSource source(ServerLevel level) {
        return new DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE_TYPE));
    }
}
