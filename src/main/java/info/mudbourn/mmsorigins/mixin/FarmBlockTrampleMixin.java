package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Keeps a leaf-form floran from trampling farmland.
@Mixin(FarmlandBlock.class)
public abstract class FarmBlockTrampleMixin {

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$spareFarmland(
            Level level,
            BlockState state,
            BlockPos pos,
            Entity entity,
            double fallDistance,
            CallbackInfo ci) {
        if (MmsOriginsPowers.leafFormComplete(entity)) {
            ci.cancel();
        }
    }
}
