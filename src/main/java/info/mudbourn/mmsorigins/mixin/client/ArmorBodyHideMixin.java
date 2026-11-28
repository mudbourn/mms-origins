package info.mudbourn.mmsorigins.mixin.client;

import java.util.ArrayList;
import java.util.List;
import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.fur.FurState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Leaves armor off the body parts a fur replaces, such as a blazeborn's arms, so the fur's own parts show instead of armor floating over them.
@Mixin(HumanoidModel.class)
public abstract class ArmorBodyHideMixin {

    // Parts this model hid for the last fur, shown again before the next entity is drawn with it.
    @Unique
    private final List<ModelPart> mmsOrigins$hidden = new ArrayList<>();

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void mmsOrigins$hideArmorOnReplacedParts(HumanoidRenderState state, CallbackInfo ci) {
        if ((Object) this instanceof PlayerModel) {
            return;
        }
        this.mmsOrigins$hidden.forEach(part -> part.visible = true);
        this.mmsOrigins$hidden.clear();
        if (!(state instanceof FurState furState)) {
            return;
        }
        FurModels.Fur fur = FurModels.resolve(furState.mmsOrigins$furOrigin());
        if (fur == null) {
            return;
        }
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        for (String name : fur.hidden()) {
            ModelPart part = mmsOrigins$partFor(model, name);
            if (part != null && part.visible) {
                part.visible = false;
                this.mmsOrigins$hidden.add(part);
            }
        }
    }

    @Unique
    private static ModelPart mmsOrigins$partFor(HumanoidModel<?> model, String name) {
        return switch (name) {
            case "head" -> model.head;
            case "body" -> model.body;
            case "leftArm" -> model.leftArm;
            case "rightArm" -> model.rightArm;
            case "leftLeg" -> model.leftLeg;
            case "rightLeg" -> model.rightLeg;
            default -> null;
        };
    }
}
