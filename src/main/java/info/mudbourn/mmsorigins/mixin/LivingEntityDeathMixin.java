package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.ActionOnDeathPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs {@link ActionOnDeathPower} for the dying entity, replacing the newer
 * Apoli {@code action_on_death} power type that Apoli-Legacy does not ship.
 */
@Mixin(LivingEntity.class)
public class LivingEntityDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void mmsOrigins$actionOnDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        for (ActionOnDeathPower power
                : PowerHolderComponent.getPowers(self, ActionOnDeathPower.class)) {
            power.onDeath(source);
        }
    }
}
