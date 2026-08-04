package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import net.minecraft.resources.Identifier;

/**
 * References to powers this mod ships that also need a Java-side check.
 *
 * <p>Apoli resolves a {@link PowerTypeReference} lazily by id, so these are safe
 * to create before the power datapack has loaded. Only powers that no Apoli
 * factory can express belong here — everything else stays pure data.
 */
public final class MmsOriginsPowers {

    /**
     * Merling/fishman trade penalty. Apoli has no merchant-offer power type, so
     * the power itself is a marker and the pricing happens in
     * {@code VillagerSwindleMixin}.
     */
    public static final PowerType<?> LANGUAGE_BARRIER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "language_barrier"));

    private MmsOriginsPowers() {
    }
}
