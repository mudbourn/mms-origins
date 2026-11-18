package info.mudbourn.mmsorigins.power.condition;

import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmsorigins.entity.FloranHenchman;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;

/** Bientity condition that holds when the target is a Grovekeeper summoned by the actor. */
public final class OwnsHenchmanCondition {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        return pair.getSecond() instanceof FloranHenchman henchman
            && pair.getFirst() != null
            && henchman.isOwnedBy(pair.getFirst().getUUID());
    }

    private OwnsHenchmanCondition() {
    }
}
