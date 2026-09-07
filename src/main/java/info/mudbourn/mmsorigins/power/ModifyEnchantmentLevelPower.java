package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.ValueModifyingPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Boosts the effective level of one enchantment on a queried item stack.
 * Reproduces the newer Apoli power that Apoli-Legacy lacks; the enderian silk
 * touch and avian fortune tweaks use it. The modifiers are applied by
 * {@code EnchantmentHelperMixin} while a mining player is captured.
 */
public final class ModifyEnchantmentLevelPower extends ValueModifyingPower {

    private final ResourceKey<Enchantment> enchantment;
    private final ConditionFactory<ItemStack>.Instance itemCondition;

    public ModifyEnchantmentLevelPower(
            PowerType<?> type,
            LivingEntity entity,
            ResourceKey<Enchantment> enchantment,
            ConditionFactory<ItemStack>.Instance itemCondition) {
        super(type, entity);
        this.enchantment = enchantment;
        this.itemCondition = itemCondition;
    }

    public boolean doesApply(Holder<Enchantment> queried, ItemStack stack) {
        if (!isActive() || !queried.is(enchantment)) {
            return false;
        }
        return itemCondition == null || itemCondition.test(stack);
    }

    public int applyModifiers(int base) {
        return (int) ModifierUtil.applyModifiers(entity, getModifiers(), base);
    }
}
