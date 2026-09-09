package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Kinsmen: a piglin bartering near a kinsman reaches for its richer hoard.
 *
 * <p>Regular piglins already barter, so this swaps the loot for the mod's
 * wealthier table whenever an unzombified kinsman is close enough to be the one
 * feeding it gold. Everyone else gets vanilla's response.
 */
@Mixin(net.minecraft.world.entity.monster.piglin.PiglinAi.class)
public class PiglinRicherBarterMixin {

    @Unique
    private static final ResourceKey<LootTable> MMS_PIGLIN_BARTER =
        ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("mms_origins", "gameplay/piglin_bartering"));

    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$richerForKin(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(piglin.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = piglin.getBoundingBox().inflate(6.0);
        boolean kinNear = !level.getEntitiesOfClass(Player.class, area,
            player -> MmsOriginsPowers.KINSMEN.isActive(player) && !MmsOriginsPowers.ZOMBIFIED.isActive(player)).isEmpty();
        if (!kinNear) {
            return;
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(MMS_PIGLIN_BARTER);
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, piglin)
            .create(LootContextParamSets.PIGLIN_BARTER);
        cir.setReturnValue(table.getRandomItems(params));
    }
}
