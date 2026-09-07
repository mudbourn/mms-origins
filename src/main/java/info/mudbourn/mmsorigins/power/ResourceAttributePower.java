package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Applies a transient attribute modifier scaled by another power's resource
 * value, refreshed every tick. Reproduces the newer Apoli resource-backed
 * attribute modifier that Apoli-Legacy lacks; the shulk shell shield uses it to
 * turn stored shield into armor.
 */
public final class ResourceAttributePower extends Power {

    private final Holder<Attribute> attribute;
    private final PowerType<?> resource;
    private final float perPoint;
    private final AttributeModifier.Operation operation;
    private final Identifier modifierId;

    public ResourceAttributePower(
            PowerType<?> type,
            LivingEntity entity,
            Holder<Attribute> attribute,
            PowerType<?> resource,
            float perPoint,
            AttributeModifier.Operation operation) {
        super(type, entity);
        this.attribute = attribute;
        this.resource = resource;
        this.perPoint = perPoint;
        this.operation = operation;
        this.modifierId = type.getIdentifier();
        this.setTicking();
    }

    @Override
    public void tick() {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(modifierId);
        double amount = resourceValue() * perPoint;
        if (amount != 0) {
            instance.addTransientModifier(new AttributeModifier(modifierId, amount, operation));
        }
    }

    @Override
    public void onLost() {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }

    private int resourceValue() {
        Power power = resource.get(entity);
        if (power instanceof VariableIntPower variable) {
            return variable.getValue();
        }
        return 0;
    }
}
