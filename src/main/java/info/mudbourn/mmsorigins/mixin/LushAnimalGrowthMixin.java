package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.LushSoil;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Ages baby animals and counts down breeding cooldowns an extra step on lush soil.
@Mixin(AgeableMob.class)
public abstract class LushAnimalGrowthMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mmsOrigins$lushAging(CallbackInfo ci) {
        if (!((Object) this instanceof Animal animal)
                || animal.level().isClientSide()
                || !animal.isAlive()
                || !animal.onGround()
                || !LushSoil.bonusTick(animal.tickCount)
                || !LushSoil.at(animal.level(), animal.getOnPos())) {
            return;
        }
        int age = animal.getAge();
        if (age < 0) {
            animal.setAge(age + 1);
        } else if (age > 0) {
            animal.setAge(age - 1);
        }
    }
}
