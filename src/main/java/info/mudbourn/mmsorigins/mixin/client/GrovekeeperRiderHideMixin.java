package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Leaves a player riding a Grovekeeper undrawn.
@Mixin(EntityRenderer.class)
public abstract class GrovekeeperRiderHideMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hideRider(
            Entity entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && player.getVehicle() instanceof FloranHenchman) {
            cir.setReturnValue(false);
        }
    }
}
