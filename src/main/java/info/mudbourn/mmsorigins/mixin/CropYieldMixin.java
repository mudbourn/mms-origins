package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Avian green thumb crop yield, generic across vanilla and modded farming.
 *
 * <p>The old per-block loot table swaps only reached the crops that shipped a
 * hand-written table. This instead boosts the drops of any block in the
 * {@code mms_origins:hoe_crops} tag when a green thumb harvests it with a hoe,
 * so mods that add their crops to that tag (or the conventional {@code c:crops})
 * are covered without a file each. Only the non-seed yield is multiplied, and a
 * carrot or melon harvest has a flat chance to also drop its gold variant.
 * Oak and dark oak leaves torn down with a hoe carry the same flat chance at a
 * golden apple.
 */
@Mixin(Block.class)
public abstract class CropYieldMixin {

    private static final Identifier GREEN_THUMB =
        Identifier.fromNamespaceAndPath("originstweaks", "green_thumb");
    private static final TagKey<Block> HOE_CROPS =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mms_origins", "hoe_crops"));
    private static final double GOLD_CHANCE = 0.12;

    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at = @At("RETURN"),
        cancellable = true)
    private static void mmsOrigins$greenThumbYield(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            BlockEntity blockEntity,
            Entity breaker,
            ItemStack tool,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(breaker instanceof Player player) || !tool.is(ItemTags.HOES)) {
            return;
        }
        boolean crop = state.is(HOE_CROPS);
        boolean goldenLeaf = state.is(Blocks.OAK_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES);
        if ((!crop && !goldenLeaf) || !mmsOrigins$hasGreenThumb(player)) {
            return;
        }
        List<ItemStack> drops = cir.getReturnValue();
        List<ItemStack> boosted = new ArrayList<>(drops.size() + 2);
        RandomSource random = level.getRandom();
        if (crop) {
            for (ItemStack stack : drops) {
                if (stack.isEmpty() || mmsOrigins$isSeed(stack)) {
                    boosted.add(stack);
                    continue;
                }
                ItemStack extra = stack.copy();
                extra.setCount(stack.getCount() + 1 + random.nextInt(2));
                boosted.add(extra);
                Item gold = mmsOrigins$goldVariant(stack.getItem());
                if (gold != null && random.nextDouble() < GOLD_CHANCE) {
                    boosted.add(new ItemStack(gold));
                }
            }
        } else {
            boosted.addAll(drops);
            if (random.nextDouble() < GOLD_CHANCE) {
                boosted.add(new ItemStack(Items.GOLDEN_APPLE));
            }
        }
        cir.setReturnValue(boosted);
    }

    private static boolean mmsOrigins$hasGreenThumb(Player player) {
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return false;
        }
        for (PowerType<?> type : component.getPowerTypes(true)) {
            if (GREEN_THUMB.equals(type.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private static boolean mmsOrigins$isSeed(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("seed");
    }

    private static Item mmsOrigins$goldVariant(Item item) {
        if (item == Items.CARROT) {
            return Items.GOLDEN_CARROT;
        }
        if (item == Items.MELON_SLICE) {
            return Items.GLISTERING_MELON_SLICE;
        }
        return null;
    }
}
