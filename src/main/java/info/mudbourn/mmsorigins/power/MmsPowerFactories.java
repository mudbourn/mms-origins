package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.power.action.CausticSporesAction;
import info.mudbourn.mmsorigins.power.action.RandomTeleportAction;
import info.mudbourn.mmsorigins.power.action.SummonHenchmenAction;
import info.mudbourn.mmsorigins.power.condition.CausticMarkedCondition;
import info.mudbourn.mmsorigins.power.condition.HeightAboveGroundCondition;
import info.mudbourn.mmsorigins.power.condition.HenchmenCountCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.HudRender;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
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
        registerGrapple();
        registerActiveSelfHud();
        registerHeightAboveGround();
        registerFairyFlight();
        registerBlazeFlight();
        registerSummonHenchmen();
        registerHenchmenCount();
        registerCausticSpores();
        registerCausticMarked();
        registerNatureReclaim();
    }

    private static void registerNatureReclaim() {
        Identifier id = id("nature_reclaim");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("cast_ticks", SerializableDataTypes.INT, 100)
                    .add("sound_delay", SerializableDataTypes.INT, 20)
                    .add("charging_sound", SerializableDataTypes.SOUND_EVENT, null)
                    .add("finish_sound", SerializableDataTypes.SOUND_EVENT, null)
                    .add("uses_resource", ApoliDataTypes.POWER_TYPE, null)
                    .add("sun_resource", ApoliDataTypes.POWER_TYPE, null)
                    .add("sun_cost", SerializableDataTypes.INT, 0)
                    .add("noon_min", SerializableDataTypes.INT, 5000)
                    .add("noon_max", SerializableDataTypes.INT, 7000)
                    .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> {
                    NatureReclaimPower power = new NatureReclaimPower(
                        type,
                        entity,
                        data.getInt("cast_ticks"),
                        data.getInt("sound_delay"),
                        data.get("charging_sound"),
                        data.get("finish_sound"),
                        data.get("uses_resource"),
                        data.get("sun_resource"),
                        data.getInt("sun_cost"),
                        data.getInt("noon_min"),
                        data.getInt("noon_max"));
                    power.setKey(data.get("key"));
                    return power;
                })
                .allowCondition());
    }

    private static void registerCausticSpores() {
        Identifier id = id("caustic_spores");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                CausticSporesAction.data(),
                CausticSporesAction::action));
    }

    private static void registerCausticMarked() {
        Identifier id = id("caustic_marked");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                CausticMarkedCondition.data(),
                CausticMarkedCondition::condition));
    }

    private static void registerHenchmenCount() {
        Identifier id = id("henchmen_count");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                HenchmenCountCondition.data(),
                HenchmenCountCondition::condition));
    }

    private static void registerSummonHenchmen() {
        Identifier id = id("summon_henchmen");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                SummonHenchmenAction.data(),
                SummonHenchmenAction::action));
    }

    private static void registerFairyFlight() {
        Identifier id = id("fairy_flight");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData(),
                data -> (type, entity) -> new FairyFlightPower(type, entity))
                .allowCondition());
    }

    private static void registerBlazeFlight() {
        Identifier id = id("blaze_flight");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("resource", ApoliDataTypes.POWER_TYPE, null),
                data -> (type, entity) -> new BlazeFlightPower(type, entity, data.get("resource")))
                .allowCondition());
    }

    private static void registerHeightAboveGround() {
        Identifier id = id("height_above_ground");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                HeightAboveGroundCondition.data(),
                HeightAboveGroundCondition::condition));
    }

    private static void registerActiveSelfHud() {
        Identifier id = id("active_self_hud");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                    .add("cooldown", SerializableDataTypes.INT, 1)
                    .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                    .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> {
                    HudCooldownPower power = new HudCooldownPower(
                        type,
                        entity,
                        data.getInt("cooldown"),
                        data.get("hud_render"),
                        data.get("entity_action"));
                    power.setKey(data.get("key"));
                    return power;
                })
                .allowCondition());
    }

    private static void registerGrapple() {
        Identifier id = id("grapple");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("max_length", SerializableDataTypes.DOUBLE, 24.0)
                    .add("reel_speed", SerializableDataTypes.DOUBLE, 1.0)
                    .add("entity_reel_speed", SerializableDataTypes.DOUBLE, 0.9)
                    .add("arrive_distance", SerializableDataTypes.DOUBLE, 1.5)
                    .add("silk_resource", ApoliDataTypes.POWER_TYPE, null)
                    .add("silk_cost_interval", SerializableDataTypes.INT, 15)
                    .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                    .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> {
                    GrappleHookPower power = new GrappleHookPower(
                        type,
                        entity,
                        data.getDouble("max_length"),
                        data.getDouble("reel_speed"),
                        data.getDouble("entity_reel_speed"),
                        data.getDouble("arrive_distance"),
                        data.get("silk_resource"),
                        data.getInt("silk_cost_interval"),
                        data.get("sound"));
                    power.setKey(data.get("key"));
                    return power;
                })
                .allowCondition());
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
                        ItemUseAnimation.EAT)
                    .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, List.of())
                    .add("set_on_fire_seconds", SerializableDataTypes.FLOAT, 0.0f)
                    .add("loop_consume_effects", SerializableDataTypes.BOOLEAN, true)
                    .add("finish_sound", SerializableDataTypes.SOUND_EVENT, null),
                data -> (type, entity) -> {
                    FoodProperties food = new FoodProperties(
                        data.getInt("nutrition"),
                        data.getFloat("saturation"),
                        false);
                    List<MobEffectInstance> effects = data.get("effects");
                    List<ConsumeEffect> onConsume = effects.isEmpty()
                        ? List.of()
                        : List.of(new ApplyStatusEffectsConsumeEffect(effects));
                    Consumable consumable = new Consumable(
                        data.getFloat("eat_seconds"),
                        data.get("use_action"),
                        Holder.direct(data.get("consume_sound")),
                        true,
                        onConsume);
                    return new EdibleItemPower(
                        type,
                        entity,
                        data.get("item_condition"),
                        consumable,
                        food,
                        data.getFloat("set_on_fire_seconds"),
                        data.getBoolean("loop_consume_effects"),
                        data.get("finish_sound"));
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
