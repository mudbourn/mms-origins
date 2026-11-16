package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmsorigins.client.entity.GrovekeeperArm;
import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Replaces the first-person hands with the Grovekeeper's arm while the player rides it.
@Mixin(ItemInHandRenderer.class)
public abstract class GrovekeeperRiderHandMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hideHands(
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            LocalPlayer player,
            int light,
            CallbackInfo ci) {
        if (player.getVehicle() instanceof FloranHenchman) {
            GrovekeeperArm.submit(poseStack, collector, light, player.getAttackAnim(partialTick));
            ci.cancel();
        }
    }
}
