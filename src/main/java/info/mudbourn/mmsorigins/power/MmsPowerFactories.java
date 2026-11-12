package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.FairyBlessing;
import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.power.action.CausticSporesAction;
import info.mudbourn.mmsorigins.power.action.CultivateStoneAction;
import info.mudbourn.mmsorigins.power.action.GuardianBeamAction;
import info.mudbourn.mmsorigins.power.action.LaunchAction;
import info.mudbourn.mmsorigins.power.action.RandomTeleportAction;
import info.mudbourn.mmsorigins.power.action.ShulkerBulletAction;
import info.mudbourn.mmsorigins.power.action.SmallFireballAction;
import info.mudbourn.mmsorigins.power.action.SporeHazeAction;
import info.mudbourn.mmsorigins.power.action.SummonHenchmenAction;
import info.mudbourn.mmsorigins.power.action.SurfaceAction;
import info.mudbourn.mmsorigins.power.action.VenomPoisonAction;
import info.mudbourn.mmsorigins.power.condition.CausticMarkedCondition;
import info.mudbourn.mmsorigins.power.condition.HeightAboveGroundCondition;
import info.mudbourn.mmsorigins.power.condition.HenchmenCountCondition;
import info.mudbourn.mmsorigins.power.condition.IncapacitatedCondition;
import info.mudbourn.mmsorigins.power.condition.ResourceFractionCondition;
import info.mudbourn.mmsorigins.power.condition.SunIntakeCondition;
import info.mudbourn.mmsorigins.power.condition.SunStrainCondition;
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
        registerPhantomFlight();
        registerKeyGesture();
        registerResourceFraction();
        registerSummonHenchmen();
        registerHenchmenCount();
        registerCausticSpores();
        registerSporeHaze();
        registerCultivateStone();
        registerVenomPoison();
        registerShulkerBullet();
        registerSmallFireball();
        registerGuardianBeam();
        registerLaunch();
        registerSurface();
        registerCausticMarked();
        registerNatureReclaim();
        registerIncapacitated();
        registerSunIntake();
        registerFairyBlessing();
    }

    private static void registerFairyBlessing() {
        Identifier id = id("near_fairy");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                new SerializableData(),
                FairyBlessing::condition));
    }

    private static void registerIncapacitated() {
        Identifier id = id("incapacitated");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                IncapacitatedCondition.data(),
                IncapacitatedCondition::condition));
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
                    .add("strain_resource", ApoliDataTypes.POWER_TYPE, null)
                    .add("noon_strain", SerializableDataTypes.INT, 10)
                    .add("sun_resource", ApoliDataTypes.POWER_TYPE, null)
                    .add("noon_sun_cost", SerializableDataTypes.INT, 5)
                    .add("noon_sound", SerializableDataTypes.SOUND_EVENT, null)
                    .add("noon_warning", SerializableDataTypes.INT, 600)
                    .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> {
                    NatureReclaimPower power = new NatureReclaimPower(
                        type,
                        entity,
                        data.getInt("cast_ticks"),
                        data.getInt("sound_delay"),
                        data.get("charging_sound"),
                        data.get("finish_sound"),
                        data.get("strain_resource"),
                        data.getInt("noon_strain"),
                        data.get("sun_resource"),
                        data.getInt("noon_sun_cost"),
                        data.get("noon_sound"),
                        data.getInt("noon_warning"));
                    power.setKey(data.get("key"));
                    return power;
                })
                .allowCondition());
    }

    private static void registerSporeHaze() {
        Identifier id = id("spore_haze");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                SporeHazeAction.data(),
                SporeHazeAction::action));
    }

    private static void registerCultivateStone() {
        Identifier id = id("cultivate_stone");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                CultivateStoneAction.data(),
                CultivateStoneAction::action));
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

    private static void registerSurface() {
        Identifier id = id("surface");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                SurfaceAction.data(),
                SurfaceAction::action));
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                SurfaceAction.data(),
                SurfaceAction::condition));
    }

    private static void registerSunIntake() {
        Identifier id = id("sun_intake");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                SunIntakeCondition.data(),
                SunIntakeCondition::condition));
        Identifier maelsBlessing = id("maels_blessing");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            maelsBlessing,
            new ConditionFactory<Entity>(
                maelsBlessing,
                new SerializableData(),
                SunIntakeCondition::maelsBlessingCondition));
        Identifier sunStrain = id("sun_strain");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            sunStrain,
            new ConditionFactory<Entity>(
                sunStrain,
                SunStrainCondition.data(),
                SunStrainCondition::condition));
    }

    private static void registerLaunch() {
        Identifier id = id("launch");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                LaunchAction.data(),
                LaunchAction::action));
    }

    private static void registerGuardianBeam() {
        Identifier id = id("guardian_beam");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                GuardianBeamAction.data(),
                GuardianBeamAction::action));
    }

    private static void registerShulkerBullet() {
        Identifier id = id("shulker_bullet");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                ShulkerBulletAction.data(),
                ShulkerBulletAction::action));
    }

    private static void registerSmallFireball() {
        Identifier id = id("small_fireball");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                SmallFireballAction.data(),
                SmallFireballAction::action));
    }

    private static void registerVenomPoison() {
        Identifier id = id("venom_poison");
        Registry.register(
            ApoliRegistries.ENTITY_ACTION,
            id,
            new ActionFactory<Entity>(
                id,
                VenomPoisonAction.data(),
                VenomPoisonAction::action));
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

    private static void registerPhantomFlight() {
        Identifier id = id("phantom_flight");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData(),
                data -> (type, entity) -> new PhantomFlightPower(type, entity))
                .allowCondition());
    }

    private static void registerKeyGesture() {
        Identifier id = id("key_gesture");
        Registry.register(
            ApoliRegistries.POWER_FACTORY,
            id,
            new PowerFactory<>(
                id,
                new SerializableData()
                    .add("cooldown", SerializableDataTypes.INT, 1)
                    .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                    .add("hold_ticks", SerializableDataTypes.INT, 10)
                    .add("press_action", ApoliDataTypes.ENTITY_ACTION, null)
                    .add("press_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                    .add("hold_action", ApoliDataTypes.ENTITY_ACTION, null)
                    .add("hold_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                    .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                data -> (type, entity) -> {
                    KeyGesturePower power = new KeyGesturePower(
                        type,
                        entity,
                        data.getInt("cooldown"),
                        data.get("hud_render"),
                        data.getInt("hold_ticks"),
                        data.get("press_action"),
                        data.get("press_condition"),
                        data.get("hold_action"),
                        data.get("hold_condition"));
                    Active.Key key = data.get("key");
                    key.continuous = true;
                    power.setKey(key);
                    return power;
                })
                .allowCondition());
    }

    private static void registerResourceFraction() {
        Identifier id = id("resource_fraction");
        Registry.register(
            ApoliRegistries.ENTITY_CONDITION,
            id,
            new ConditionFactory<Entity>(
                id,
                ResourceFractionCondition.data(),
                ResourceFractionCondition::condition));
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
                    .add("block_max_length", SerializableDataTypes.DOUBLE, 24.0)
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
                        data.getDouble("block_max_length"),
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
                    .add("consume_sound_times", SerializableDataTypes.INT, 0)
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
                        data.getInt("consume_sound_times"),
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
