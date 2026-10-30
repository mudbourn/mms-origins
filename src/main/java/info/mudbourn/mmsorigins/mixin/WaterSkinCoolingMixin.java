package info.mudbourn.mmsorigins.mixin;

import com.github.thedeathlycow.scorchful.item.WaterSkinItem;
import info.mudbourn.mmsorigins.TemperatureBaseline;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes a finished waterskin sip shed a large share of the drinker's heat at
 * once, on top of Scorchful's own hydration.
 */
@Mixin(ItemStack.class)
public class WaterSkinCoolingMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void mmsOrigins$waterskinCooling(
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (self.getItem() instanceof WaterSkinItem && WaterSkinItem.hasDrink(self)) {
            TemperatureBaseline.coolFromWaterskin(player);
        }
    }
}
