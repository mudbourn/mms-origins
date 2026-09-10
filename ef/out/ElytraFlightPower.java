package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents.Custom;
import net.minecraft.class_1309;
import net.minecraft.class_2960;

public class ElytraFlightPower extends Power {
   private final boolean renderElytra;
   private final class_2960 textureLocation;

   public ElytraFlightPower(PowerType<?> type, class_1309 entity, boolean renderElytra, class_2960 textureLocation) {
      super(type, entity);
      this.renderElytra = renderElytra;
      this.textureLocation = textureLocation;
   }

   public boolean shouldRenderElytra() {
      return this.renderElytra;
   }

   public class_2960 getTextureLocation() {
      return this.textureLocation;
   }

   public static PowerFactory createFactory() {
      EntityElytraEvents.CUSTOM.register((Custom)(livingEntity, b) -> PowerHolderComponent.hasPower(livingEntity, ElytraFlightPower.class));
      return new PowerFactory(
            Apoli.identifier("elytra_flight"),
            new SerializableData().add("render_elytra", SerializableDataTypes.BOOLEAN).add("texture_location", SerializableDataTypes.IDENTIFIER, null),
            data -> (type, player) -> new ElytraFlightPower(type, player, data.getBoolean("render_elytra"), data.getId("texture_location"))
         )
         .allowCondition();
   }
}
