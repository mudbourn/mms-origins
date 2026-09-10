package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registers the mod's own entities. Only the floran henchman lives here; every
 * other creature the origins touch is vanilla.
 */
public final class MmsEntities {

    public static final EntityType<FloranHenchman> FLORAN_HENCHMAN = register(
        "floran_henchman",
        EntityType.Builder.of(FloranHenchman::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .eyeHeight(1.74f)
            .clientTrackingRange(8));

    private MmsEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(FLORAN_HENCHMAN, FloranHenchman.createAttributes());
    }

    private static EntityType<FloranHenchman> register(String path, EntityType.Builder<FloranHenchman> builder) {
        Identifier id = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, path);
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id,
            builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }
}
