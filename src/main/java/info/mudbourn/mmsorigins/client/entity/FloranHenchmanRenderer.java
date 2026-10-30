package info.mudbourn.mmsorigins.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Draws the Grovekeeper as a jungle creaking: the creaking model with softened
 * animations under the jungle bark and leaf overlay, eyes that glow while it
 * has a target, and any flower it is offering held in its long right arm.
 */
public class FloranHenchmanRenderer
        extends MobRenderer<FloranHenchman, CreakingRenderState, CreakingModel> {

    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/entity/grovekeeper.png");
    private static final Identifier LEAVES_TEXTURE =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/entity/grovekeeper_layer.png");
    private static final Identifier EYES_TEXTURE =
        Identifier.withDefaultNamespace("textures/entity/creaking/creaking_eyes.png");

    private final ItemModelResolver itemModelResolver;

    public FloranHenchmanRenderer(EntityRendererProvider.Context context) {
        super(context, new GrovekeeperModel(context.bakeLayer(ModelLayers.CREAKING)), 0.6f);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new LeavesLayer(this));
        this.addLayer(new LivingEntityEmissiveLayer<>(
            this,
            state -> EYES_TEXTURE,
            (state, ageInTicks) -> state.eyesGlowing ? 1.0f : 0.0f,
            new CreakingModel(context.bakeLayer(ModelLayers.CREAKING_EYES)),
            RenderTypes::eyes,
            true));
        this.addLayer(new FlowerLayer(this));
    }

    @Override
    public Identifier getTextureLocation(CreakingRenderState state) {
        return TEXTURE;
    }

    @Override
    public CreakingRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FloranHenchman henchman, CreakingRenderState state, float partialTick) {
        super.extractRenderState(henchman, state, partialTick);
        state.attackAnimationState.copyFrom(henchman.attackAnimationState);
        state.canMove = henchman.canMove();
        state.eyesGlowing = henchman.getTarget() != null || !henchman.canMove();
        this.itemModelResolver.updateForLiving(
            ((State) state).flower,
            henchman.getOfferedFlower(),
            ItemDisplayContext.GROUND,
            henchman);
    }

    public static class State extends CreakingRenderState {

        public final ItemStackRenderState flower = new ItemStackRenderState();
    }

    private static final class LeavesLayer extends RenderLayer<CreakingRenderState, CreakingModel> {

        private LeavesLayer(RenderLayerParent<CreakingRenderState, CreakingModel> parent) {
            super(parent);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                int light,
                CreakingRenderState state,
                float yRot,
                float xRot) {
            if (!state.isInvisible) {
                renderColoredCutoutModel(
                    this.getParentModel(),
                    LEAVES_TEXTURE,
                    poseStack,
                    collector,
                    light,
                    state,
                    -1,
                    1);
            }
        }
    }

    private static final class FlowerLayer extends RenderLayer<CreakingRenderState, CreakingModel> {

        private FlowerLayer(RenderLayerParent<CreakingRenderState, CreakingModel> parent) {
            super(parent);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                int light,
                CreakingRenderState state,
                float yRot,
                float xRot) {
            ItemStackRenderState flower = ((State) state).flower;
            if (flower.isEmpty()) {
                return;
            }
            poseStack.pushPose();
            this.getParentModel().root().getChild("root").translateAndRotate(poseStack);
            this.getParentModel().root().getChild("root").getChild("upper_body").translateAndRotate(poseStack);
            this.getParentModel().root().getChild("root").getChild("upper_body").getChild("right_arm")
                .translateAndRotate(poseStack);
            poseStack.translate(-0.03f, 1.4f, -0.15f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            flower.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
