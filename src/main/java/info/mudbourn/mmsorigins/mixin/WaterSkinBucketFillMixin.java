package info.mudbourn.mmsorigins.mixin;

import com.github.thedeathlycow.scorchful.item.WaterSkinItem;
import com.github.thedeathlycow.scorchful.item.component.DrinkContainer;
import com.github.thedeathlycow.scorchful.registry.SSoundEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a waterskin be filled from a water bucket inside the inventory by
 * right-clicking one onto the other, the way bundles take items. A bucket
 * fills two waterskins before it empties.
 */
@Mixin(Item.class)
public class WaterSkinBucketFillMixin {

    @Unique
    private static final String FILLS_KEY = "mms_origins_waterskin_fills";

    @Unique
    private static final int FILLS_PER_BUCKET = 2;

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$skinOntoBucket(
            ItemStack stack,
            Slot slot,
            ClickAction action,
            Player player,
            CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof WaterSkinItem) || action != ClickAction.SECONDARY) {
            return;
        }
        ItemStack bucket = mmsOrigins$fill(stack, slot.getItem(), player);
        if (bucket != null) {
            slot.set(bucket);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$bucketOntoSkin(
            ItemStack stack,
            ItemStack other,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess access,
            CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof WaterSkinItem) || action != ClickAction.SECONDARY) {
            return;
        }
        ItemStack bucket = mmsOrigins$fill(stack, other, player);
        if (bucket != null) {
            slot.setChanged();
            access.set(bucket);
            cir.setReturnValue(true);
        }
    }

    /**
     * Fills the waterskin from the bucket and returns what the bucket becomes,
     * or null when the pair cannot fill.
     */
    @Unique
    private static ItemStack mmsOrigins$fill(ItemStack skin, ItemStack bucket, Player player) {
        if (!bucket.is(Items.WATER_BUCKET) || WaterSkinItem.getContainer(skin).isFull()) {
            return null;
        }
        DrinkContainer.fillCompletely(skin);
        player.playSound(SSoundEvents.ITEM_WATER_SKIN_FILL, 1.0F, 1.0F);
        int fills = bucket.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getIntOr(FILLS_KEY, 0) + 1;
        if (fills >= FILLS_PER_BUCKET) {
            return new ItemStack(Items.BUCKET);
        }
        ItemStack result = bucket.copy();
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.putInt(FILLS_KEY, fills));
        return result;
    }
}
