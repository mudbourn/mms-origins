package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.FishmanRiptide;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a fishman spin-attack with a riptide trident on dry land while wetness
 * lasts, and charges the whole wetness reserve for each dry launch.
 */
@Mixin(TridentItem.class)
public class TridentRiptideMixin {

    @Redirect(
        method = {"use", "releaseUsing"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean mms_origins$allowDryRiptide(Player player) {
        return player.isInWaterOrRain() || FishmanRiptide.isWet(player);
    }

    @Inject(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startAutoSpinAttack(IFLnet/minecraft/world/item/ItemStack;)V"))
    private void mms_origins$spendWetnessOnDryRiptide(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            int timeLeft,
            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && !player.isInWaterOrRain()) {
            FishmanRiptide.drainWetness(player);
        }
    }
}
