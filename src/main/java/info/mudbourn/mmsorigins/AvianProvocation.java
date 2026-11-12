package info.mudbourn.mmsorigins;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Breaks an avian's truce with every monster near the one they strike.
 *
 * <p>Each {@link Enemy} mob within {@link #RADIUS} blocks of the struck mob takes the
 * avian as its attacker, so {@code AvianPeaceMixin} and {@code AvianForgetTargetMixin}
 * stop sparing them until that revenge memory lapses.
 */
public final class AvianProvocation {

    private static final double RADIUS = 24.0;

    private AvianProvocation() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) -> {
            if (!(entity instanceof Enemy)) {
                return;
            }
            if (!(source.getEntity() instanceof ServerPlayer player) || !MmsOriginsPowers.AVIAN_PEACE.isActive(player)) {
                return;
            }
            for (Mob mob : entity.level().getEntitiesOfClass(
                Mob.class,
                entity.getBoundingBox().inflate(RADIUS),
                mob -> mob instanceof Enemy && mob.isAlive()
            )) {
                mob.setLastHurtByMob(player);
                mob.setTarget(player);
            }
        });
    }
}
