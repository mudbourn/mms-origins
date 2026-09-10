package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Floran growth aura, driven by the {@code mms_origins:verdant_growth} marker.
 *
 * <p>Plant growth in vanilla is a random-tick phenomenon, so this hastens it by
 * adding extra random ticks near each floran rather than by touching any single
 * plant directly. Each world tick, for every chunk within {@link #CHUNK_RADIUS}
 * of a bearer, it fires a few more random ticks per section at the same rate
 * vanilla uses, scaled by {@link #GROWTH_BONUS}, and acts only on plant blocks
 * so leaf decay and ice melt are left alone. The extra ticks are a fraction of
 * what vanilla already spends on the same chunks, so the cost stays modest.
 */
public final class VerdantGrowth {

    private static final Identifier MARKER =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "verdant_growth");
    private static final int CHUNK_RADIUS = 2;
    private static final float GROWTH_BONUS = 0.3f;

    private VerdantGrowth() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(VerdantGrowth::onEndWorldTick);
    }

    private static void onEndWorldTick(ServerLevel level) {
        int randomTickSpeed = level.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
        if (randomTickSpeed <= 0) {
            return;
        }
        int extraPerSection = Math.max(1, Math.round(randomTickSpeed * GROWTH_BONUS));
        for (ServerPlayer player : level.players()) {
            if (!hasVerdantGrowth(player)) {
                continue;
            }
            int centerX = player.chunkPosition().x;
            int centerZ = player.chunkPosition().z;
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                    hastenChunk(level, centerX + dx, centerZ + dz, extraPerSection);
                }
            }
        }
    }

    private static void hastenChunk(ServerLevel level, int chunkX, int chunkZ, int extraPerSection) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        RandomSource random = level.getRandom();
        int blockX = SectionPos.sectionToBlockCoord(chunkX);
        int blockZ = SectionPos.sectionToBlockCoord(chunkZ);
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (!section.isRandomlyTicking()) {
                continue;
            }
            int blockY = SectionPos.sectionToBlockCoord(level.getSectionYFromSectionIndex(i));
            for (int n = 0; n < extraPerSection; n++) {
                BlockPos pos = level.getBlockRandomPos(blockX, blockY, blockZ, 15);
                BlockState state = chunk.getBlockState(pos);
                if (state.isRandomlyTicking() && isPlant(state)) {
                    state.randomTick(level, pos, random);
                }
            }
        }
    }

    private static boolean isPlant(BlockState state) {
        return state.getBlock() instanceof BonemealableBlock
            || state.getBlock() instanceof BushBlock
            || state.getBlock() instanceof GrowingPlantBlock
            || state.getBlock() instanceof SugarCaneBlock
            || state.getBlock() instanceof CactusBlock
            || state.getBlock() instanceof BambooStalkBlock;
    }

    private static boolean hasVerdantGrowth(ServerPlayer player) {
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return false;
        }
        for (PowerType<?> type : component.getPowerTypes(true)) {
            if (MARKER.equals(type.getIdentifier())) {
                return true;
            }
        }
        return false;
    }
}
