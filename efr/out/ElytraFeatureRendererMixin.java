package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.minecraft.class_10034;
import net.minecraft.class_10191;
import net.minecraft.class_10192;
import net.minecraft.class_1304;
import net.minecraft.class_2960;
import net.minecraft.class_3417;
import net.minecraft.class_5321;
import net.minecraft.class_979;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_979.class)
public class ElytraFeatureRendererMixin {
   @ModifyExpressionValue(
      method = "method_17161",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_58694(Lnet/minecraft/class_9331;)Ljava/lang/Object;")
   )
   private Object modifyEquippedStackToElytra(Object original, @Local(argsOnly = true) class_10034 renderState) {
      if (!renderState.field_53333) {
         for (ElytraFlightPower power : PowerHolderComponent.getPowers(renderState, ElytraFlightPower.class)) {
            if (power.shouldRenderElytra()) {
               class_10192 cached = ((ApoliLivingEntityRenderState)renderState).apoli$getCachedEquippable();
               if (cached != null && (!cached.comp_3176().isPresent() || ((class_5321)cached.comp_3176().orElseThrow()).equals(class_10191.field_54142))) {
                  return cached;
               }

               class_10192 equippable = class_10192.method_64202(class_1304.field_6174)
                  .method_64205(class_3417.field_14966)
                  .method_64204(class_10191.field_54142)
                  .method_64210(false)
                  .method_64203();
               ((ApoliLivingEntityRenderState)renderState).apoli$setCachedEquippable(equippable);
               return equippable;
            }
         }
      }

      return original;
   }

   @ModifyExpressionValue(
      method = "method_17161",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_979;method_64084(Lnet/minecraft/class_10034;)Lnet/minecraft/class_2960;")
   )
   private class_2960 modifyEntityElytraTextureToPower(class_2960 original, @Local(argsOnly = true) class_10034 renderState) {
      if (!renderState.field_53333) {
         for (ElytraFlightPower power : PowerHolderComponent.getPowers(renderState, ElytraFlightPower.class)) {
            if (power.shouldRenderElytra() && power.getTextureLocation() != null) {
               return power.getTextureLocation();
            }
         }
      }

      return original;
   }
}
