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

import java.util.List;

/**
 * Arms Dealing: piglin brutes learn to barter, which vanilla never lets them do.
 *
 * <p>A brute normally ignores gold entirely. When an arms-dealing piglin drops a
 * gold ingot or block within reach, the brute takes one into its off hand and
 * admires it for a spell, the way a common piglin would, before paying out from
 * its bastion-gear table. A block always pays; an ingot is stiffed three times in
 * ten, the brute pocketing the gold. Anything that is not a gold ingot or block
 * is ignored.
 */
@Mixin(PiglinBrute.class)
public class PiglinBruteBarterMixin {

    @Unique
    private static final ResourceKey<LootTable> MMS_BRUTE_BARTER =
        ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("mms_origins", "gameplay/brute_bartering"));

    @Unique
    private static final float MMS_STIFF_CHANCE = 0.30F;

    @Unique
    private static final int MMS_ADMIRE_TICKS = 120;

    @Unique
    private int mmsOrigins$admireTicks;

    @Unique
    private boolean mmsOrigins$admirePays;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void mmsOrigins$barterForGear(ServerLevel level, CallbackInfo ci) {
        PiglinBrute brute = (PiglinBrute) (Object) this;
        if (this.mmsOrigins$admireTicks > 0) {
            this.mmsOrigins$admireTicks--;
            if (this.mmsOrigins$admireTicks == 0) {
                mmsOrigins$finishBarter(level, brute);
            }
            return;
        }
        AABB reach = brute.getBoundingBox().inflate(4.0);
        for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, reach)) {
            ItemStack stack = dropped.getItem();
            boolean block = stack.is(Items.GOLD_BLOCK);
            boolean ingot = stack.is(Items.GOLD_INGOT);
            if (!block && !ingot) {
                continue;
            }
            if (!(dropped.getOwner() instanceof Player player) || !MmsOriginsPowers.ARMS_DEALING.isActive(player)) {
                continue;
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                dropped.discard();
            } else {
                dropped.setItem(stack);
            }
            brute.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(block ? Items.GOLD_BLOCK : Items.GOLD_INGOT));
            brute.playSound(SoundEvents.PIGLIN_ADMIRING_ITEM, 1.0F, 1.0F);
            this.mmsOrigins$admirePays = block || brute.getRandom().nextFloat() >= MMS_STIFF_CHANCE;
            this.mmsOrigins$admireTicks = MMS_ADMIRE_TICKS;
            return;
        }
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
