package info.mudbourn.mmsorigins.entity;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Ties a floran's henchmen to the summoner's presence and paces their return.
 *
 * <p>The whole pack vanishes and the charge bar refills the moment the summoner
 * dies or leaves the game, so a fresh spawn never inherits a stranded pack. A
 * henchman killed in the field instead refunds its charge only after a delay, so
 * a summoner cannot replace one the instant it falls.
 */
public final class HenchmanLifecycle {

    private static final int DEATH_REFUND_DELAY = 600;
    private static final List<PendingRefund> PENDING = new ArrayList<>();

    private HenchmanLifecycle() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                FloranHenchman.despawnOwnedBy(player.level().getServer(), player.getUUID());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            FloranHenchman.despawnOwnedBy(server, handler.getPlayer().getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(HenchmanLifecycle::tick);
    }

    public static void scheduleRefund(MinecraftServer server, UUID ownerUuid) {
        if (server != null && ownerUuid != null) {
            PENDING.add(new PendingRefund(ownerUuid, DEATH_REFUND_DELAY));
        }
    }

    private static void tick(MinecraftServer server) {
        Iterator<PendingRefund> refunds = PENDING.iterator();
        while (refunds.hasNext()) {
            PendingRefund refund = refunds.next();
            if (--refund.ticks > 0) {
                continue;
            }
            refunds.remove();
            ServerPlayer owner = server.getPlayerList().getPlayer(refund.owner);
            VariableIntPower charge = FloranHenchman.chargeOf(owner, null);
            if (charge != null && charge.getValue() < charge.getMax()) {
                charge.increment();
                PowerHolderComponent.syncPower(owner, charge.getType());
            }
        }
    }

    private static final class PendingRefund {

        private final UUID owner;
        private int ticks;

        private PendingRefund(UUID owner, int ticks) {
            this.owner = owner;
            this.ticks = ticks;
        }
    }
}
