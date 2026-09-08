package info.mudbourn.mmsorigins.mixin;

import java.util.Collection;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the creative inventory from crashing when two sources contribute the
 * same parent-tab stack. Vanilla asserts on the duplicate; this drops the second
 * one instead, mirroring the standalone Item Split Bug Fix so no extra mod is
 * needed at runtime.
 */
@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public class CreativeTabDuplicateGuardMixin {

    @Shadow
    @Final
    public Collection<ItemStack> tabContents;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$skipDuplicate(
            ItemStack stack,
            CreativeModeTab.TabVisibility visibility,
            CallbackInfo ci) {
        if (visibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY
                && this.tabContents.contains(stack)) {
            ci.cancel();
        }
    }
}
