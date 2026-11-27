package info.mudbourn.mmsorigins.mixin.originsclasses;

import io.github.apace100.originsclasses.power.ClassesPowerTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Points every Origins Classes power hook at the {@code mms_origins} copy of that power, so the
 * class powers keep working from the {@code mms_origins} class origins.
 */
@Mixin(ClassesPowerTypes.class)
public abstract class ClassesPowerTypesMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resources/Identifier;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
        ),
        index = 0
    )
    private static String mmsOrigins$classPowerNamespace(String namespace) {
        return "mms_origins";
    }
}
