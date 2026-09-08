package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards Apoli-Legacy's loot replacement against a null table id, which some
 * 1.21.11 loot contexts pass. Without this the raw {@code id.toString()} throws
 * and aborts the whole loot roll, so an avian with green thumb gets no block
 * drops at all.
 */
@Mixin(ReplaceLootTablePower.class)
public class ReplaceLootTablePowerGuardMixin {

    @Inject(method = "hasReplacement", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$skipNullTable(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (id == null) {
            cir.setReturnValue(false);
        }
    }
}
