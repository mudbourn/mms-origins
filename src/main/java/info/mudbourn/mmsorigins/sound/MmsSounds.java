package info.mudbourn.mmsorigins.sound;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Registers the mod's own sound events so datapack {@code play_sound} actions can
 * resolve them. Apoli looks a sound up in the registry, so a resource-pack
 * {@code sounds.json} entry alone is not enough; the event must exist here too.
 */
public final class MmsSounds {

    public static final SoundEvent FLANDRE_HURT = register("flandre.hurt");
    public static final SoundEvent FLANDRE_DEATH = register("flandre.death");

    private MmsSounds() {
    }

    public static void register() {
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
