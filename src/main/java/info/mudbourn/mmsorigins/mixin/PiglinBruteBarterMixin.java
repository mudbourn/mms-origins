package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Arms Dealing: piglin brutes learn to barter, which vanilla never lets them do.
 *
 * <p>A brute normally ignores gold entirely. When an arms-dealing piglin drops a
 * gold ingot or block nearby, the brute walks over to the nearest such piece and,
 * once within two blocks, takes it into its off hand and admires it for a spell,
 * the way a common piglin would, before paying out from its bastion-gear table.
 * An ingot pays half the time and a block a little more often, so a block is a
 * touch better as well as being worth more, without leaving ingots pointless.
 * Anything that is not a gold ingot or block is ignored. A zombified player within
 * 128 blocks freezes the brute's dealing entirely, so none may trade while the
 * sickness is in the air.
 *
 * <p>The admire timer does not survive a save, but the held gold does, so a brute
 * reloaded mid-admire would hold its gold forever with no timer left to finish it.
 * A brute found holding gold with no timer resumes the admire, which lets the
 * barter finish instead of leaving it staring at the piece.
 */
@Mixin(PiglinBrute.class)
public class PiglinBruteBarterMixin {

    @Unique
    private static final ResourceKey<LootTable> MMS_BRUTE_BARTER =
        ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("mms_origins", "gameplay/brute_bartering"));

    @Unique
    private static final float MMS_INGOT_STIFF_CHANCE = 0.50F;

    @Unique
    private static final float MMS_BLOCK_STIFF_CHANCE = 0.45F;

    @Unique
    private static final int MMS_ADMIRE_TICKS = 120;

    @Unique
    private static final double MMS_ZOMBIE_WARD_RANGE = 128.0;

    @Unique
    private static final double MMS_DETECT_RANGE = 12.0;

    @Unique
    private static final double MMS_PICKUP_RANGE = 2.0;

    @Unique
    private static final double MMS_WALK_SPEED = 1.0;

    @Unique
    private int mmsOrigins$admireTicks;

    @Unique
    private boolean mmsOrigins$admirePays;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void mmsOrigins$barterForGear(ServerLevel level, CallbackInfo ci) {
        PiglinBrute brute = (PiglinBrute) (Object) this;
        // A zombified player within sight freezes all dealing: no pickup, and no admire pays out until they leave.
        if (mmsOrigins$zombifiedNear(level, brute)) {
            return;
        }
        if (this.mmsOrigins$admireTicks > 0) {
            this.mmsOrigins$admireTicks--;
            if (this.mmsOrigins$admireTicks == 0) {
                mmsOrigins$finishBarter(level, brute);
            }
            return;
        }
        ItemStack held = brute.getItemInHand(InteractionHand.OFF_HAND);
        boolean heldBlock = held.is(Items.GOLD_BLOCK);
        if (heldBlock || held.is(Items.GOLD_INGOT)) {
            this.mmsOrigins$admirePays = mmsOrigins$rollPays(brute, heldBlock);
            this.mmsOrigins$admireTicks = MMS_ADMIRE_TICKS;
            return;
        }
        ItemEntity target = mmsOrigins$nearestGold(level, brute);
        if (target == null) {
            return;
        }
        // Walk up to the gold first; a brute only takes it once it is close enough to reach down for it.
        if (brute.distanceToSqr(target) > MMS_PICKUP_RANGE * MMS_PICKUP_RANGE) {
            brute.getNavigation().moveTo(target, MMS_WALK_SPEED);
            return;
        }
        ItemStack stack = target.getItem();
        boolean block = stack.is(Items.GOLD_BLOCK);
        stack.shrink(1);
        if (stack.isEmpty()) {
            target.discard();
        } else {
            target.setItem(stack);
        }
        brute.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(block ? Items.GOLD_BLOCK : Items.GOLD_INGOT));
        brute.playSound(SoundEvents.PIGLIN_ADMIRING_ITEM, 1.0F, 1.0F);
        this.mmsOrigins$admirePays = mmsOrigins$rollPays(brute, block);
        this.mmsOrigins$admireTicks = MMS_ADMIRE_TICKS;
    }

    // The closest gold ingot or block an arms dealer has dropped in reach, or null when none is on offer.
    @Unique
    private static ItemEntity mmsOrigins$nearestGold(ServerLevel level, PiglinBrute brute) {
        AABB reach = brute.getBoundingBox().inflate(MMS_DETECT_RANGE);
        ItemEntity nearest = null;
        double nearestSq = Double.MAX_VALUE;
        for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, reach)) {
            ItemStack stack = dropped.getItem();
            if (!stack.is(Items.GOLD_BLOCK) && !stack.is(Items.GOLD_INGOT)) {
                continue;
            }
            if (!(dropped.getOwner() instanceof Player player) || !MmsOriginsPowers.ARMS_DEALING.isActive(player)) {
                continue;
            }
            double distSq = brute.distanceToSqr(dropped);
            if (distSq < nearestSq) {
                nearest = dropped;
                nearestSq = distSq;
            }
        }
        return nearest;
    }

    @Unique
    private static boolean mmsOrigins$zombifiedNear(ServerLevel level, PiglinBrute brute) {
        double wardSq = MMS_ZOMBIE_WARD_RANGE * MMS_ZOMBIE_WARD_RANGE;
        for (Player player : level.players()) {
            if (MmsOriginsPowers.ZOMBIFIED.isActive(player) && player.distanceToSqr(brute) <= wardSq) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean mmsOrigins$rollPays(PiglinBrute brute, boolean block) {
        float stiff = block ? MMS_BLOCK_STIFF_CHANCE : MMS_INGOT_STIFF_CHANCE;
        return brute.getRandom().nextFloat() >= stiff;
    }

    @Unique
    private void mmsOrigins$finishBarter(ServerLevel level, PiglinBrute brute) {
        brute.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        brute.playSound(this.mmsOrigins$admirePays ? SoundEvents.PIGLIN_CELEBRATE : SoundEvents.PIGLIN_JEALOUS, 1.0F, 1.0F);
        if (!this.mmsOrigins$admirePays) {
            return;
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(MMS_BRUTE_BARTER);
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, brute)
            .create(LootContextParamSets.PIGLIN_BARTER);
        for (ItemStack loot : table.getRandomItems(params)) {
            brute.spawnAtLocation(level, loot);
        }
    }
}
