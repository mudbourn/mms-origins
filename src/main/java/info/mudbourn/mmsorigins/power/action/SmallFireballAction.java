package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Hurls one small fireball the way a blaze does: launched slowly from the caster's
 * eyes along their look direction, scattered by {@code spread}, then gathering speed.
 */
public final class SmallFireballAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("spread", SerializableDataTypes.DOUBLE, 0.05);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity caster) || !(caster.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 look = caster.getLookAngle();
        double spread = data.getDouble("spread");
        RandomSource random = caster.getRandom();
        Vec3 direction = new Vec3(
            random.triangle(look.x, spread),
            random.triangle(look.y, spread),
            random.triangle(look.z, spread));
        SmallFireball fireball = new SmallFireball(level, caster, direction.normalize());
        Vec3 eye = caster.getEyePosition();
        fireball.setPos(eye.x + look.x, eye.y + look.y - 0.1, eye.z + look.z);
        level.addFreshEntity(fireball);
        level.levelEvent(null, LevelEvent.SOUND_BLAZE_FIREBALL, caster.blockPosition(), 0);
    }

    private SmallFireballAction() {
    }
}
