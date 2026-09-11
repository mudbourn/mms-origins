package info.mudbourn.mmsorigins.power;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * The nature magic that unmakes a crafted item back into its parts. It finds the
 * crafting recipe whose result matches the off-hand stack, refunds one of each
 * ingredient per full craft, and writes any enchantments onto books drawn from
 * the caster's own supply. Apoli has no power for this, so it is Java.
 */
public final class NatureReclaim {

    /** The outcome of one reclaim attempt, so the caller can voice the reason. */
    public enum Result {
        SUCCESS,
        NO_RECIPE,
        NOT_ENOUGH
    }

    public static Result reclaim(Player player, ServerLevel level) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty()) {
            return Result.NO_RECIPE;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return Result.NO_RECIPE;
        }
        RecipeManager recipes = server.getRecipeManager();
        HolderLookup.Provider provider = level.registryAccess();
        Item target = offhand.getItem();
        CraftingRecipe match = null;
        ItemStack result = null;
        List<Ingredient> ingredients = null;
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe) || recipe.isSpecial()) {
                continue;
            }
            ItemStack assembled = recipe.assemble(CraftingInput.EMPTY, provider);
            if (assembled.isEmpty() || !assembled.is(target)) {
                continue;
            }
            List<Ingredient> parts = recipe.placementInfo().ingredients();
            if (parts.isEmpty()) {
                continue;
            }
            match = recipe;
            result = assembled;
            ingredients = parts;
            break;
        }
        if (match == null) {
            return Result.NO_RECIPE;
        }
        int crafts = offhand.getCount() / result.getCount();
        if (crafts <= 0) {
            return Result.NOT_ENOUGH;
        }
        List<ItemStack> refunds = new ArrayList<>();
        collectEnchantmentBooks(player, offhand, refunds);
        for (Ingredient ingredient : ingredients) {
            Optional<Holder<Item>> pick = ingredient.items().findFirst();
            pick.ifPresent(item -> refunds.add(new ItemStack(item, crafts)));
        }
        offhand.shrink(crafts * result.getCount());
        for (ItemStack refund : refunds) {
            grant(player, refund);
        }
        return Result.SUCCESS;
    }

    private static void collectEnchantmentBooks(Player player, ItemStack offhand, List<ItemStack> refunds) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(offhand);
        if (enchantments.isEmpty()) {
            return;
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (!consumeBook(player)) {
                break;
            }
            refunds.add(EnchantmentHelper.createBook(
                new EnchantmentInstance(entry.getKey(), entry.getIntValue())));
        }
    }

    private static boolean consumeBook(Player player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(Items.BOOK)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void grant(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private NatureReclaim() {
    }
}
