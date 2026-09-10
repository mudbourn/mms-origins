package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import info.mudbourn.mmsorigins.entity.MmsEntities;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Summons a single floran henchman, letting the caster ration their pack one
 * press at a time. Each summon fills one empty slot up to the cap and spends a
 * point of the charge resource, which the henchman refunds when it leaves.
 */
public final class SummonHenchmenAction {

    private static final double SEARCH_RADIUS = 96.0;
    private static final double RING_RADIUS = 1.5;

    public static SerializableData data() {
        return new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE, null)
            .add("max_active", SerializableDataTypes.INT, 2);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity owner) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        int maxActive = data.getInt("max_active");
        AABB search = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<FloranHenchman> existing =
            level.getEntities(MmsEntities.FLORAN_HENCHMAN, search, henchman -> owner.equals(henchman.getOwner()));
        if (existing.size() >= maxActive) {
            return;
        }
        VariableIntPower charge = FloranHenchman.chargeOf(owner, data.get("resource"));
        if (charge != null && charge.getValue() <= 0) {
            return;
        }
        RandomSource random = owner.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double x = owner.getX() + Math.cos(angle) * RING_RADIUS;
        double z = owner.getZ() + Math.sin(angle) * RING_RADIUS;
        FloranHenchman henchman = new FloranHenchman(MmsEntities.FLORAN_HENCHMAN, level);
        henchman.snapTo(x, owner.getY(), z, owner.getYRot(), 0.0f);
        henchman.setOwner(owner);
        level.addFreshEntity(henchman);
        if (charge != null) {
            charge.decrement();
            PowerHolderComponent.syncPower(owner, charge.getType());
        }
    }

    private SummonHenchmenAction() {
    }
}
