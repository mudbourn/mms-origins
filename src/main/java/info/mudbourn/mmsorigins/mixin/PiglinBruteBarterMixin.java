package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
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
 * <p>A brute normally ignores gold entirely. Each server step this looks for a
 * gold ingot or gold block thrown near the brute by an arms-dealing piglin,
 * takes one, and rolls the brute's own bastion-gear table. A block always pays
 * out; an ingot is stiffed three times in ten, the brute pocketing the gold. Any
 * item that is not a gold ingot or block is ignored.
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
    private int mmsOrigins$barterCooldown;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void mmsOrigins$barterForGear(ServerLevel level, CallbackInfo ci) {
        PiglinBrute brute = (PiglinBrute) (Object) this;
        if (this.mmsOrigins$barterCooldown > 0) {
            this.mmsOrigins$barterCooldown--;
            return;
        }
        AABB reach = brute.getBoundingBox().inflate(4.0);
        List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, reach);
        for (ItemEntity dropped : nearby) {
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
            this.mmsOrigins$barterCooldown = 40;
            boolean pays = block || brute.getRandom().nextFloat() >= MMS_STIFF_CHANCE;
            brute.playSound(pays ? SoundEvents.PIGLIN_CELEBRATE : SoundEvents.PIGLIN_JEALOUS, 1.0F, 1.0F);
            if (pays) {
                LootTable table = level.getServer().reloadableRegistries().getLootTable(MMS_BRUTE_BARTER);
                LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, brute)
                    .create(LootContextParamSets.PIGLIN_BARTER);
                for (ItemStack loot : table.getRandomItems(params)) {
                    brute.spawnAtLocation(level, loot);
                }
            }
            return;
        }
    }
}
