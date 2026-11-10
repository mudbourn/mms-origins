package info.mudbourn.mmsorigins.compat;

import info.mudbourn.mmsorigins.MmsOrigins;
import java.util.Map;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public final class FishOfThievesFairyFishing {
    private static final ResourceKey<LootTable> FAIRY_FISHING = ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "gameplay/fairy_fishing")
    );

    private FishOfThievesFairyFishing() {
    }

    // Gives the fairy fishing table the bait pool Fish of Thieves adds to vanilla fishing.
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!key.equals(FAIRY_FISHING)) {
                return;
            }
            BiFunction<LootPool.Builder, HolderLookup.Provider, LootPool.Builder> baitPool = findBaitPool();
            if (baitPool != null) {
                tableBuilder.pool(baitPool.apply(LootPool.lootPool(), registries).build());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static BiFunction<LootPool.Builder, HolderLookup.Provider, LootPool.Builder> findBaitPool() {
        try {
            Map<ResourceKey<LootTable>, BiFunction<LootPool.Builder, HolderLookup.Provider, LootPool.Builder>> injected =
                (Map<ResourceKey<LootTable>, BiFunction<LootPool.Builder, HolderLookup.Provider, LootPool.Builder>>) Class
                    .forName("com.stevekung.fishofthieves.loot.FOTLootManager")
                    .getMethod("getInjectedLootTableMap")
                    .invoke(null);
            BiFunction<LootPool.Builder, HolderLookup.Provider, LootPool.Builder> baitPool = injected.get(BuiltInLootTables.FISHING);
            if (baitPool == null) {
                MmsOrigins.LOGGER.warn("Fish of Thieves no longer injects into vanilla fishing; fairy fishing left unchanged.");
            }
            return baitPool;
        } catch (ReflectiveOperationException | ClassCastException e) {
            MmsOrigins.LOGGER.warn("Could not read Fish of Thieves fishing loot; fairy fishing left unchanged.", e);
            return null;
        }
    }
}
