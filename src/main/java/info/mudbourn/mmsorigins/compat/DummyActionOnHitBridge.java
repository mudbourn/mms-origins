package info.mudbourn.mmsorigins.compat;

import info.mudbourn.mmscombat.dummy.TrainingDummy;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnHitPower;
import io.github.apace100.apoli.power.SelfActionOnHitPower;
import io.github.apace100.apoli.power.TargetActionOnHitPower;
import net.minecraft.world.entity.LivingEntity;

// Fires the attacker's Apoli on-hit powers on a training dummy hit, since the dummy cancels the hurt that would normally fire them.
public final class DummyActionOnHitBridge {

    private DummyActionOnHitBridge() {
    }

    public static void register() {
        TrainingDummy.addDamageHook((dummy, source, dealt) -> {
            if (!(source.getEntity() instanceof LivingEntity attacker)) {
                return;
            }
            for (ActionOnHitPower power : PowerHolderComponent.getPowers(attacker, ActionOnHitPower.class)) {
                power.onHit(dummy, source, dealt);
            }
            for (SelfActionOnHitPower power : PowerHolderComponent.getPowers(attacker, SelfActionOnHitPower.class)) {
                power.onHit(dummy, source, dealt);
            }
            for (TargetActionOnHitPower power : PowerHolderComponent.getPowers(attacker, TargetActionOnHitPower.class)) {
                power.onHit(dummy, source, dealt);
            }
        });
    }
}
