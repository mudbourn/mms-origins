package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

/**
 * Fires one small fireball from the caster's eyes along their look direction.
 */
public final class SmallFireballAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("speed", SerializableDataTypes.FLOAT, 1.5F);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity caster) || !(caster.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 look = caster.getLookAngle();
        SmallFireball fireball = new SmallFireball(level, caster, look);
        Vec3 eye = caster.getEyePosition();
        fireball.setPos(eye.x + look.x, eye.y + look.y - 0.1, eye.z + look.z);
        fireball.shoot(look.x, look.y, look.z, data.getFloat("speed"), 0.0F);
        level.addFreshEntity(fireball);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private SmallFireballAction() {
    }
}
