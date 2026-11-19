package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Passes a mob's blow at a Grovekeeper's rider onto the golem, leaving the rider open only to players.
@Mixin(ServerPlayer.class)
public abstract class RiderShieldMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$shieldRider(ServerLevel level, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.getVehicle() instanceof FloranHenchman golem && source.getEntity() instanceof Mob) {
            cir.setReturnValue(golem.hurtServer(level, source, amount));
        }
    }
}
