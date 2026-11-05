package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.Comparator;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Fires a homing shulker bullet at the living entity the caster is looking toward.
 *
 * <p>The target is the closest living entity within {@code range} blocks whose
 * direction lies inside a {@code cone_degrees} cone around the caster's look
 * vector and that the caster can see. The caster, their own pets and the dead are
 * never picked. With no target nothing fires, so {@code on_fire} runs only for a
 * bullet actually launched.
 */
public final class ShulkerBulletAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("range", SerializableDataTypes.DOUBLE, 24.0)
            .add("cone_degrees", SerializableDataTypes.DOUBLE, 15.0)
            .add("on_fire", ApoliDataTypes.ENTITY_ACTION, null);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity caster) || !(caster.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = findTarget(caster, level, data.getDouble("range"), data.getDouble("cone_degrees"));
        if (target == null) {
            return;
        }
        ShulkerBullet bullet = new ShulkerBullet(level, caster, target, caster.getDirection().getAxis());
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        bullet.setPos(eye.x + look.x, eye.y + look.y - 0.2, eye.z + look.z);
        level.addFreshEntity(bullet);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        Consumer<Entity> onFire = data.get("on_fire");
        if (onFire != null) {
            onFire.accept(caster);
        }
    }

    /** The closest visible living entity inside the caster's look cone, or null when there is none. */
    public static LivingEntity findTarget(LivingEntity caster, ServerLevel level, double range, double coneDegrees) {
        double minDot = Math.cos(Math.toRadians(coneDegrees));
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(range * 0.5);
        return level.getEntitiesOfClass(LivingEntity.class, box, candidate ->
                isCandidate(caster, candidate)
                    && candidate.getBoundingBox().getCenter().subtract(eye).length() <= range
                    && candidate.getBoundingBox().getCenter().subtract(eye).normalize().dot(look) >= minDot)
            .stream()
            .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(caster)))
            .orElse(null);
    }

    /** Whether a living entity may be picked as the caster's target. */
    public static boolean isCandidate(LivingEntity caster, LivingEntity candidate) {
        if (candidate == caster || !candidate.isAlive() || candidate.isSpectator()) {
            return false;
        }
        if (candidate instanceof OwnableEntity pet && pet.getOwner() == caster) {
            return false;
        }
        return caster.hasLineOfSight(candidate);
    }

    private ShulkerBulletAction() {
    }
}
