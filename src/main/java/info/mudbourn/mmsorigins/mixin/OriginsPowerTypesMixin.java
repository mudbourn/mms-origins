package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Points the Origins water and cobweb hooks at the {@code mms_origins} copies of those powers,
 * so the origins that carry them keep breathing, swimming and walking through webs as before.
 */
@Mixin(OriginsPowerTypes.class)
public abstract class OriginsPowerTypesMixin {

    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lio/github/apace100/origins/Origins;identifier(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
        )
    )
    private static Identifier mmsOrigins$repointPower(String path, Operation<Identifier> original) {
        return switch (path) {
            case "like_water",
                 "water_breathing",
                 "conduit_power_on_land",
                 "master_of_webs_no_slowdown" -> Identifier.fromNamespaceAndPath("mms_origins", path);
            default -> original.call(path);
        };
    }
}
