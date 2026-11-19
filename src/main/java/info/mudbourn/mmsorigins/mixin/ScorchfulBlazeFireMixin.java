package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.BlazebornFire;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Keeps a Blazeborn's flame from heating its victims toward Scorchful's heat stroke.
@Pseudo
@Mixin(targets = "com.github.thedeathlycow.scorchful.temperature.tick.ActiveTemperatureTickListeners")
public abstract class ScorchfulBlazeFireMixin {

    @Inject(method = "getOnFireTemperatureChange", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mmsOrigins$coolBlazeFire(LivingEntity entity, @Coerce Object config,
                                                 CallbackInfoReturnable<Integer> cir) {
        if (BlazebornFire.burning(entity)) {
            cir.setReturnValue(0);
        }
    }
}
