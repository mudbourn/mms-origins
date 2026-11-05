package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.LeafFormState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves a buried leaf-form floran's shadow up onto the surface above them so other players can spot them.
 */
@Mixin(EntityRenderer.class)
public abstract class LeafFormShadowMixin {

    @Unique
    private static final TagKey<Block> LEAF_PASSABLE =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mms_origins", "leaf_passable"));

    @Unique
    private static final int MAX_DEPTH = 16;

    @Unique
    private static final float ALPHA = 0.5F;

    @Inject(method = "finalizeRenderState", at = @At("RETURN"))
    private void mmsOrigins$surfaceShadow(Entity entity, EntityRenderState state, CallbackInfo ci) {
        if (!(state instanceof LeafFormState leaf) || !leaf.mmsOrigins$leafForm() || state.shadowRadius <= 0) {
            return;
        }
        Level level = entity.level();
        BlockPos.MutableBlockPos pos = BlockPos.containing(state.x, state.y + 0.1, state.z).mutable();
        if (!level.getBlockState(pos).is(LEAF_PASSABLE)) {
            return;
        }
        int depth = 0;
        while (depth < MAX_DEPTH && level.getBlockState(pos.above()).is(LEAF_PASSABLE)) {
            pos.move(0, 1, 0);
            depth++;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
            return;
        }
        state.shadowPieces.clear();
        int minX = (int) Math.floor(state.x - state.shadowRadius);
        int maxX = (int) Math.floor(state.x + state.shadowRadius);
        int minZ = (int) Math.floor(state.z - state.shadowRadius);
        int maxZ = (int) Math.floor(state.z + state.shadowRadius);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos ground = new BlockPos(x, pos.getY(), z);
                BlockState groundState = level.getBlockState(ground);
                if (!groundState.isCollisionShapeFullBlock(level, ground)) {
                    continue;
                }
                state.shadowPieces.add(new EntityRenderState.ShadowPiece(
                    (float) (x - state.x),
                    (float) (pos.getY() + 1 - state.y),
                    (float) (z - state.z),
                    groundState.getShape(level, ground),
                    ALPHA));
            }
        }
    }
}
