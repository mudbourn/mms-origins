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

    public static final SoundEvent BEASTFOLK_HURT = register("beastfolk.hurt");
    public static final SoundEvent BEASTFOLK_DEATH = register("beastfolk.death");
    public static final SoundEvent BEASTFOLK_COLLAR_HURT = register("beastfolk.collar_hurt");
    public static final SoundEvent BEASTFOLK_COLLAR_DEATH = register("beastfolk.collar_death");
    public static final SoundEvent BEASTFOLK_FOX_HURT = register("beastfolk.fox_hurt");
    public static final SoundEvent BEASTFOLK_FOX_DEATH = register("beastfolk.fox_death");
    public static final SoundEvent BEASTFOLK_FOX_COLLARLESS_HURT = register("beastfolk.fox_collarless_hurt");
    public static final SoundEvent BEASTFOLK_FOX_COLLARLESS_DEATH = register("beastfolk.fox_collarless_death");

    private MmsSounds() {
    }

    public static void register() {
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
