package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the queried enchantment level for an entity's equipment, the sibling
 * lookup to {@link EnchantmentHelperMixin}. Looting reads through this path with
 * the killer as the entity, so a {@link ModifyEnchantmentLevelPower} keyed to
 * looting grants extra mob drops the weapon never carried.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentEntityLevelMixin {

    @Inject(method = "getEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void mmsOrigins$modifyEntityLevel(
            Holder<Enchantment> enchantment,
            LivingEntity entity,
            CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int level = cir.getReturnValueI();
        boolean modified = false;
        for (ModifyEnchantmentLevelPower power
                : PowerHolderComponent.getPowers(player, ModifyEnchantmentLevelPower.class)) {
            if (power.doesApply(enchantment, ItemStack.EMPTY)) {
                level = power.applyModifiers(level);
                modified = true;
            }
        }
        if (modified) {
            cir.setReturnValue(level);
        }
    }
}
