package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kinsmen: a piglin pays out its finer stock to one of its own, and takes gold
 * blocks as readily as ingots.
 *
 * <p>Vanilla rolls {@code minecraft:gameplay/piglin_bartering} for every ingot
 * barter and never trades for a gold block at all. When a bartering piglin has a
 * Kinsmen player within reach, its ingot response is rolled from the mod's own
 * barter table, and a gold block it finishes admiring pays out three distinct
 * goods from that same table instead of being kept, the block being worth far
 * more than a single ingot. The vanilla table and the piglin's dealings with
 * everyone else are left untouched.
 *
 * <p>A piglin that finishes admiring gold with no player within reach, or with a
 * zombified player anywhere within 128 blocks, keeps the gold in hand instead of
 * paying out, so dropping currency and fleeing earns nothing and the sickness
 * spoils trade for all. This holds for every piglin, not just those with a
 * kinsman near.
 */
@Mixin(PiglinAi.class)
public class PiglinBarterMixin {

    @Unique
    private static final double MMS_KIN_RANGE = 16.0;

    @Unique
    private static final double MMS_ZOMBIE_WARD_RANGE = 128.0;

    @Unique
    private static final int MMS_BLOCK_DROP_COUNT = 3;

    @Unique
    private static final int MMS_BLOCK_ROLL_ATTEMPTS = 32;

    @Unique
    private static final ResourceKey<LootTable> MMS_KIN_BARTER =
        ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("mms_origins", "gameplay/piglin_bartering"));

    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$kinsmenLoot(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        Level level = piglin.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Player player = level.getNearestPlayer(piglin, MMS_KIN_RANGE);
        if (player == null || !MmsOriginsPowers.KINSMEN.isActive(player)) {
            return;
        }
        cir.setReturnValue(mmsOrigins$rollKinBarter(serverLevel, piglin));
    }

    // A zombified player anywhere near sours the piglin on trade; it keeps the gold instead of paying out.
    @Unique
    private static boolean mmsOrigins$zombifiedNear(Level level, Piglin piglin) {
        double wardSq = MMS_ZOMBIE_WARD_RANGE * MMS_ZOMBIE_WARD_RANGE;
        for (Player player : level.players()) {
            if (MmsOriginsPowers.ZOMBIFIED.isActive(player) && player.distanceToSqr(piglin) <= wardSq) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$keepGoldWhenAlone(ServerLevel level, Piglin piglin, boolean bartering, CallbackInfo ci) {
        if (!bartering || !piglin.isAdult()) {
            return;
        }
        ItemStack offhand = piglin.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhand.is(Items.GOLD_INGOT) && !offhand.is(Items.GOLD_BLOCK)) {
            return;
        }
        // No one is left to trade with, or a zombified player has spoiled the mood; keep the gold rather than paying out.
        if (level.getNearestPlayer(piglin, MMS_KIN_RANGE) == null || mmsOrigins$zombifiedNear(level, piglin)) {
            ci.cancel();
        }
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$blockBarter(ServerLevel level, Piglin piglin, boolean bartering, CallbackInfo ci) {
        if (!piglin.isAdult()) {
            return;
        }
        ItemStack offhand = piglin.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhand.is(Items.GOLD_BLOCK)) {
            return;
        }
        Player player = level.getNearestPlayer(piglin, MMS_KIN_RANGE);
        if (player == null || !MmsOriginsPowers.KINSMEN.isActive(player)) {
            return;
        }
        // The keep-gold guard already holds the block when a zombified player is near; do not pay over it.
        if (mmsOrigins$zombifiedNear(level, piglin)) {
            return;
        }
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        for (ItemStack loot : mmsOrigins$rollDistinctKinBarter(level, piglin, MMS_BLOCK_DROP_COUNT)) {
            piglin.spawnAtLocation(level, loot);
        }
        ci.cancel();
    }

    @Unique
    private static List<ItemStack> mmsOrigins$rollKinBarter(ServerLevel level, Piglin piglin) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(MMS_KIN_BARTER);
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, piglin)
            .create(LootContextParamSets.PIGLIN_BARTER);
        return table.getRandomItems(params);
    }

    // Rolls the barter table repeatedly, keeping only fresh item types, so a gold block pays out distinct goods.
    @Unique
    private static List<ItemStack> mmsOrigins$rollDistinctKinBarter(ServerLevel level, Piglin piglin, int wanted) {
        List<ItemStack> picks = new ArrayList<>();
        Set<Item> seen = new HashSet<>();
        for (int attempt = 0; attempt < MMS_BLOCK_ROLL_ATTEMPTS && picks.size() < wanted; attempt++) {
            for (ItemStack loot : mmsOrigins$rollKinBarter(level, piglin)) {
                if (picks.size() >= wanted) {
                    break;
                }
                if (!loot.isEmpty() && seen.add(loot.getItem())) {
                    picks.add(loot);
                }
            }
        }
        return picks;
    }
}
