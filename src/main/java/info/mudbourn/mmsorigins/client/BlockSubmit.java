package info.mudbourn.mmsorigins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** Draws a block state's baked model the way a moving block is drawn, untinted or with one tint. */
public final class BlockSubmit {

    private static final long MODEL_SEED = 42L;
    private static final int[] WHITE = {-1};

    public static void submit(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            BlockState state,
            int light,
            int overlay,
            int outline) {
        submit(collector, poseStack, state, light, overlay, outline, WHITE);
    }

    /** Draws the model with {@code tint} (ARGB) on its tint index 0 faces, like grass in a biome. */
    public static void submitTinted(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            BlockState state,
            int light,
            int overlay,
            int tint) {
        submit(collector, poseStack, state, light, overlay, 0, new int[] {tint});
    }

    private static void submit(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            BlockState state,
            int light,
            int overlay,
            int outline,
            int[] tints) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(MODEL_SEED), parts);
        collector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), parts, tints, light, overlay, outline);
    }

    private BlockSubmit() {
    }
}
