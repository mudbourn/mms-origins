package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registers the mod's own entities: the floran henchman and the Blazeborn's wildfire
 * shield. Every other creature the origins touch is vanilla.
 */
public final class MmsEntities {

    public static final EntityType<FloranHenchman> FLORAN_HENCHMAN = register(
        "floran_henchman",
        EntityType.Builder.of(FloranHenchman::new, MobCategory.MISC)
            .sized(0.9f, 2.7f)
            .eyeHeight(2.3f)
            .clientTrackingRange(8));

    public static final EntityType<WildfireShield> WILDFIRE_SHIELD = register(
        "wildfire_shield",
        EntityType.Builder.<WildfireShield>of(WildfireShield::new, MobCategory.MISC)
            .sized(0.75f, 0.75f)
            .clientTrackingRange(4)
            .updateInterval(10));

    private MmsEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(FLORAN_HENCHMAN, FloranHenchman.createAttributes());
    }

    private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
        Identifier id = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, path);
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id,
            builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }
}
