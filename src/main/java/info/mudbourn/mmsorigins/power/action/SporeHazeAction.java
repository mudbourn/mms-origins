package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.SporeHaze;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Looses the truffle's mind haze where the caster stands. Run as an entity
 * action; anything other than a player does nothing.
 */
public final class SporeHazeAction {

    public static SerializableData data() {
        return new SerializableData();
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            SporeHaze.release(player);
        }
    }

    private SporeHazeAction() {
    }
}
