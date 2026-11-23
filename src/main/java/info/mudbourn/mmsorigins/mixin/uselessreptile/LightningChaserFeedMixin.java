package info.mudbourn.mmsorigins.mixin.uselessreptile;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A wild Lightning Chaser cannot be tamed by feeding it; it has to be killed (LightningChaserTamingMixin).
@Pseudo
@Mixin(targets = "nordmods.uselessreptile.common.entity.LightningChaser", remap = false)
public abstract class LightningChaserFeedMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$noFeedTaming(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        TamableAnimal dragon = (TamableAnimal) (Object) this;
        if (dragon.isTame() || player.isCreative() || player.getItemInHand(hand).isEmpty()) {
            return;
        }
        if (!player.level().isClientSide()) {
            player.sendOverlayMessage(Component.literal("A Lightning Chaser only yields to the one who defeats it."));
        }
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
