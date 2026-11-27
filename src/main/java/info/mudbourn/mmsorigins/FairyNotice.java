package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.power.FlyingMob;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/**
 * Lets hostile mobs spot a fairy from farther off than they would a human.
 *
 * <p>A fairy is small, bright and easy prey. A hostile mob with nothing to
 * fight picks out a fairy it can see within one and a half times its usual
 * follow range. Mobs that are neutral, or neutral until looked at like
 * endermen, do not. This only starts the chase: the mob's own goals take over
 * from there, so a creeper still creeps and a skeleton still shoots. Fairies
 * that are players or origin-carrying mobs, such as mms-dungeons' faction
 * humans, are both covered.
 */
public final class FairyNotice {

    private static final int CHECK_TICKS = 10;
    private static final double RANGE_MULTIPLIER = 1.5;
    private static final double MAX_FOLLOW_RANGE = 64.0;

    private FairyNotice() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(FairyNotice::onEndLevelTick);
    }

    private static void onEndLevelTick(ServerLevel level) {
        if (level.getGameTime() % CHECK_TICKS != 0) {
            return;
        }
        for (LivingEntity fairy : fairies(level)) {
            double reach = MAX_FOLLOW_RANGE * RANGE_MULTIPLIER;
            for (Mob mob : level.getEntitiesOfClass(Mob.class, fairy.getBoundingBox().inflate(reach), mob -> notices(mob, fairy))) {
                mob.setTarget(fairy);
            }
        }
    }

    private static List<LivingEntity> fairies(ServerLevel level) {
        List<LivingEntity> fairies = new ArrayList<>();
        for (Player player : level.players()) {
            if (!player.isCreative() && !player.isSpectator() && player.isAlive() && FairyScaleEnforcer.fairy(player)) {
                fairies.add(player);
            }
        }
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof FlyingMob && entity instanceof LivingEntity mob && mob.isAlive() && FairyScaleEnforcer.fairy(mob)) {
                fairies.add(mob);
            }
        }
        return fairies;
    }

    // A hostile mob with nothing to fight that can see the fairy within its stretched follow range.
    private static boolean notices(Mob mob, LivingEntity fairy) {
        if (!(mob instanceof Enemy) || mob instanceof NeutralMob || mob == fairy || mob.getTarget() != null || mob.isNoAi()) {
            return false;
        }
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE) * RANGE_MULTIPLIER;
        return mob.distanceToSqr(fairy) <= range * range
            && mob.canAttack(fairy)
            && mob.getSensing().hasLineOfSight(fairy);
    }
}
