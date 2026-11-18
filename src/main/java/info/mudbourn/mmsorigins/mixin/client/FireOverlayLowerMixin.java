package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops the first-person fire overlay lower on the screen for blazeborn. They
 * burn near-constantly to stoke their fire power, and the full-height flames
 * blind the view; nudging the overlay's own vertical offset down clears the
 * upper screen while leaving the effect for everyone else untouched.
 */
@Mixin(ScreenEffectRenderer.class)
public class FireOverlayLowerMixin {

    private static final float DROP = 0.45f;

    @Inject(method = "submitFire", at = @At("HEAD"))
    private static void mmsOrigins$lowerFire(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            TextureAtlasSprite sprite,
            CallbackInfo ci) {
        poseStack.pushPose();
        if (mmsOrigins$burnsLow()) {
            poseStack.translate(0.0f, -DROP, 0.0f);
        }
    }

    @Inject(method = "submitFire", at = @At("RETURN"))
    private static void mmsOrigins$restoreFire(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            TextureAtlasSprite sprite,
            CallbackInfo ci) {
        poseStack.popPose();
    }

    @Unique
    private static boolean mmsOrigins$burnsLow() {
        Player player = Minecraft.getInstance().player;
        return player != null && MmsOriginsPowers.holds(player, MmsOriginsPowers.FIRE_POWER);
    }
}
