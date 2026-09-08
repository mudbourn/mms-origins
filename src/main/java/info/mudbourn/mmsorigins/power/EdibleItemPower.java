package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;

/**
 * Makes an otherwise inedible item eatable while the power is active,
 * reproducing the newer Apoli {@code edible_item} power that Apoli-Legacy omits.
 * The avian glistering melon tweak uses it. {@code ItemStackConsumeMixin} routes
 * item use, duration and completion through the {@link Consumable} built here.
 */
public final class EdibleItemPower extends Power {

    private final ConditionFactory<ItemStack>.Instance itemCondition;
    private final Consumable consumable;
    private final FoodProperties food;

    public EdibleItemPower(
            PowerType<?> type,
            LivingEntity entity,
            ConditionFactory<ItemStack>.Instance itemCondition,
            Consumable consumable,
            FoodProperties food) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.consumable = consumable;
        this.food = food;
    }

    public static EdibleItemPower find(LivingEntity entity, ItemStack stack) {
        for (EdibleItemPower power : PowerHolderComponent.getPowers(entity, EdibleItemPower.class)) {
            if (power.doesApply(stack)) {
                return power;
            }
        }
        return null;
    }

    public boolean doesApply(ItemStack stack) {
        if (!isActive() || stack.has(DataComponents.CONSUMABLE)) {
            return false;
        }
        return itemCondition == null || itemCondition.test(stack);
    }

    public Consumable getConsumable() {
        return consumable;
    }

    public FoodProperties getFood() {
        return food;
    }
}
