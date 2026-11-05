package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.ShulkerGrounding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Grounds anything a player-fired shulker bullet strikes.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletGroundingMixin {

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void mmsOrigins$ground(EntityHitResult hit, CallbackInfo ci) {
        ShulkerBullet bullet = (ShulkerBullet) (Object) this;
        if (bullet.getOwner() instanceof Player && hit.getEntity() instanceof LivingEntity victim) {
            ShulkerGrounding.ground(victim);
        }
    }
}
