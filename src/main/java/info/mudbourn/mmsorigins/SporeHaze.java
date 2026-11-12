package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The truffle's mind haze: a green spore cloud that lingers where it is loosed.
 *
 * <p>Every ten ticks the cloud mends its owner, weakens and slows each other mob
 * inside it, and enlists any eligible mob as the owner's soldier through
 * {@link HazeBond}. The Ender Dragon, the Wither and Grovekeepers are weakened but
 * never enlisted.
 */
public final class SporeHaze {

    private static final double RADIUS = 5.0;
    private static final int DURATION = 180;
    private static final int PULSE_INTERVAL = 10;
    private static final int EFFECT_TICKS = 60;
    private static final int DUST_COLOR = 0x5FA82A;
    private static final float DUST_SCALE = 2.0f;
    private static final int DUST_PER_PUFF = 14;
    private static final List<Cloud> CLOUDS = new ArrayList<>();

    private SporeHaze() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SporeHaze::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CLOUDS.clear());
    }

    public static void release(ServerPlayer owner) {
        Vec3 center = owner.position().add(0.0, owner.getBbHeight() * 0.5, 0.0);
        CLOUDS.add(new Cloud(owner.level().dimension(), center, owner.getUUID()));
        owner.level().playSound(
            null,
            center.x,
            center.y,
            center.z,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.PLAYERS,
            1.2f,
            0.6f
        );
    }

    private static void tick(MinecraftServer server) {
        Iterator<Cloud> clouds = CLOUDS.iterator();
        while (clouds.hasNext()) {
            Cloud cloud = clouds.next();
            ServerLevel level = server.getLevel(cloud.dimension);
            if (level == null || cloud.age >= DURATION) {
                clouds.remove();
                continue;
            }
            if (cloud.age % 2 == 0) {
                puff(level, cloud);
            }
            if (cloud.age % PULSE_INTERVAL == 0) {
                pulse(level, cloud);
            }
            cloud.age++;
        }
    }

    private static void puff(ServerLevel level, Cloud cloud) {
        RandomSource random = level.getRandom();
        DustParticleOptions dust = new DustParticleOptions(DUST_COLOR, DUST_SCALE);
        for (int i = 0; i < DUST_PER_PUFF; i++) {
            Vec3 at = scatter(cloud.center, random);
            level.sendParticles(dust, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (int i = 0; i < 3; i++) {
            Vec3 at = scatter(cloud.center, random);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (int i = 0; i < 2; i++) {
            Vec3 at = scatter(cloud.center, random);
            level.sendParticles(ParticleTypes.COMPOSTER, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 scatter(Vec3 center, RandomSource random) {
        Vec3 offset;
        do {
            offset = new Vec3(
                random.nextDouble() * 2.0 - 1.0,
                random.nextDouble() * 2.0 - 1.0,
                random.nextDouble() * 2.0 - 1.0
            );
        } while (offset.lengthSqr() > 1.0);
        return center.add(offset.scale(RADIUS));
    }

    private static void pulse(ServerLevel level, Cloud cloud) {
        List<LivingEntity> inside = level.getEntitiesOfClass(
            LivingEntity.class,
            AABB.ofSize(cloud.center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0),
            entity -> entity.isAlive() && entity.distanceToSqr(cloud.center) <= RADIUS * RADIUS
        );
        for (LivingEntity entity : inside) {
            if (entity.getUUID().equals(cloud.owner)) {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PULSE_INTERVAL * 2, 1, true, true, true));
            } else if (entity instanceof Mob mob) {
                touch(mob, cloud.owner);
            }
        }
    }

    private static void touch(Mob mob, UUID owner) {
        if (HazeBond.isSoldierOf(mob, owner)) {
            return;
        }
        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_TICKS, 0, true, true, true));
        mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_TICKS, 1, true, true, true));
        if (isEnlistable(mob) && HazeBond.activeMaster(mob) == null) {
            HazeBond.enlist(mob, owner);
        }
    }

    private static boolean isEnlistable(Mob mob) {
        return !(mob instanceof EnderDragon) && !(mob instanceof WitherBoss) && !(mob instanceof FloranHenchman);
    }

    private static final class Cloud {

        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final UUID owner;
        private int age;

        private Cloud(ResourceKey<Level> dimension, Vec3 center, UUID owner) {
            this.dimension = dimension;
            this.center = center;
            this.owner = owner;
        }
    }
}
