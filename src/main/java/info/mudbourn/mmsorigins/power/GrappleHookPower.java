package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A held grappling hook. Faithful to the old web shot: it fires once toward the
 * crosshair up to its full range, marks the hit with a cobweb, and drags the
 * holder straight to it. The upgrade is that the pull sustains while the key is
 * held, so it reels the holder all the way in instead of a single leap that
 * fell short, and an entity hit is reeled toward the holder instead.
 *
 * <p>Apoli cannot remember where the web caught between ticks, so the reel is
 * Java. The pull overwrites velocity toward the anchor each tick, so it climbs
 * to rooftops without arcing and never speeds up with distance.
 */
public final class GrappleHookPower extends Power implements Active {

    private static final int HOLD_GRACE_TICKS = 3;
    private static final int WEB_COLOR = 0xFFFFFF;

    private final double maxLength;
    private final double reelSpeed;
    private final double entityReelSpeed;
    private final double arriveDistance;
    private final PowerType<?> silkResource;
    private final int silkCostInterval;
    private final SoundEvent sound;

    private Key key = new Key();
    private int ticksSinceUse = HOLD_GRACE_TICKS + 1;
    private Vec3 anchorPoint;
    private LivingEntity anchorEntity;
    private int silkTimer;

    public GrappleHookPower(
            PowerType<?> type,
            LivingEntity entity,
            double maxLength,
            double reelSpeed,
            double entityReelSpeed,
            double arriveDistance,
            PowerType<?> silkResource,
            int silkCostInterval,
            SoundEvent sound) {
        super(type, entity);
        this.maxLength = maxLength;
        this.reelSpeed = reelSpeed;
        this.entityReelSpeed = entityReelSpeed;
        this.arriveDistance = arriveDistance;
        this.silkResource = silkResource;
        this.silkCostInterval = silkCostInterval;
        this.sound = sound;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        ticksSinceUse = 0;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player) || entity.level().isClientSide()) {
            return;
        }
        boolean held = ticksSinceUse <= HOLD_GRACE_TICKS;
        ticksSinceUse++;
        if (!held) {
            release();
            return;
        }
        if (anchorPoint == null && anchorEntity == null) {
            VariableIntPower silk = findSilk(player);
            if (silk != null && silk.getValue() <= 0) {
                return;
            }
            acquire(player, silk);
            return;
        }
        if (!spendSilk(player)) {
            release();
            return;
        }
        if (anchorEntity != null) {
            reelEntity(player);
        } else {
            reelToBlock(player);
        }
    }

    private void acquire(Player player, VariableIntPower silk) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(maxLength));
        BlockHitResult block = level.clip(
            new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = block.getType() == HitResult.Type.MISS ? maxLength : eye.distanceTo(block.getLocation());
        AABB search = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player,
            eye,
            eye.add(look.scale(reach)),
            search,
            target -> target instanceof LivingEntity living && living.isAlive() && living != player,
            reach * reach);
        Vec3 to;
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            anchorEntity = living;
            to = living.getEyePosition();
        } else if (block.getType() == HitResult.Type.BLOCK) {
            anchorPoint = block.getLocation();
            to = anchorPoint;
        } else {
            return;
        }
        if (silk != null) {
            silk.decrement();
            PowerHolderComponent.syncPower(player, silk.getType());
        }
        drawWeb(player, eye, to);
        playShot(player);
    }

    private void reelToBlock(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 toAnchor = anchorPoint.subtract(eye);
        double dist = toAnchor.length();
        drawWeb(player, eye, anchorPoint);
        markAnchor(player, anchorPoint);
        if (dist < arriveDistance) {
            drive(player, player.getDeltaMovement().scale(0.2));
            return;
        }
        drive(player, toAnchor.scale(reelSpeed / dist));
    }

    private void reelEntity(Player player) {
        if (!anchorEntity.isAlive() || anchorEntity.isRemoved()) {
            release();
            return;
        }
        Vec3 toPlayer = player.position().subtract(anchorEntity.position());
        double dist = toPlayer.length();
        if (dist > maxLength + 4.0) {
            release();
            return;
        }
        drawWeb(player, player.getEyePosition(), anchorEntity.getEyePosition());
        if (dist < arriveDistance) {
            return;
        }
        drive(anchorEntity, toPlayer.scale(entityReelSpeed / dist));
    }

    private void drive(LivingEntity target, Vec3 velocity) {
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.resetFallDistance();
    }

    private void drawWeb(Player player, Vec3 from, Vec3 to) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 step = to.subtract(from);
        int points = (int) Math.max(1, step.length());
        Vec3 inc = step.scale(1.0 / points);
        Vec3 at = from;
        DustParticleOptions dust = new DustParticleOptions(WEB_COLOR, 1.0F);
        for (int i = 0; i <= points; i++) {
            level.sendParticles(dust, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            at = at.add(inc);
        }
    }

    private void markAnchor(Player player, Vec3 point) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.COBWEB.defaultBlockState()),
                point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void playShot(Player player) {
        if (sound != null && player.level() instanceof ServerLevel level) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 0.6F, 1.4F);
        }
    }

    private VariableIntPower findSilk(Player player) {
        if (silkResource == null) {
            return null;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType() == silkResource
                    || power.getType().getIdentifier().getPath().endsWith("silk_resource")) {
                return power;
            }
        }
        return null;
    }

    private boolean spendSilk(Player player) {
        VariableIntPower silk = findSilk(player);
        if (silk == null) {
            return true;
        }
        if (silk.getValue() <= 0) {
            return false;
        }
        if (++silkTimer >= silkCostInterval) {
            silkTimer = 0;
            silk.decrement();
            PowerHolderComponent.syncPower(player, silk.getType());
        }
        return true;
    }

    private void release() {
        anchorPoint = null;
        anchorEntity = null;
        silkTimer = 0;
    }
}
