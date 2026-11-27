package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.LegacyIds;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Resolves a saved origin id that moved into {@code mms_origins}, so a player loads with the
 * origin they had and is not asked to pick again.
 */
@Mixin(OriginRegistry.class)
public abstract class OriginRegistryMigrationMixin {

    @ModifyVariable(
        method = "get(Lnet/minecraft/resources/Identifier;)Lio/github/apace100/origins/origin/Origin;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static Identifier mmsOrigins$migrateGet(Identifier id) {
        return LegacyIds.origin(id);
    }

    @ModifyVariable(
        method = "contains(Lnet/minecraft/resources/Identifier;)Z",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static Identifier mmsOrigins$migrateContains(Identifier id) {
        return LegacyIds.origin(id);
    }
}
