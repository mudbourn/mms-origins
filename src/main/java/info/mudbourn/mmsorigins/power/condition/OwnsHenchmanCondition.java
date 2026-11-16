package info.mudbourn.mmsorigins.power.condition;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

/** Bientity condition that holds when the target is a Grovekeeper summoned by the actor. */
public final class OwnsHenchmanCondition {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static boolean condition(SerializableData.Instance data, Tuple<Entity, Entity> pair) {
        return pair.getB() instanceof FloranHenchman henchman
            && pair.getA() != null
            && henchman.isOwnedBy(pair.getA().getUUID());
    }

    private OwnsHenchmanCondition() {
    }
}
