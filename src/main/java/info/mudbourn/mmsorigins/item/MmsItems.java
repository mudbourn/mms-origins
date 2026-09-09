package info.mudbourn.mmsorigins.item;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Registers the Umbrella item and drives its weather wear, ported from the
 * Origins: Umbrellas mod.
 *
 * <p>The umbrella is a dyeable, single-stack tool with high durability. Holding
 * one shields the elemental origins from the sky: the immunities themselves are
 * datapack conditions on {@code water_vulnerability} and {@code burn_in_daylight}
 * that test for a held umbrella, so the only Java concern here is the item and
 * its durability. Shielding a hand wears the umbrella down and eventually breaks
 * it: direct sunlight spends its full durability over about fifteen minutes, and
 * rain wears it a little faster. Under cover the wear simply stops; it does not
 * mend back.
 */
public final class MmsItems {

    private static final int MAX_DAMAGE = 1200;
    private static final int LEATHER_COLOR = 0xA06540;
    private static final int SUN_INTERVAL = 15;
    private static final int RAIN_INTERVAL = 10;

    public static final Item UMBRELLA = register("umbrella");

    private MmsItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(UMBRELLA));
        ServerTickEvents.END_WORLD_TICK.register(MmsItems::wearLevel);
    }

    private static Item register(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, name));
        Item.Properties properties = new Item.Properties()
                .stacksTo(1)
                .durability(MAX_DAMAGE)
                .component(DataComponents.DYED_COLOR, new DyedItemColor(LEATHER_COLOR))
                .setId(key);
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
    }

    private static void wearLevel(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            wear(level, player);
        }
    }

    private static void wear(ServerLevel level, ServerPlayer player) {
        int age = player.tickCount;
        BlockPos pos = player.blockPosition();
        if (level.isRainingAt(pos)) {
            if (age % RAIN_INTERVAL == 0) {
                shield(player, EquipmentSlot.MAINHAND);
                shield(player, EquipmentSlot.OFFHAND);
            }
            return;
        }
        if (level.isBrightOutside() && !player.isInvisible() && level.canSeeSky(pos)) {
            if (age % SUN_INTERVAL == 0) {
                shield(player, EquipmentSlot.MAINHAND);
                shield(player, EquipmentSlot.OFFHAND);
            }
        }
    }

    private static void shield(ServerPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!stack.is(UMBRELLA)) {
            return;
        }
        stack.hurtAndBreak(1, player, slot);
    }
}
