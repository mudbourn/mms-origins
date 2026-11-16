package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.LushSoil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Gives plants rooted in lush soil a second random tick on every other random tick.
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LushPlantGrowthMixin {

    @Unique
    private static boolean mmsOrigins$bonusRunning;

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void mmsOrigins$lushGrowth(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockState state = (BlockState) (Object) this;
        if (mmsOrigins$bonusRunning || !(state.getBlock() instanceof BushBlock) || !(state.getBlock() instanceof BonemealableBlock)) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.getBlock() == state.getBlock()
                && LushSoil.at(level, pos.below())
                && LushSoil.bonusTick(random)) {
            mmsOrigins$bonusRunning = true;
            try {
                current.randomTick(level, pos, random);
            } finally {
                mmsOrigins$bonusRunning = false;
            }
        }
    }
}
