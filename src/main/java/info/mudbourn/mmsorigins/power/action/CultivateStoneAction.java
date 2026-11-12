package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Grows the stone or deepslate the caster is looking at into shroomlight once
 * the key has been held on that block for {@code charge_ticks}.
 *
 * <p>The power fires this every tick while its continuous key is held. A gap of
 * more than a few ticks, or a change of block, restarts the charge, and a finished
 * conversion waits for the key to be released before charging again.
 * {@code finish_action} runs on the caster when the block converts.
 */
public final class CultivateStoneAction {

    private static final long RELEASE_GAP_TICKS = 4;
    private static final int PARTICLE_INTERVAL = 4;
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CultivateStoneAction::pruneReleased);
    }

    public static SerializableData data() {
        return new SerializableData()
            .add("charge_ticks", SerializableDataTypes.INT, 20)
            .add("reach", SerializableDataTypes.DOUBLE, 5.0)
            .add("finish_action", ApoliDataTypes.ENTITY_ACTION, null);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        Channel channel = CHANNELS.computeIfAbsent(player.getUUID(), uuid -> new Channel());
        boolean held = now - channel.lastTick <= RELEASE_GAP_TICKS;
        channel.lastTick = now;
        if (!held) {
            channel.reset();
        }
        BlockPos target = lookedAtStone(player, level, data.getDouble("reach"));
        if (target == null) {
            channel.reset();
            return;
        }
        if (channel.spent) {
            return;
        }
        if (!target.equals(channel.pos)) {
            channel.reset();
            channel.pos = target;
        }
        if (channel.ticks == 0) {
            level.playSound(
                null,
                target,
                SoundEvents.COMPOSTER_FILL,
                SoundSource.PLAYERS,
                1.0f,
                0.8f
            );
        }
        channel.ticks++;
        if (channel.ticks % PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                target.getX() + 0.5,
                target.getY() + 0.5,
                target.getZ() + 0.5,
                6,
                0.5,
                0.5,
                0.5,
                0.0
            );
        }
        if (channel.ticks >= data.getInt("charge_ticks")) {
            channel.spent = true;
            level.setBlock(target, Blocks.SHROOMLIGHT.defaultBlockState(), 3);
            Consumer<Entity> finish = data.get("finish_action");
            if (finish != null) {
                finish.accept(player);
            }
        }
    }

    private static BlockPos lookedAtStone(ServerPlayer player, ServerLevel level, double reach) {
        HitResult hit = player.pick(reach, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.STONE) && !state.is(Blocks.DEEPSLATE)) {
            return null;
        }
        return player.mayInteract(level, pos) ? pos : null;
    }

    private static void pruneReleased(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<Channel> channels = CHANNELS.values().iterator();
        while (channels.hasNext()) {
            if (now - channels.next().lastTick > RELEASE_GAP_TICKS) {
                channels.remove();
            }
        }
    }

    private static final class Channel {

        private long lastTick = Long.MIN_VALUE;
        private BlockPos pos;
        private int ticks;
        private boolean spent;

        private void reset() {
            this.pos = null;
            this.ticks = 0;
            this.spent = false;
        }
    }

    private CultivateStoneAction() {
    }
}
