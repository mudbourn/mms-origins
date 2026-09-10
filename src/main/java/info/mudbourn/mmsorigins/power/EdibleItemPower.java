package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Makes an otherwise inedible item eatable while the power is active,
 * reproducing the newer Apoli {@code edible_item} power that Apoli-Legacy omits.
 * The avian glistering melon tweak uses it. {@code ItemStackConsumeMixin} routes
 * item use, duration and completion through the {@link Consumable} built here.
 */
public final class EdibleItemPower extends Power {

    // One entry per edible power type; used to pick the eating animation, which is
    // queried without an entity in scope. Only power holders ever enter the using
    // state for the item, so matching on the item condition alone is safe.
    private static final Map<Identifier, EdibleItemPower> BY_TYPE = new ConcurrentHashMap<>();

    private final ConditionFactory<ItemStack>.Instance itemCondition;
    private final Consumable consumable;
    private final FoodProperties food;
    private final float fireSeconds;
    private final boolean loopConsumeEffects;
    private final SoundEvent finishSound;

    public EdibleItemPower(
            PowerType<?> type,
            LivingEntity entity,
            ConditionFactory<ItemStack>.Instance itemCondition,
            Consumable consumable,
            FoodProperties food,
            float fireSeconds,
            boolean loopConsumeEffects,
            SoundEvent finishSound) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.consumable = consumable;
        this.food = food;
        this.fireSeconds = fireSeconds;
        this.loopConsumeEffects = loopConsumeEffects;
        this.finishSound = finishSound;
        BY_TYPE.put(type.getIdentifier(), this);
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

    /**
     * @return the eating animation for a stack any edible power targets, or {@code null}
     *         when no power turns this stack into food.
     */
    public static ItemUseAnimation animationFor(ItemStack stack) {
        if (stack.has(DataComponents.CONSUMABLE)) {
            return null;
        }
        for (EdibleItemPower power : BY_TYPE.values()) {
            if (power.itemCondition != null && power.itemCondition.test(stack)) {
                return power.consumable.animation();
            }
        }
        return null;
    }

    public Consumable getConsumable() {
        return consumable;
    }

    public FoodProperties getFood() {
        return food;
    }

    /** @return how many seconds to set the eater alight on consuming, or zero to leave them unlit. */
    public float getFireSeconds() {
        return fireSeconds;
    }

    /** @return whether to repeat the chewing sound and particles mid-bite, or only emit once on completion. */
    public boolean loopsConsumeEffects() {
        return loopConsumeEffects;
    }

    /** @return a one-shot sound played when the bite completes, like a food burp, or {@code null} for none. */
    public SoundEvent getFinishSound() {
        return finishSound;
    }
}
