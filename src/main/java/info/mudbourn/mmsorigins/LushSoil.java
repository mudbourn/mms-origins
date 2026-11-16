package info.mudbourn.mmsorigins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Lush soil: plants rooted in it and animals standing on it grow one and a half
 * times as fast, by taking one extra growth step on every second opportunity.
 */
public final class LushSoil {

    public static final TagKey<Block> TAG =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "lush_soil"));

    private static final int BONUS_PERIOD = 2;

    /** Whether the block at the position is lush soil. */
    public static boolean at(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(TAG);
    }

    /** Whether a plant's random tick earns an extra growth step, by chance. */
    public static boolean bonusTick(RandomSource random) {
        return random.nextInt(BONUS_PERIOD) == 0;
    }

    /** Whether an animal's tick earns an extra growth step, on alternate ticks. */
    public static boolean bonusTick(int tickCount) {
        return tickCount % BONUS_PERIOD == 0;
    }

    private LushSoil() {
    }
}
