package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Turns a Grovekeeper rider's arm swing into the golem's strike, so a click into empty air still swings it.
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class RiderSwingStrikeMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePunch", at = @At("TAIL"))
    private void mmsOrigins$strikeOnSwing(ServerboundPunchPacket packet, CallbackInfo ci) {
        if (this.player.getVehicle() instanceof FloranHenchman golem) {
            golem.strikeFor(this.player, null);
        }
    }
}
