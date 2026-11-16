package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Turns a rider's attack into the Grovekeeper's own strike and never lets them hit their mount.
@Mixin(Player.class)
public abstract class PlayerRiderAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$strikeAsGolem(Entity target, CallbackInfo ci) {
        if (((Player) (Object) this).getVehicle() instanceof FloranHenchman golem) {
            ci.cancel();
            golem.strikeFor((Player) (Object) this, target);
        }
    }
}
