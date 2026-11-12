package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fairy Allure: the mystique that charms villagers lures monsters to their doom.
 *
 * <p>An explicitly hostile mob with no quarry of its own sometimes fixes on a fairy it can
 * see within its usual sight. Targeting carries no Apoli hook, so every half
 * second this rolls a chance to hand the mob the nearest visible fairy. Once
 * chosen, the mob's own goals press the hunt. Neutral mobs such as endermen,
 * zombified piglins and piglins are never lured, nor are spiders in bright
 * light, where vanilla spiders turn neutral.
 */
@Mixin(Mob.class)
public class FairyAllureMixin {

    private static final float LURE_CHANCE = 0.25F;

    private static final float SPIDER_NEUTRAL_LIGHT = 0.5F;

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$lureToFairy(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mmsOrigins$isHostile(mob) || (mob.tickCount + mob.getId()) % 10 != 0) {
            return;
        }
        LivingEntity current = mob.getTarget();
        if (current != null && current.isAlive()) {
            return;
        }
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (range <= 0.0 || mob.getRandom().nextFloat() >= LURE_CHANCE) {
            return;
        }
        Player nearest = null;
        double nearestSqr = range * range;
        for (Player player : mob.level().players()) {
            double distanceSqr = mob.distanceToSqr(player);
            if (distanceSqr >= nearestSqr) {
                continue;
            }
            if (player.isSpectator() || player.getAbilities().instabuild || !player.isAlive()) {
                continue;
            }
            if (MmsOriginsPowers.FAIRY_ALLURE.isActive(player) && mob.getSensing().hasLineOfSight(player)) {
                nearest = player;
                nearestSqr = distanceSqr;
            }
        }
        if (nearest != null) {
            mob.setTarget(nearest);
        }
    }

    private static boolean mmsOrigins$isHostile(Mob mob) {
        if (mob instanceof Spider) {
            return mob.getLightLevelDependentMagicValue() < SPIDER_NEUTRAL_LIGHT;
        }
        return mob instanceof Enemy && !(mob instanceof NeutralMob) && !(mob instanceof Piglin);
    }
}
