package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmsorigins.client.entity.GrovekeeperArm;
import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Replaces the first-person hands with the Grovekeeper's arm while the player rides it.
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class GrovekeeperRiderHandMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hideHands(
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState handsState,
            CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player != null && player.getVehicle() instanceof FloranHenchman) {
            int light = client.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
            GrovekeeperArm.submit(poseStack, collector, light, player.getSwingAnimation(partialTick));
            ci.cancel();
        }
    }
}
