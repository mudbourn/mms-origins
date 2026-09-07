package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.fur.FurState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Hides the vanilla player parts a fur replaces while the base body is submitted.
 *
 * <p>A fur that stands in for the arms or torso lists those parts, and this suppresses
 * them for the duration of the body submit so the fur is not drawn over a visible
 * vanilla limb. Only parts that were visible are re-shown on return, so a part already
 * hidden for another reason stays hidden.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class AvatarBodyHideMixin {

    @Unique
    private final List<ModelPart> mmsOrigins$suppressed = new ArrayList<>();

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"))
    private void mmsOrigins$hideReplacedParts(LivingEntityRenderState state, CallbackInfo ci) {
        this.mmsOrigins$suppressed.clear();
        if (!(state instanceof AvatarRenderState avatar)
                || !(((LivingEntityRenderer<?, ?, ?>) (Object) this).getModel() instanceof PlayerModel model)) {
            return;
        }
        FurModels.Fur fur = FurModels.resolve(((FurState) state).mmsOrigins$furOrigin());
        if (fur == null || fur.hidden().isEmpty()) {
            return;
        }
        for (String name : fur.hidden()) {
            ModelPart part = mmsOrigins$partFor(model, name);
            if (part != null && part.visible) {
                part.visible = false;
                this.mmsOrigins$suppressed.add(part);
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("RETURN"))
    private void mmsOrigins$restoreReplacedParts(LivingEntityRenderState state, CallbackInfo ci) {
        for (ModelPart part : this.mmsOrigins$suppressed) {
            part.visible = true;
        }
        this.mmsOrigins$suppressed.clear();
    }

    @Unique
    private static ModelPart mmsOrigins$partFor(PlayerModel model, String name) {
        HumanoidModel<?> biped = model;
        return switch (name) {
            case "head" -> biped.head;
            case "hat" -> biped.hat;
            case "body" -> biped.body;
            case "leftArm" -> biped.leftArm;
            case "rightArm" -> biped.rightArm;
            case "leftLeg" -> biped.leftLeg;
            case "rightLeg" -> biped.rightLeg;
            case "leftSleeve" -> model.leftSleeve;
            case "rightSleeve" -> model.rightSleeve;
            case "leftPants" -> model.leftPants;
            case "rightPants" -> model.rightPants;
            case "jacket" -> model.jacket;
            default -> null;
        };
    }
}
