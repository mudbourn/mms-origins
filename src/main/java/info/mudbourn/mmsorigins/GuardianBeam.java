package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.network.GuardianBeamPayload;
import info.mudbourn.mmsorigins.power.action.ShulkerBulletAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Server side of the fishman guardian beam: target lock, charge and release.
 *
 * <p>Each held tick of the beam power calls {@link #channel}. The first call locks
 * the closest creature in the caster's sight, like a guardian picking its prey, and
 * the beam then charges on that creature while it stays alive, in range and in
 * sight. A full charge strikes it once; letting go, losing the target or breaking
 * sight drops the beam. Clients are told when a beam starts and stops so they can
 * draw it with the guardian's own renderer.
 */
public final class GuardianBeam {

    private static final int RELEASE_GRACE_TICKS = 2;
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    private GuardianBeam() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(GuardianBeamPayload.TYPE, GuardianBeamPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(GuardianBeam::releaseLetGo);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stop(handler.getPlayer()));
    }

    /** Advances the caster's beam by one held tick, running {@code onFire} when it strikes. */
    public static void channel(LivingEntity caster, double range, double coneDegrees, int chargeTicks,
                               float damage, Consumer<Entity> onFire) {
        if (!(caster instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Channel channel = CHANNELS.get(player.getUUID());
        if (channel != null && !holds(player, level, channel, range)) {
            stop(player);
            channel = null;
        }
        if (channel == null) {
            LivingEntity target = ShulkerBulletAction.findTarget(player, level, range, coneDegrees);
            if (target == null) {
                return;
            }
            channel = new Channel(target.getId());
            CHANNELS.put(player.getUUID(), channel);
            broadcast(player, new GuardianBeamPayload(player.getId(), target.getId(), chargeTicks));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        channel.lastHeld = level.getServer().getTickCount();
        if (++channel.ticks < chargeTicks) {
            return;
        }
        if (level.getEntity(channel.targetId) instanceof LivingEntity target) {
            target.hurtServer(level, level.damageSources().indirectMagic(player, player), damage);
        }
        if (onFire != null) {
            onFire.accept(player);
        }
        stop(player);
    }

    private static boolean holds(ServerPlayer player, ServerLevel level, Channel channel, double range) {
        return level.getEntity(channel.targetId) instanceof LivingEntity target
            && ShulkerBulletAction.isCandidate(player, target)
            && target.distanceTo(player) <= range;
    }

    private static void releaseLetGo(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Channel>> channels = CHANNELS.entrySet().iterator();
        while (channels.hasNext()) {
            Map.Entry<UUID, Channel> entry = channels.next();
            if (server.getTickCount() - entry.getValue().lastHeld <= RELEASE_GRACE_TICKS) {
                continue;
            }
            channels.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                broadcast(player, new GuardianBeamPayload(player.getId(), GuardianBeamPayload.NONE, 0));
            }
        }
    }

    private static void stop(ServerPlayer player) {
        if (CHANNELS.remove(player.getUUID()) != null) {
            broadcast(player, new GuardianBeamPayload(player.getId(), GuardianBeamPayload.NONE, 0));
        }
    }

    private static void broadcast(ServerPlayer player, GuardianBeamPayload payload) {
        ServerPlayNetworking.send(player, payload);
        for (ServerPlayer watcher : PlayerLookup.tracking(player)) {
            if (watcher != player) {
                ServerPlayNetworking.send(watcher, payload);
            }
        }
    }

    private static final class Channel {

        private final int targetId;
        private int ticks;
        private int lastHeld;

        private Channel(int targetId) {
            this.targetId = targetId;
        }
    }
}
