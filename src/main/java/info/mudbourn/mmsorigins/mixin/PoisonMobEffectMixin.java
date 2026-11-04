package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmsorigins.effect.Venom;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.PoisonMobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Deals venom damage instead of magic damage on poison ticks while the victim is
 * inside a {@link Venom} window, so arachnid poison ignores armor and Protection.
 */
@Mixin(PoisonMobEffect.class)
public class PoisonMobEffectMixin {

    @WrapOperation(
        method = "applyEffectTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean mmsOrigins$venomDamage(
            LivingEntity entity,
            ServerLevel level,
            DamageSource source,
            float amount,
            Operation<Boolean> original) {
        DamageSource used = Venom.isVenomed(entity) ? Venom.source(level) : source;
        return original.call(entity, level, used, amount);
    }
}
