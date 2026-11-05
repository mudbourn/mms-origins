package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.network.GuardianBeamPayload;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Client record of every fishman beam currently charging, fed by {@link GuardianBeamPayload}.
 *
 * <p>Beams are drawn from the target back to the caster's eyes, so a caster in first
 * person, whose own body is never rendered, still sees their beam.
 */
public final class GuardianBeamClient {

    private static final Map<Integer, Beam> BEAMS = new HashMap<>();

    private GuardianBeamClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(GuardianBeamPayload.TYPE, (payload, context) -> {
            if (payload.targetId() == GuardianBeamPayload.NONE) {
                BEAMS.remove(payload.casterId());
                return;
            }
            ClientLevel level = context.client().level;
            long start = level == null ? 0L : level.getGameTime();
            BEAMS.put(payload.casterId(), new Beam(payload.targetId(), payload.chargeTicks(), start));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BEAMS.clear());
    }

    /** Stamps the beam aimed at this entity onto its render state, or clears it when none is. */
    public static void stamp(LivingEntity entity, GuardianBeamState state, float partialTick) {
        state.mmsOrigins$setBeamVector(null);
        for (Map.Entry<Integer, Beam> entry : BEAMS.entrySet()) {
            Beam beam = entry.getValue();
            if (beam.targetId != entity.getId()
                    || !(entity.level().getEntity(entry.getKey()) instanceof Player caster)) {
                continue;
            }
            float height = entity.getBbHeight() * 0.5F;
            Vec3 origin = entity.getPosition(partialTick).add(0.0, height, 0.0);
            float attackTime = (entity.level().getGameTime() - beam.startTick) + partialTick;
            state.mmsOrigins$setBeamVector(caster.getEyePosition(partialTick).subtract(origin));
            state.mmsOrigins$setBeamHeight(height);
            state.mmsOrigins$setBeamTime(attackTime);
            state.mmsOrigins$setBeamScale(Math.min(attackTime / beam.chargeTicks, 1.0F));
            return;
        }
    }

    private record Beam(int targetId, int chargeTicks, long startTick) {
    }
}
