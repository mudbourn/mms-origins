package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.network.WebFirePayload;
import info.mudbourn.mmsorigins.network.WebLinePayload;
import info.mudbourn.mmsorigins.network.WebReelPayload;
import info.mudbourn.mmsorigins.network.WebRopePayload;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side record of every arachnid web line. Lines are sent only when they change: to
 * the owner, to the players tracking the owner, and to anyone who starts tracking the owner
 * later. The owner reports reeling, fresh presses and throttled rope lengths; every client
 * draws the lines itself, so the server sends no particles.
 */
public final class WebReel {

    private static final Set<UUID> REELING = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> FRESH_PRESS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, WebLinePayload> LINES = new ConcurrentHashMap<>();

    private WebReel() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(WebReelPayload.TYPE, WebReelPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WebFirePayload.TYPE, WebFirePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WebRopePayload.TYPE, WebRopePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WebLinePayload.TYPE, WebLinePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WebRopePayload.TYPE, WebRopePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WebReelPayload.TYPE, (payload, context) -> {
            UUID id = context.player().getUUID();
            if (payload.reeling()) {
                REELING.add(id);
            } else {
                REELING.remove(id);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(WebFirePayload.TYPE, (payload, context) ->
            FRESH_PRESS.add(context.player().getUUID()));
        ServerPlayNetworking.registerGlobalReceiver(WebRopePayload.TYPE, (payload, context) -> {
            WebLinePayload line = LINES.get(context.player().getUUID());
            if (line != null && line.entityId() == WebLinePayload.NO_ENTITY) {
                updateRope(context.player(), payload.rope());
            }
        });
        EntityTrackingEvents.START_TRACKING.register((tracked, viewer) -> {
            if (tracked instanceof ServerPlayer owner) {
                WebLinePayload line = LINES.get(owner.getUUID());
                if (line != null) {
                    send(viewer, line.forViewers());
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> detach(oldPlayer));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            detach(player);
            REELING.remove(player.getUUID());
            FRESH_PRESS.remove(player.getUUID());
        });
    }

    /** Whether this player is holding use with an empty hand to reel in. */
    public static boolean isReeling(Player player) {
        return REELING.contains(player.getUUID());
    }

    /** Whether this player pressed its web key afresh since the last check, clearing it. */
    public static boolean consumeFreshPress(Player player) {
        return FRESH_PRESS.remove(player.getUUID());
    }

    /** Records the owner's new web line and sends it to the owner and its trackers. */
    public static void attach(Player player, WebLinePayload line) {
        if (!(player instanceof ServerPlayer owner)) {
            return;
        }
        LINES.put(owner.getUUID(), line);
        send(owner, line);
        broadcast(owner, line.forViewers());
    }

    /** Drops the owner's web line, telling the owner and its trackers, if it had one. */
    public static void detach(Player player) {
        if (!(player instanceof ServerPlayer owner) || LINES.remove(owner.getUUID()) == null) {
            return;
        }
        WebLinePayload detached = WebLinePayload.detached(owner.getId());
        send(owner, detached);
        broadcast(owner, detached);
    }

    /**
     * Records a new rope length for the owner's line and relays it. A creature line's rope
     * is run here, so the owner hears it too; a block line's owner already has it.
     */
    public static void updateRope(Player player, float rope) {
        if (!(player instanceof ServerPlayer owner) || !Float.isFinite(rope)) {
            return;
        }
        WebLinePayload line = LINES.get(owner.getUUID());
        if (line == null) {
            return;
        }
        float clamped = Math.max(0.0F, Math.min(rope, line.maxRope()));
        LINES.put(owner.getUUID(), line.withRope(clamped));
        WebRopePayload payload = new WebRopePayload(owner.getId(), clamped);
        broadcast(owner, payload);
        if (line.entityId() != WebLinePayload.NO_ENTITY) {
            send(owner, payload);
        }
    }

    private static void broadcast(ServerPlayer owner, CustomPacketPayload payload) {
        for (ServerPlayer viewer : PlayerLookup.tracking(owner)) {
            if (viewer != owner) {
                send(viewer, payload);
            }
        }
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
