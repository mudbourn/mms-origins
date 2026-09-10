package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents.Allow;
import net.minecraft.class_1297;
import net.minecraft.class_1309;

public class PreventElytraFlightPower extends Power {
   private final Consumer<class_1297> entityAction;

   public PreventElytraFlightPower(PowerType<?> type, class_1309 entity, Consumer<class_1297> entityAction) {
      super(type, entity);
      this.entityAction = entityAction;
   }

   public void executeAction(class_1297 entity) {
      if (this.entityAction != null) {
         this.entityAction.accept(entity);
      }
   }

   public static PowerFactory createFactory() {
      EntityElytraEvents.ALLOW.register((Allow)entity -> !PowerHolderComponent.hasPower(entity, PreventElytraFlightPower.class));
      return new PowerFactory(
            Apoli.identifier("prevent_elytra_flight"),
            new SerializableData().add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (type, player) -> new PreventElytraFlightPower(type, player, (Consumer<class_1297>)data.get("entity_action"))
         )
         .allowCondition();
   }
}
