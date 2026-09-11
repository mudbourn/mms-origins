package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fairy Allure: the mystique that charms villagers lures monsters to their doom.
 *
 * <p>A hostile mob with no quarry of its own reaches for a fairy from a fifth
 * farther than its usual sight, and prefers the nearest fairy in that reach over
 * any other prey. Targeting carries no Apoli hook, so this proactively hands the
 * mob its target every half second while the fairy stays unseen elsewhere. Once
 * chosen, the mob's own goals press the hunt.
 */
@Mixin(Mob.class)
public class FairyAllureMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$lureToFairy(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Enemy) || mob.tickCount % 10 != 0) {
            return;
        }
        LivingEntity current = mob.getTarget();
        if (current != null && current.isAlive()) {
            return;
        }
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 1.2;
        if (range <= 0.0) {
            return;
        }
        AABB reach = mob.getBoundingBox().inflate(range);
        Player nearest = null;
        double nearestSqr = range * range;
        for (Player player : mob.level().getEntitiesOfClass(Player.class, reach)) {
            if (player.isSpectator() || player.getAbilities().instabuild || !player.isAlive()) {
                continue;
            }
            if (!MmsOriginsPowers.FAIRY_ALLURE.isActive(player)) {
                continue;
            }
            double distanceSqr = mob.distanceToSqr(player);
            if (distanceSqr < nearestSqr && mob.getSensing().hasLineOfSight(player)) {
                nearest = player;
                nearestSqr = distanceSqr;
            }
        }
        if (nearest != null) {
            mob.setTarget(nearest);
        }
    }
}
