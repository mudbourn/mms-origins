package info.mudbourn.mmsorigins.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import java.util.function.Consumer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Runs a bientity action when the holder dies, with the killer as the actor and
 * the holder as the target. Ports the newer Apoli {@code action_on_death}, which
 * Apoli-Legacy lacks; the shulk shell resets its shield resource on death.
 */
public final class ActionOnDeathPower extends Power {

    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionOnDeathPower(
            PowerType<?> type,
            LivingEntity entity,
            Consumer<Pair<Entity, Entity>> bientityAction) {
        super(type, entity);
        this.bientityAction = bientityAction;
    }

    public void onDeath(DamageSource source) {
        if (bientityAction != null) {
            bientityAction.accept(new Pair<>(source.getEntity(), entity));
        }
    }
}
