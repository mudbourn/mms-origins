package info.mudbourn.mmsorigins.mixin.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Arms Dealing: a bartering brute holds its gold up and looks it over.
 *
 * <p>A vanilla brute has no admiring pose, so the gold {@code PiglinBruteBarterMixin}
 * hands it just sits flat in the off hand. While that gold is held, the arm pose is
 * swapped to the common piglin's admiring pose, which the shared model already
 * animates, so the brute raises the piece to its face the way a piglin would. The
 * off hand is empty at every other time, so the ordinary poses are left untouched.
 */
@Mixin(PiglinBrute.class)
public class PiglinBruteAdmirePoseMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$admirePose(CallbackInfoReturnable<PiglinArmPose> cir) {
        PiglinBrute brute = (PiglinBrute) (Object) this;
        ItemStack offhand = brute.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.is(Items.GOLD_INGOT) || offhand.is(Items.GOLD_BLOCK)) {
            cir.setReturnValue(PiglinArmPose.ADMIRING_ITEM);
        }
    }
}
