package info.mudbourn.mmsorigins;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Runs a mob's per-step behaviour for the spore haze bond.
 *
 * <p>A neutral mob drops any lock on a truffle it is neutral to. A soldier keeps
 * its owner's company and fights only what the owner fights or what hurts the
 * owner, and its service ends when the time lapses or the owner is gone.
 */
public final class HazeSoldiers {

    private static final int FOE_MEMORY_TICKS = 100;
    private static final double FOE_RANGE_SQR = 32.0 * 32.0;
    private static final double FOLLOW_START_SQR = 6.0 * 6.0;
    private static final double FOLLOW_STOP_SQR = 3.0 * 3.0;
    private static final double FOLLOW_SPEED = 1.2;
    private static final int CUE_INTERVAL = 10;

    private HazeSoldiers() {
    }

    public static void step(Mob mob, ServerLevel level) {
        if (!HazeBond.hasBond(mob)) {
            return;
        }
        if (mob.getTarget() instanceof Player player && HazeBond.isNeutralTo(mob, player.getUUID())) {
            mob.setTarget(null);
        }
        UUID master = HazeBond.of(mob).master().orElse(null);
        if (master == null) {
            return;
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(master);
        if (HazeBond.activeMaster(mob) == null || owner == null || owner.level() != level || !owner.isAlive()) {
            HazeBond.retire(mob);
            mob.setTarget(null);
            mob.getNavigation().stop();
            return;
        }
        cue(mob, level);
        LivingEntity foe = pickFoe(mob, owner);
        mob.setTarget(foe);
        if (foe == null) {
            follow(mob, owner);
        }
    }

    private static void cue(Mob mob, ServerLevel level) {
        if (mob.tickCount % CUE_INTERVAL != 0) {
            return;
        }
        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            mob.getX(),
            mob.getY() + mob.getBbHeight() + 0.2,
            mob.getZ(),
            2,
            0.25,
            0.1,
            0.25,
            0.0
        );
    }

    private static LivingEntity pickFoe(Mob mob, ServerPlayer owner) {
        LivingEntity attacker = owner.getLastHurtByMob();
        if (owner.tickCount - owner.getLastHurtByMobTimestamp() < FOE_MEMORY_TICKS && isFoe(mob, owner, attacker)) {
            return attacker;
        }
        LivingEntity victim = owner.getLastHurtMob();
        if (owner.tickCount - owner.getLastHurtMobTimestamp() < FOE_MEMORY_TICKS && isFoe(mob, owner, victim)) {
            return victim;
        }
        return null;
    }

    private static boolean isFoe(Mob mob, ServerPlayer owner, LivingEntity foe) {
        if (foe == null || foe == mob || foe == owner || !foe.isAlive() || foe.level() != mob.level()) {
            return false;
        }
        if (foe.distanceToSqr(mob) > FOE_RANGE_SQR) {
            return false;
        }
        return !(foe instanceof Mob other && HazeBond.isSoldierOf(other, owner.getUUID()));
    }

    private static void follow(Mob mob, ServerPlayer owner) {
        double distance = mob.distanceToSqr(owner);
        if (distance > FOLLOW_START_SQR) {
            mob.getNavigation().moveTo(owner, FOLLOW_SPEED);
        } else if (distance < FOLLOW_STOP_SQR) {
            mob.getNavigation().stop();
        }
    }
}
