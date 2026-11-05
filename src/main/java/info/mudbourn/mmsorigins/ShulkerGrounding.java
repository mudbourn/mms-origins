package info.mudbourn.mmsorigins;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tracks who was struck by a shulk's bullet so they cannot open an elytra while still levitating.
 */
public final class ShulkerGrounding {

    private static final long DURATION_TICKS = 200;

    private static final Map<UUID, Long> GROUNDED = new ConcurrentHashMap<>();

    /** Grounds the victim, folding any open elytra. */
    public static void ground(LivingEntity victim) {
        GROUNDED.put(victim.getUUID(), victim.level().getGameTime() + DURATION_TICKS);
        if (victim.isFallFlying()) {
            victim.stopFallFlying();
        }
    }

    /** Whether the entity is still levitating from a shulk's bullet. */
    public static boolean isGrounded(LivingEntity entity) {
        Long until = GROUNDED.get(entity.getUUID());
        if (until == null) {
            return false;
        }
        if (entity.level().getGameTime() > until || !entity.hasEffect(MobEffects.LEVITATION)) {
            GROUNDED.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    private ShulkerGrounding() {
    }
}
