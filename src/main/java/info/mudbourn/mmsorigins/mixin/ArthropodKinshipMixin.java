package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Arthropod Kinship: arthropod mobs will not hunt an arachnid bearer.
 *
 * <p>After each server AI step this drops any arthropod's target that is a
 * bearer, covering both fresh acquisition and a lock a goal already holds. A
 * bearer who has struck the mob stays its target through its revenge memory.
 */
@Mixin(Mob.class)
public class ArthropodKinshipMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$spareArachnid(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player) || mob.getLastHurtByMob() == target) {
            return;
        }
        if (mob.getType().is(EntityTypeTags.ARTHROPOD) && MmsOriginsPowers.ARTHROPOD_KINSHIP.isActive(target)) {
            mob.setTarget(null);
        }
    }
}
