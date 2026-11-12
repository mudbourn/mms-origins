package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.PhantomWings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands a flying phantom-form player's movement to {@link PhantomWings} in place of vanilla
 * creative flight. Only the local player moves itself; every other player is positioned by
 * the server.
 */
@Mixin(Player.class)
public abstract class PhantomWingsTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$wingedTravel(Vec3 input, CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer player && PhantomWings.travel(player, input)) {
            ci.cancel();
        }
    }
}
