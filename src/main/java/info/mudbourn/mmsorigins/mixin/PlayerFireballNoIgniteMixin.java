package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps a player's small fireballs from setting other players alight, so a
 * Blazeborn volley still deals its hit damage in PvP without the burn.
 */
@Mixin(SmallFireball.class)
public abstract class PlayerFireballNoIgniteMixin {

    @WrapWithCondition(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"
        )
    )
    private boolean mmsOrigins$skipPlayerIgnite(Entity target, float seconds, EntityHitResult hit) {
        return !(target instanceof Player && ((SmallFireball) (Object) this).getOwner() instanceof Player);
    }
}
