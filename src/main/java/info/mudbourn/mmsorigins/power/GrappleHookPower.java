package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.WebReel;
import info.mudbourn.mmsorigins.effect.MmsMobEffects;
import info.mudbourn.mmsorigins.network.WebLinePayload;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A held web line. It fires once toward the crosshair and holds while the key is
 * held. Only creature shots spend silk; block shots are free. On a block it becomes a rope the holder swings from, and holding use with
 * an empty hand reels them in, speeding up after a delay; the swing itself runs on
 * the holder's client in {@link info.mudbourn.mmsorigins.client.WebSwing}. On a
 * creature, the same rope tethers the creature instead: it cannot stray past the rope,
 * reeling pulls it in with the same ramp, and crouching pays the rope out.
 */
public final class GrappleHookPower extends Power implements Active {

    private static final int HOLD_GRACE_TICKS = 3;
    private static final int WEBBED_TICKS = 60;
    private static final double BREAK_SLACK = 8.0;
    private static final double EXTEND_SPEED = 0.35;
    private static final double TAUT_PULL = 0.3;
    private static final double MAX_TAUT_CORRECTION = 1.0;
    private static final double ROPE_SYNC_STEP = 0.5;
    private static final int ROPE_SYNC_TICKS = 4;

    private final double maxLength;
    private final double blockMaxLength;
    private final double reelSpeed;
    private final double maxReelSpeed;
    private final int reelRampDelay;
    private final int reelRampTicks;
    private final double entityReelSpeed;
    private final double maxEntityReelSpeed;
    private final double arriveDistance;
    private final PowerType<?> silkResource;
    private final int silkCostInterval;
    private final int snareSilkCost;
    private final SoundEvent sound;

    private Key key = new Key();
    private int ticksSinceUse = HOLD_GRACE_TICKS + 1;
    private Vec3 anchorPoint;
    private LivingEntity anchorEntity;
    private int silkTimer;
    private double entityRope;
    private double syncedRope;
    private int entityReelTicks;
    private int ropeSyncTimer;

    public GrappleHookPower(
            PowerType<?> type,
            LivingEntity entity,
            double maxLength,
            double blockMaxLength,
            double reelSpeed,
            double maxReelSpeed,
            int reelRampDelay,
            int reelRampTicks,
            double entityReelSpeed,
            double maxEntityReelSpeed,
            double arriveDistance,
            PowerType<?> silkResource,
            int silkCostInterval,
            int snareSilkCost,
            SoundEvent sound) {
        super(type, entity);
        this.maxLength = maxLength;
        this.blockMaxLength = blockMaxLength;
        this.reelSpeed = reelSpeed;
        this.maxReelSpeed = maxReelSpeed;
        this.reelRampDelay = reelRampDelay;
        this.reelRampTicks = reelRampTicks;
        this.entityReelSpeed = entityReelSpeed;
        this.maxEntityReelSpeed = maxEntityReelSpeed;
        this.arriveDistance = arriveDistance;
        this.silkResource = silkResource;
        this.silkCostInterval = silkCostInterval;
        this.snareSilkCost = snareSilkCost;
        this.sound = sound;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        ticksSinceUse = 0;
    }

    @Override
    public void onLost() {
        release();
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
        if (WebReel.consumeFreshPress(player)) {
            release();
        }
        boolean held = ticksSinceUse <= HOLD_GRACE_TICKS;
        ticksSinceUse++;
        if (!held) {
            release();
            return;
        }
        if (anchorPoint == null && anchorEntity == null) {
            acquire(player, findSilk(player));
            return;
        }
        if (anchorEntity != null) {
            reelEntity(player);
        } else {
            holdBlock(player);
        }
    }

    private void acquire(Player player, VariableIntPower silk) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(blockMaxLength));
        BlockHitResult block = level.clip(
            new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double blockReach = block.getType() == HitResult.Type.MISS
            ? blockMaxLength
            : eye.distanceTo(block.getLocation());
        double reach = Math.min(blockReach, maxLength);
        AABB search = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player,
            eye,
            eye.add(look.scale(reach)),
            search,
            target -> target instanceof LivingEntity living && living.isAlive() && living != player,
            reach * reach);
        Vec3 to;
        boolean hasSilk = silk == null || silk.getValue() >= snareSilkCost;
        if (hasSilk && hit != null && hit.getEntity() instanceof LivingEntity living) {
            anchorEntity = living;
            entityRope = Math.max(arriveDistance, player.position().distanceTo(living.position()));
            syncedRope = entityRope;
            entityReelTicks = 0;
            if (silk != null) {
                silk.setValue(silk.getValue() - snareSilkCost);
                PowerHolderComponent.syncPower(player, silk.getType());
            }
            living.addEffect(new MobEffectInstance(MmsMobEffects.WEBBED, WEBBED_TICKS), player);
            to = living.getEyePosition();
        } else if (block.getType() == HitResult.Type.BLOCK) {
            anchorPoint = block.getLocation();
            to = anchorPoint;
        } else {
            return;
        }
        WebReel.attach(player, new WebLinePayload(
            player.getId(),
            true,
            to.x,
            to.y,
            to.z,
            anchorEntity == null ? WebLinePayload.NO_ENTITY : anchorEntity.getId(),
            (float) eye.distanceTo(to),
            (float) blockMaxLength,
            (float) reelSpeed,
            (float) maxReelSpeed,
            reelRampDelay,
            reelRampTicks));
        playShot(player);
    }

    private void holdBlock(Player player) {
        Vec3 eye = player.getEyePosition();
        if (eye.distanceTo(anchorPoint) > blockMaxLength + BREAK_SLACK) {
            release();
            return;
        }
        player.resetFallDistance();
    }

    private void reelEntity(Player player) {
        if (!anchorEntity.isAlive() || anchorEntity.isRemoved()) {
            release();
            return;
        }
        Vec3 toPlayer = player.position().subtract(anchorEntity.position());
        double dist = toPlayer.length();
        if (dist > maxLength + BREAK_SLACK || dist < 1.0E-4) {
            release();
            return;
        }
        Vec3 dir = toPlayer.scale(1.0 / dist);
        Vec3 velocity = anchorEntity.getDeltaMovement();
        double inward = velocity.dot(dir);
        boolean reeling = WebReel.isReeling(player);
        if (reeling && !spendSilk(player)) {
            release();
            return;
        }
        if (reeling) {
            entityReelTicks++;
            double speed = entityReelSpeed(entityReelTicks);
            if (dist > arriveDistance && inward < speed) {
                velocity = velocity.add(dir.scale(speed - inward));
            }
            entityRope = Math.max(arriveDistance, Math.min(entityRope, dist - speed));
        } else {
            entityReelTicks = 0;
            if (player.isShiftKeyDown()) {
                entityRope = Math.min(maxLength, Math.max(entityRope, dist) + EXTEND_SPEED);
            }
            if (dist <= entityRope) {
                syncEntityRope(player);
                return;
            }
            if (inward < 0.0) {
                velocity = velocity.subtract(dir.scale(inward));
            }
            double stretch = Math.min(dist - entityRope, MAX_TAUT_CORRECTION);
            velocity = velocity.add(dir.scale(stretch * TAUT_PULL));
        }
        drive(anchorEntity, velocity);
        syncEntityRope(player);
    }

    private double entityReelSpeed(int ticks) {
        if (ticks <= reelRampDelay) {
            return entityReelSpeed;
        }
        double t = Mth.clamp((ticks - reelRampDelay) / (double) Math.max(1, reelRampTicks), 0.0, 1.0);
        double eased = t * t * (3.0 - 2.0 * t);
        return Mth.lerp(eased, entityReelSpeed, maxEntityReelSpeed);
    }

    private void syncEntityRope(Player player) {
        ropeSyncTimer++;
        if (Math.abs(entityRope - syncedRope) >= ROPE_SYNC_STEP && ropeSyncTimer >= ROPE_SYNC_TICKS) {
            syncedRope = entityRope;
            ropeSyncTimer = 0;
            WebReel.updateRope(player, (float) entityRope);
        }
    }

    private void drive(LivingEntity target, Vec3 velocity) {
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        target.resetFallDistance();
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
        if ((anchorPoint != null || anchorEntity != null) && entity instanceof Player player) {
            WebReel.detach(player);
        }
        anchorPoint = null;
        anchorEntity = null;
        silkTimer = 0;
    }
}
