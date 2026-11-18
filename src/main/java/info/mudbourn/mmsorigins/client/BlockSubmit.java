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

/** Draws a block state's baked model untinted, the way a moving block is drawn. */
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
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(MODEL_SEED), parts);
        collector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), parts, WHITE, light, overlay, outline);
    }

    private BlockSubmit() {
    }
}
