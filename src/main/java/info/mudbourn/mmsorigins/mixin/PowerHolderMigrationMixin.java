package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import info.mudbourn.mmsorigins.LegacyIds;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Renames a saved power and its saved sources to their {@code mms_origins} ids while the power
 * holder reads a player, so resource values, cooldowns and inventories carry over and no power
 * stays tied to an origin id that no longer exists.
 */
@Mixin(value = PowerHolderComponentImpl.class, remap = false)
public abstract class PowerHolderMigrationMixin {

    @ModifyExpressionValue(
        method = "fromTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resources/Identifier;tryParse(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
        )
    )
    private Identifier mmsOrigins$migratePower(Identifier id) {
        return LegacyIds.power(id);
    }

    @ModifyArg(
        method = "fromTag",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        ),
        index = 0
    )
    private Object mmsOrigins$migrateSource(Object source) {
        return source instanceof Identifier id ? LegacyIds.origin(id) : source;
    }
}
