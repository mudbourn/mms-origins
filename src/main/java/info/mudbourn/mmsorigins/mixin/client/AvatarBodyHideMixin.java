package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.fur.FurState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the vanilla player parts a fur replaces.
 *
 * <p>The hide rides {@code setupAnim}, which is where the player model re-establishes
 * every part's visibility from the render state before both the body and its layers are
 * drawn. Hiding here, after vanilla has had its say, keeps a replaced arm or torso from
 * showing through or fighting the fur that stands in for it. No restore is needed: the
 * next entity's {@code setupAnim} sets visibility afresh, so the shared model never
 * carries a hidden part over to a player that did not ask for it.
 */
@Mixin(PlayerModel.class)
public abstract class AvatarBodyHideMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("RETURN"))
    private void mmsOrigins$hideReplacedParts(AvatarRenderState state, CallbackInfo ci) {
        if (!(state instanceof FurState furState)) {
            return;
        }
        FurModels.Fur fur = FurModels.resolve(furState.mmsOrigins$furOrigin());
        if (fur == null || fur.hidden().isEmpty()) {
            return;
        }
        PlayerModel model = (PlayerModel) (Object) this;
        for (String name : fur.hidden()) {
            ModelPart part = mmsOrigins$partFor(model, name);
            if (part != null) {
                part.visible = false;
            }
        }
    }

    @Unique
    private static ModelPart mmsOrigins$partFor(PlayerModel model, String name) {
        return switch (name) {
            case "head" -> model.head;
            case "hat" -> model.hat;
            case "body" -> model.body;
            case "leftArm" -> model.leftArm;
            case "rightArm" -> model.rightArm;
            case "leftLeg" -> model.leftLeg;
            case "rightLeg" -> model.rightLeg;
            case "leftSleeve" -> model.leftSleeve;
            case "rightSleeve" -> model.rightSleeve;
            case "leftPants" -> model.leftPants;
            case "rightPants" -> model.rightPants;
            case "jacket" -> model.jacket;
            default -> null;
        };
    }
}
