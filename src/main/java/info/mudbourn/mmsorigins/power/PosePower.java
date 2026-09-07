package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

/**
 * Forces the holder into a pose while active. Ports the newer Apoli
 * {@code pose}, which Apoli-Legacy lacks; the avian charge crouches while
 * winding up a leap.
 */
public final class PosePower extends Power {

    private final Pose pose;

    public PosePower(PowerType<?> type, LivingEntity entity, Pose pose) {
        super(type, entity);
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }
}
