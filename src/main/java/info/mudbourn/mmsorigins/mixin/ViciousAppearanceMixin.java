package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vicious Appearance: the overworld's timid creatures bolt from a beastfolk.
 *
 * <p>Cows, pigs, sheep, chickens, horses, and even spiders read a beastfolk as a
 * predator and flee. Flight has no Apoli hook, so every half second a scared mob
 * near a beastfolk drops its quarry and paths to open ground away from them; once
 * it is beyond the dread's reach its own goals take back over.
 */
@Mixin(Mob.class)
public abstract class ViciousAppearanceMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$flee(CallbackInfo ci) {
        if (!((Object) this instanceof PathfinderMob mob)) {
            return;
        }
        if (mob.tickCount % 10 != 0 || !mmsOrigins$isTimid(mob)) {
            return;
        }
        Player predator = mmsOrigins$nearestPredator(mob);
        if (predator == null) {
            return;
        }
        mob.setTarget(null);
        Vec3 away = DefaultRandomPos.getPosAway(mob, 16, 7, predator.position());
        if (away != null) {
            mob.getNavigation().moveTo(away.x, away.y, away.z, 1.4);
        }
    }

    private static boolean mmsOrigins$isTimid(PathfinderMob mob) {
        return mob instanceof Cow
            || mob instanceof Pig
            || mob instanceof Sheep
            || mob instanceof Chicken
            || mob instanceof Horse
            || mob instanceof Spider;
    }

    private static Player mmsOrigins$nearestPredator(PathfinderMob mob) {
        double range = MmsOriginsPowers.VICIOUS_APPEARANCE_RANGE;
        AABB reach = mob.getBoundingBox().inflate(range);
        Player nearest = null;
        double nearestSqr = range * range;
        for (Player player : mob.level().getEntitiesOfClass(Player.class, reach)) {
            if (player.isSpectator() || player.getAbilities().instabuild || !player.isAlive()) {
                continue;
            }
            if (!MmsOriginsPowers.VICIOUS_APPEARANCE.isActive(player)) {
                continue;
            }
            double distanceSqr = mob.distanceToSqr(player);
            if (distanceSqr < nearestSqr) {
                nearest = player;
                nearestSqr = distanceSqr;
            }
        }
        return nearest;
    }
}
