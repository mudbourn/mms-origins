package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.network.GuardianBeamPayload;
import info.mudbourn.mmsorigins.power.action.ShulkerBulletAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Server side of the fishman guardian beam: target lock, charge and release.
 *
 * <p>Each held tick of the beam power calls {@link #channel}. The first call locks
 * the closest creature in the caster's sight, like a guardian picking its prey, and
 * the beam then charges on that creature while it stays alive, in range and in
 * sight. A full charge strikes it once; letting go, losing the target or breaking
 * sight drops the beam. Half a second later the strike arcs once, weaker, to enemies that were close to the target. Clients are told when a beam starts and stops so they can
 * draw it with the guardian's own renderer.
 */
public final class GuardianBeam {

    /** Beam damage type, tagged to bypass armor and enchantments. */
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "guardian_beam"));

    private static final int RELEASE_GRACE_TICKS = 2;
    private static final double CHAIN_RADIUS = 3.0;
    private static final double CHAIN_PARTICLES_PER_BLOCK = 3.0;
    private static final int CHAIN_DELAY_TICKS = 10;
    private static final float CHAIN_DAMAGE_FACTOR = 0.75F;
    private static final List<PendingChain> CHAINS = new ArrayList<>();
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    private GuardianBeam() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(GuardianBeamPayload.TYPE, GuardianBeamPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(GuardianBeam::releaseLetGo);
        ServerTickEvents.END_SERVER_TICK.register(GuardianBeam::fireChains);
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
            DamageSource source = new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE_TYPE),
                player,
                player);
            target.hurtServer(level, source, damage);
            chain(player, level, target, source, damage);
        }
        if (onFire != null) {
            onFire.accept(player);
        }
        stop(player);
    }

    /** Picks the enemies within {@link #CHAIN_RADIUS} of the target that the strike arcs to after {@link #CHAIN_DELAY_TICKS}. */
    private static void chain(ServerPlayer player, ServerLevel level, LivingEntity target, DamageSource source, float damage) {
        AABB area = target.getBoundingBox().inflate(CHAIN_RADIUS);
        List<Integer> victims = level.getEntitiesOfClass(LivingEntity.class, area, other -> isChainVictim(player, target, other))
            .stream()
            .map(Entity::getId)
            .toList();
        if (victims.isEmpty()) {
            return;
        }
        CHAINS.add(new PendingChain(
            level,
            target.getId(),
            target.getEyePosition(),
            victims,
            source,
            damage * CHAIN_DAMAGE_FACTOR,
            level.getServer().getTickCount() + CHAIN_DELAY_TICKS));
    }

    private static void fireChains(MinecraftServer server) {
        Iterator<PendingChain> chains = CHAINS.iterator();
        while (chains.hasNext()) {
            PendingChain chain = chains.next();
            if (server.getTickCount() < chain.fireTick()) {
                continue;
            }
            chains.remove();
            ServerLevel level = chain.level();
            Vec3 from = level.getEntity(chain.targetId()) instanceof LivingEntity target && target.isAlive()
                ? target.getEyePosition()
                : chain.origin();
            for (int id : chain.victimIds()) {
                if (level.getEntity(id) instanceof LivingEntity victim && victim.isAlive()) {
                    arc(level, from, victim);
                    victim.hurtServer(level, chain.source(), chain.damage());
                }
            }
        }
    }

    private static void arc(ServerLevel level, Vec3 from, LivingEntity victim) {
        Vec3 step = victim.getEyePosition().subtract(from);
        int points = Math.max(1, (int) Math.ceil(step.length() * CHAIN_PARTICLES_PER_BLOCK));
        for (int i = 0; i <= points; i++) {
            Vec3 at = from.add(step.scale((double) i / points));
            level.sendParticles(ParticleTypes.GLOW, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
            SoundEvents.GUARDIAN_HURT, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    private static boolean isChainVictim(ServerPlayer player, LivingEntity target, LivingEntity other) {
        if (other == target
            || other.distanceTo(target) > CHAIN_RADIUS
            || !ShulkerBulletAction.isCandidate(player, other)
            || !target.hasLineOfSight(other)) {
            return false;
        }
        return other instanceof Enemy || other instanceof Mob mob && mob.getTarget() == player;
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

    private record PendingChain(
        ServerLevel level,
        int targetId,
        Vec3 origin,
        List<Integer> victimIds,
        DamageSource source,
        float damage,
        int fireTick
    ) {
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
