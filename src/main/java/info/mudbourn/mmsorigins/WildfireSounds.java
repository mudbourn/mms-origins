package info.mudbourn.mmsorigins;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Friends and Foes' wildfire sounds, looked up by id for the Blazeborn's wildfire shield.
 */
public final class WildfireSounds {

    public static final Identifier SHOOT = Identifier.fromNamespaceAndPath("friendsandfoes", "entity.wildfire.shoot");
    public static final Identifier SHOCKWAVE = Identifier.fromNamespaceAndPath("friendsandfoes", "entity.wildfire.shockwave");
    public static final Identifier SHIELD_BREAK = Identifier.fromNamespaceAndPath("friendsandfoes", "entity.wildfire.shield_break");

    /** The registered sound event for the id. */
    public static SoundEvent get(Identifier id) {
        return BuiltInRegistries.SOUND_EVENT.getValue(id);
    }

    private WildfireSounds() {
    }
}
