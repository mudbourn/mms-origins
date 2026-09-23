package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Drops the first-person fire overlay lower on the screen for blazeborn. They
 * burn near-constantly to stoke their fire power, and the full-height flames
 * blind the view; nudging the overlay's own vertical offset down clears the
 * upper screen while leaving the effect for everyone else untouched.
 */
@Mixin(ScreenEffectRenderer.class)
public class FireOverlayLowerMixin {

    private static final float DROP = 0.45f;

    @ModifyArg(
        method = "renderFire",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
        index = 1)
    private static float mmsOrigins$lowerFire(float y) {
        Player player = Minecraft.getInstance().player;
        if (player != null && MmsOriginsPowers.holds(player, MmsOriginsPowers.FIRE_POWER)) {
            return y - DROP;
        }
        return y;
    }
}
