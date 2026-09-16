package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.AbsolutReviveState;
import io.github.apace100.apoli.networking.ModPacketsC2S;
import io.github.apace100.apoli.networking.UseActivePowersPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops every active power a downed player tries to use.
 *
 * <p>Apoli funnels each key-triggered active through this one server handler, so
 * cancelling it here gates all of them at once while AbsolutRevive holds the
 * player down. Passive traits keep running; only the abilities the player fires
 * are refused. The check reads false whenever AbsolutRevive is absent.
 */
@Mixin(ModPacketsC2S.class)
public class ApoliActivePowerGateMixin {

    @Inject(method = "useActivePowers", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsOrigins$blockWhileDowned(UseActivePowersPacket packet, ServerPlayNetworking.Context context, CallbackInfo ci) {
        if (AbsolutReviveState.isIncapacitated(context.player())) {
            ci.cancel();
        }
    }
}
