package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.power.condition.SunIntakeCondition;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Grants armor that follows the sun. A holder with enough of the charge resource
 * keeps a base armor all night, and the armor climbs along the sun curve to its
 * peak at noon.
 */
public final class SunArmorPower extends Power {

    private static final double EPSILON = 0.005;

    private final PowerType<?> resource;
    private final double nightArmor;
    private final double peakArmor;
    private final double threshold;
    private final Identifier modifierId;

    public SunArmorPower(
            PowerType<?> type,
            LivingEntity entity,
            PowerType<?> resource,
            double nightArmor,
            double peakArmor,
            double threshold) {
        super(type, entity);
        this.resource = resource;
        this.nightArmor = nightArmor;
        this.peakArmor = peakArmor;
        this.threshold = threshold;
        this.modifierId = type.getIdentifier();
        this.setTicking();
    }

    @Override
    public void tick() {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        double base = charged() ? nightArmor : 0.0;
        double amount = base + (peakArmor - base) * SunIntakeCondition.strength(entity);
        AttributeModifier current = armor.getModifier(modifierId);
        if (current != null && Math.abs(current.amount() - amount) < EPSILON) {
            return;
        }
        armor.removeModifier(modifierId);
        if (amount > 0.0) {
            armor.addTransientModifier(
                new AttributeModifier(modifierId, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void onLost() {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(modifierId);
        }
    }

    private boolean charged() {
        return resource.get(entity) instanceof VariableIntPower charge
            && charge.getValue() >= threshold * charge.getMax();
    }
}
