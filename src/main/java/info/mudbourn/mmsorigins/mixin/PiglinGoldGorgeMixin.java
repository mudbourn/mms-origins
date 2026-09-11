package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a piglin eat golden food on a full belly so the zombification cure is
 * always reachable. Vanilla gates a consumable on the eater being hungry unless
 * the food is {@code canAlwaysEat}, which golden carrots are not, so a stuffed
 * piglin cannot cure. This lifts that one gate for the gold the cure runs on.
 */
@Mixin(Consumable.class)
public class PiglinGoldGorgeMixin {

    private static final TagKey<Item> GOLDEN_FOOD =
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("originstweaks", "golden_food"));

    @Inject(
        method = "canConsume(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("RETURN"),
        cancellable = true)
    private void mmsOrigins$gorgeGold(
            LivingEntity entity,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !(entity instanceof Player player)) {
            return;
        }
        if (MmsOriginsPowers.ZOMBIE_METER.isActive(player) && stack.is(GOLDEN_FOOD)) {
            cir.setReturnValue(true);
        }
    }
}
