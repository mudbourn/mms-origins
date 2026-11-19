package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.BlazebornFire;
import info.mudbourn.mmsorigins.WildfireSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A burning shield a Blazeborn flings straight ahead. It flies without falling for a fixed
 * distance, bursts in an explosion on the first entity it touches and sets it alight unless
 * it is a player, and gutters out harmlessly against a block or at the end of its flight.
 */
public class WildfireShield extends ThrowableProjectile {

    private static final double RANGE = 20.0;
    private static final float BLAST_POWER = 2.0f;
    private static final float IGNITE_SECONDS = 5.0f;

    private Vec3 origin;

    public WildfireShield(EntityType<? extends WildfireShield> type, Level level) {
        super(type, level);
    }

    public WildfireShield(Level level, LivingEntity owner) {
        super(MmsEntities.WILDFIRE_SHIELD, level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }
        if (this.origin == null) {
            this.origin = this.position();
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 3, 0.2, 0.2, 0.2, 0.01);
        if (this.position().distanceToSqr(this.origin) >= RANGE * RANGE) {
            this.gutter(level);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (this.level() instanceof ServerLevel level) {
            level.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_POWER, false, Level.ExplosionInteraction.NONE);
            this.playSound(WildfireSounds.get(WildfireSounds.SHOCKWAVE), 1.0f, 1.0f);
            if (!(hit.getEntity() instanceof Player)) {
                BlazebornFire.ignite(hit.getEntity(), IGNITE_SECONDS);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (this.level() instanceof ServerLevel level) {
            this.gutter(level);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hit);
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) hit);
        }
    }

    private void gutter(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        this.playSound(WildfireSounds.get(WildfireSounds.SHIELD_BREAK), 1.0f, 1.0f);
        this.discard();
    }
}
