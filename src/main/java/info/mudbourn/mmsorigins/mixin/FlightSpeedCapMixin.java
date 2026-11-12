package info.mudbourn.mmsorigins.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class FlightSpeedCapMixin {

    // 50 blocks per second
    private static final double MAX_FLIGHT_SPEED = 2.5;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mmsOrigins$capFlightSpeed(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!player.isFallFlying() && !player.getAbilities().flying) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.lengthSqr() > MAX_FLIGHT_SPEED * MAX_FLIGHT_SPEED) {
            player.setDeltaMovement(motion.normalize().scale(MAX_FLIGHT_SPEED));
        }
    }
}
