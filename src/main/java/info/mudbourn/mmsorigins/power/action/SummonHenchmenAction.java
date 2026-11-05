package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import info.mudbourn.mmsorigins.entity.MmsEntities;
import info.mudbourn.mmsorigins.power.condition.SunIntakeCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Summons a single Grovekeeper once the caster has held the key for
 * {@code charge_ticks}, letting them ration their pack one summon at a time.
 *
 * <p>The power fires this every tick while its continuous key is held. A gap of
 * more than a few ticks means the key was let go, which restarts the charge, and a
 * finished summon waits for the key to be released before charging again. The
 * evoker's summoning chant plays as the charge begins and is cut off if the
 * key is let go before the summon. Each summon fills one empty
 * slot up to the cap and spends {@link FloranHenchman#CHARGE_COST} sun power,
 * which the Grovekeeper refunds when it leaves.
 */
public final class SummonHenchmenAction {

    private static final double SEARCH_RADIUS = 96.0;
    private static final double RING_RADIUS = 1.5;
    private static final long RELEASE_GAP_TICKS = 4;
    private static final double HEARING_RANGE = 32.0;
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SummonHenchmenAction::cancelReleasedCharges);
    }

    public static SerializableData data() {
        return new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE, null)
            .add("max_active", SerializableDataTypes.INT, 2)
            .add("maels_blessing_max_active", SerializableDataTypes.INT, 4)
            .add("charge_ticks", SerializableDataTypes.INT, 30);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity owner) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        Channel channel = CHANNELS.computeIfAbsent(owner.getUUID(), uuid -> new Channel());
        boolean held = now - channel.lastTick <= RELEASE_GAP_TICKS;
        channel.lastTick = now;
        if (!held) {
            channel.ticks = 0;
            channel.spent = false;
        }
        if (channel.spent || !canSummon(data, owner, level)) {
            return;
        }
        if (channel.ticks == 0) {
            level.playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                SoundEvents.EVOKER_PREPARE_SUMMON,
                SoundSource.PLAYERS,
                1.0f,
                1.0f);
        }
        channel.ticks++;
        if (channel.ticks >= data.getInt("charge_ticks")) {
            channel.spent = true;
            summon(data, owner, level);
        }
    }

    private static void cancelReleasedCharges(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, Channel>> channels = CHANNELS.entrySet().iterator();
        while (channels.hasNext()) {
            Map.Entry<UUID, Channel> entry = channels.next();
            Channel channel = entry.getValue();
            if (now - channel.lastTick <= RELEASE_GAP_TICKS) {
                continue;
            }
            ServerPlayer owner = server.getPlayerList().getPlayer(entry.getKey());
            if (owner != null && channel.ticks > 0 && !channel.spent) {
                stopChargeSound(owner);
            }
            channels.remove();
        }
    }

    private static void stopChargeSound(ServerPlayer owner) {
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
            SoundEvents.EVOKER_PREPARE_SUMMON.location(),
            SoundSource.PLAYERS);
        for (ServerPlayer listener : owner.level().players()) {
            if (listener.distanceToSqr(owner) < HEARING_RANGE * HEARING_RANGE) {
                listener.connection.send(packet);
            }
        }
    }

    private static boolean canSummon(SerializableData.Instance data, LivingEntity owner, ServerLevel level) {
        AABB search = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<FloranHenchman> existing =
            level.getEntities(MmsEntities.FLORAN_HENCHMAN, search, henchman -> owner.equals(henchman.getOwner()));
        int cap = SunIntakeCondition.maelsBlessing(owner) ? data.getInt("maels_blessing_max_active") : data.getInt("max_active");
        if (existing.size() >= cap) {
            return false;
        }
        VariableIntPower charge = FloranHenchman.chargeOf(owner, data.<PowerType<?>>get("resource"));
        return charge == null || charge.getValue() >= FloranHenchman.CHARGE_COST;
    }

    private static void summon(SerializableData.Instance data, LivingEntity owner, ServerLevel level) {
        RandomSource random = owner.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double x = owner.getX() + Math.cos(angle) * RING_RADIUS;
        double z = owner.getZ() + Math.sin(angle) * RING_RADIUS;
        FloranHenchman henchman = new FloranHenchman(MmsEntities.FLORAN_HENCHMAN, level);
        henchman.snapTo(x, owner.getY(), z, owner.getYRot(), 0.0f);
        henchman.setOwner(owner);
        if (SunIntakeCondition.maelsBlessing(owner)) {
            henchman.empower();
        }
        level.addFreshEntity(henchman);
        henchman.playSound(SoundEvents.CREAKING_SPAWN, 1.0f, 1.0f);
        VariableIntPower charge = FloranHenchman.chargeOf(owner, data.<PowerType<?>>get("resource"));
        if (charge != null) {
            charge.setValue(charge.getValue() - FloranHenchman.CHARGE_COST);
            PowerHolderComponent.syncPower(owner, charge.getType());
        }
    }

    private static final class Channel {

        private long lastTick = Long.MIN_VALUE;
        private int ticks;
        private boolean spent;
    }

    private SummonHenchmenAction() {
    }
}
