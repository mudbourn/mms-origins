package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.EnchantmentMiningContext;
import info.mudbourn.mmsorigins.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the queried enchantment level for the block-breaking player captured in
 * {@link EnchantmentMiningContext}, letting {@link ModifyEnchantmentLevelPower}
 * grant tool enchantments the item does not carry.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void mmsOrigins$modifyLevel(
            Holder<Enchantment> enchantment,
            ItemStack stack,
            CallbackInfoReturnable<Integer> cir) {
        Player miner = EnchantmentMiningContext.get();
        if (miner == null) {
            return;
        }
        int level = cir.getReturnValueI();
        boolean modified = false;
        for (ModifyEnchantmentLevelPower power
                : PowerHolderComponent.getPowers(miner, ModifyEnchantmentLevelPower.class)) {
            if (power.doesApply(enchantment, stack)) {
                level = power.applyModifiers(level);
                modified = true;
            }
        }
        if (modified) {
            cir.setReturnValue(level);
        }
    }
}
