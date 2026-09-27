package info.mudbourn.mmsorigins.client.beastfolk;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmsorigins.client.fur.FurState;
import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

/**
 * Draws a beastfolk's ears, their chosen tail, and, while claw mode is on, their claws.
 *
 * <p>Each piece is posed by moving the pose stack into the player bone it rides (head,
 * body, or arm) before submitting, since the collector runs a model's own setupAnim at
 * draw time and would discard any pose copied into the model's parts. The ears take the
 * chosen tail's coat, or the fox coat when there is no tail.
 */
public class BeastfolkFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier CLAWS_TEXTURE =
            Identifier.fromNamespaceAndPath("mms_origins", "textures/entity/claws.png");
    private static final float PIXEL = 1.0F / 16.0F;

    private final TailModel tail;
    private final EarsModel ears;
    private final ClawsModel claws;

    public BeastfolkFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent,
                                    Function<ModelLayerLocation, ModelPart> baker) {
        super(parent);
        this.tail = new TailModel(baker.apply(TailModel.LAYER));
        this.ears = new EarsModel(baker.apply(EarsModel.LAYER));
        this.claws = new ClawsModel(baker.apply(ClawsModel.LAYER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        FurState fur = (FurState) state;
        if (state.isInvisible || !isBeastfolk(fur.mmsOrigins$furOrigin())) {
            return;
        }
        PlayerModel player = this.getParentModel();
        BeastfolkTail tail = BeastfolkTail.of(fur.mmsOrigins$tailOption());
        Identifier earTexture = (tail == null ? BeastfolkTail.FOX : tail).earTexture();
        submitOn(poseStack, collector, light, state, player.head, 0.0F, this.ears, earTexture);
        if (tail != null) {
            poseStack.pushPose();
            player.body.translateAndRotate(poseStack);
            poseStack.translate(0.0F, 0.65F, 0.1F);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            submit(poseStack, collector, light, state, this.tail, tail.texture());
            poseStack.popPose();
        }
        if (fur.mmsOrigins$clawsOut()) {
            float side = (isSlim(state) ? 2.0F : 3.0F) * PIXEL;
            submitOn(poseStack, collector, light, state, player.rightArm, -side, this.claws, CLAWS_TEXTURE);
            submitOn(poseStack, collector, light, state, player.leftArm, side, this.claws, CLAWS_TEXTURE);
        }
    }

    /** Whether a fur id is one of the beastfolk's collar variants. */
    private static boolean isBeastfolk(Identifier fur) {
        return fur != null && fur.getPath().startsWith("beastfolk");
    }

    /** Whether the player is wearing a three-pixel-arm (slim) skin. */
    private static boolean isSlim(AvatarRenderState state) {
        return state.skin != null && state.skin.model() == PlayerModelType.SLIM;
    }

    /** Submits a model in the pose of a player bone, nudged sideways by the given block offset. */
    private static void submitOn(PoseStack poseStack, SubmitNodeCollector collector, int light,
                                 AvatarRenderState state, ModelPart bone, float xOffset,
                                 Model<AvatarRenderState> model, Identifier texture) {
        poseStack.pushPose();
        bone.translateAndRotate(poseStack);
        poseStack.translate(xOffset, 0.0F, 0.0F);
        submit(poseStack, collector, light, state, model, texture);
        poseStack.popPose();
    }

    private static void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                               AvatarRenderState state, Model<AvatarRenderState> model, Identifier texture) {
        collector.submitModel(model,
                state,
                poseStack,
                RenderTypes.entityCutoutNoCull(texture),
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
    }
}
