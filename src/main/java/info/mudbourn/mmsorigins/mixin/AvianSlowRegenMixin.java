package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Avian slow mending: hunger-driven natural regeneration heals the bearer less per tick.
 */
@Mixin(FoodData.class)
public class AvianSlowRegenMixin {

    private static final float REGEN_FACTOR = 0.8F;

    @ModifyArg(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V")
    )
    private float mmsOrigins$slowAvianRegen(float amount, @Local(argsOnly = true) ServerPlayer player) {
        return MmsOriginsPowers.AVIAN_SLOW_REGEN.isActive(player) ? amount * REGEN_FACTOR : amount;
    }
}
