package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.LegacyIds;
import io.github.apace100.origins.origin.OriginLayers;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Resolves a saved layer id that moved into {@code mms_origins}, so the layer a player chose in
 * still holds their pick.
 */
@Mixin(OriginLayers.class)
public abstract class OriginLayersMigrationMixin {

    @ModifyVariable(
        method = "getLayer(Lnet/minecraft/resources/Identifier;)Lio/github/apace100/origins/origin/OriginLayer;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static Identifier mmsOrigins$migrateLayer(Identifier id) {
        return LegacyIds.layer(id);
    }
}
