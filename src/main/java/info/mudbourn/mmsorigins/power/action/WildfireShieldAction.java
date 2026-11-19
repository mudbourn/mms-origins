package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.WildfireSounds;
import info.mudbourn.mmsorigins.entity.WildfireShield;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Flings one {@link WildfireShield} from the caster's eyes along their look direction.
 */
public final class WildfireShieldAction {

    public static SerializableData data() {
        return new SerializableData()
            .add("speed", SerializableDataTypes.FLOAT, 1.2F);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity caster) || !(caster.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 look = caster.getLookAngle();
        WildfireShield shield = new WildfireShield(level, caster);
        Vec3 eye = caster.getEyePosition();
        shield.setPos(eye.x + look.x, eye.y + look.y - 0.1, eye.z + look.z);
        shield.shoot(look.x, look.y, look.z, data.getFloat("speed"), 0.0F);
        level.addFreshEntity(shield);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
            WildfireSounds.get(WildfireSounds.SHOOT), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private WildfireShieldAction() {
    }
}
