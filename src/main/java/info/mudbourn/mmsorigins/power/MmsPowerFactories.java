package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.power.action.RandomTeleportAction;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import java.util.List;

/**
 * Registers the Apoli factories that OriginsTweaks relies on but Apoli-Legacy
 * does not ship. Called once from {@link MmsOrigins#onInitialize()}.
 */
public final class MmsPowerFactories {

    public static void register() {
        registerRandomTeleport();
        registerResourceAttribute();
        registerActionOnDeath();
        registerPose();
        registerModifyEnchantmentLevel();
        registerEdibleItem();
    }

    private static void registerEdibleItem() {
        Identifier id = id("edible_item");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                    .add("nutrition", SerializableDataTypes.INT)
                    .add("saturation", SerializableDataTypes.FLOAT, 0.0f)
                    .add("consume_sound", SerializableDataTypes.SOUND_EVENT)
                    .add("eat_seconds", SerializableDataTypes.FLOAT, 1.6f)
                    .add("use_action",
                        SerializableDataType.enumValue(ItemUseAnimation.class),
                        ItemUseAnimation.EAT),
                data -> (type, entity) -> {
                    FoodProperties food = new FoodProperties(
                        data.getInt("nutrition"),
                        data.getFloat("saturation"),
                        false);
                    Consumable consumable = new Consumable(
                        data.getFloat("eat_seconds"),
                        data.get("use_action"),
                        Holder.direct(data.get("consume_sound")),
                        true,
                        List.of());
                    return new EdibleItemPower(type, entity, data.get("item_condition"), consumable, food);
                })
                .allowCondition());
    }

    private static void registerModifyEnchantmentLevel() {
        Identifier id = id("modify_enchantment_level");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                    .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                    .add("modifier", Modifier.DATA_TYPE, null)
                    .add("modifiers", Modifier.LIST_TYPE, null),
                data -> (type, entity) -> {
                    ModifyEnchantmentLevelPower power = new ModifyEnchantmentLevelPower(
                        type,
                        entity,
                        data.get("enchantment"),
                        data.get("item_condition"));
                    data.<Modifier>ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        modifiers -> modifiers.forEach(power::addModifier));
                    return power;
                })
                .allowCondition());
    }

    private static void registerPose() {
        Identifier id = id("pose");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("entity_pose", SerializableDataType.enumValue(Pose.class)),
                data -> (type, entity) -> new PosePower(
                    type,
                    entity,
                    data.get("entity_pose")))
                .allowCondition());
    }

    private static void registerActionOnDeath() {
        Identifier id = id("action_on_death");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION),
                data -> (type, entity) -> new ActionOnDeathPower(
                    type,
                    entity,
                    data.get("bientity_action")))
                .allowCondition());
    }

    private static void registerRandomTeleport() {
        Identifier id = id("random_teleport");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                RandomTeleportAction.data(),
                RandomTeleportAction::action));
    }

    private static void registerResourceAttribute() {
        Identifier id = id("resource_attribute");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("attribute", SerializableDataTypes.ATTRIBUTE)
                    .add("resource", ApoliDataTypes.POWER_TYPE)
                    .add("value", SerializableDataTypes.FLOAT, 1.0f)
                    .add("operation", SerializableDataTypes.MODIFIER_OPERATION,
                        AttributeModifier.Operation.ADD_VALUE),
                data -> (type, entity) -> new ResourceAttributePower(
                    type,
                    entity,
                    data.get("attribute"),
                    data.get("resource"),
                    data.getFloat("value"),
                    data.get("operation")))
                .allowCondition());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, path);
    }

    private MmsPowerFactories() {
    }
}
