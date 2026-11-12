package info.mudbourn.mmsorigins.client;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.food.FoodProperties;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;

/**
 * Shows the player's origin-modified nutrition and saturation in AppleSkin by applying
 * the same Apoli {@code modify_food} powers the server applies when the food is eaten.
 * Only AppleSkin loads this class, through its {@code appleskin} entrypoint.
 */
public class AppleSkinCompat implements AppleSkinApi {
    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(AppleSkinCompat::modifyFoodValues);
    }

    private static void modifyFoodValues(FoodValuesEvent event) {
        List<ModifyFoodPower> powers = PowerHolderComponent.getPowers(event.player, ModifyFoodPower.class)
            .stream()
            .filter(power -> power.doesApply(event.itemStack))
            .toList();
        if (powers.isEmpty()) {
            return;
        }

        List<Modifier> foodModifiers = new ArrayList<>();
        List<Modifier> saturationModifiers = new ArrayList<>();
        for (ModifyFoodPower power : powers) {
            foodModifiers.addAll(power.getFoodModifiers());
            saturationModifiers.addAll(power.getSaturationModifiers());
        }

        FoodProperties base = event.modifiedFoodComponent;
        int nutrition = (int) ModifierUtil.applyModifiers(event.player, foodModifiers, base.nutrition());
        float saturation = (float) ModifierUtil.applyModifiers(event.player, saturationModifiers, base.saturation());
        event.modifiedFoodComponent = new FoodProperties(nutrition, saturation, base.canAlwaysEat());
    }
}
