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
     * Fishman trade penalty. Apoli has no merchant-offer power type, so
     * the power itself is a marker and the pricing happens in
     * {@code VillagerSwindleMixin}.
     */
    public static final PowerType<?> LANGUAGE_BARRIER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "language_barrier"));

    /**
     * Piglin kinship. Piglins and brutes leave the bearer alone; Apoli has no
     * factory to pacify a brain-driven mob, so {@code PiglinKinshipMixin} clears
     * their target while this is active and the bearer is not zombified.
     */
    public static final PowerType<?> KINSMEN =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "kinsmen"));

    /**
     * Brute arms dealing. Piglin brutes carry no bartering behaviour at all, so
     * {@code PiglinBruteBarterMixin} adds one and gates it on the gold's thrower
     * holding this power.
     */
    public static final PowerType<?> ARMS_DEALING =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "arms_dealing"));

    /**
     * The zombified state, gated on the overworld meter reaching its cap. Read
     * Java-side by {@code PiglinKinshipMixin} to drop kinship while it holds.
     */
    public static final PowerType<?> ZOMBIFIED =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "zombified"));

    /**
     * The overworld zombification meter. Read client-side by the render mixins so
     * a piglin whose sickness has passed the halfway mark visibly quivers.
     */
    public static final PowerType<?> ZOMBIE_METER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "zombie_meter"));

    /** Half of the zombie meter's cap, past which the shakes set in. */
    public static final int ZOMBIE_SHAKE_THRESHOLD = 300;

    /**
     * Piglin trade shunning. Villagers refuse to open trade with the bearer, a
     * merchant-interaction call no Apoli factory reaches, so
     * {@code VillagerBeastlyRefusalMixin} cancels the interaction unless a carved
     * pumpkin hides the face.
     */
    public static final PowerType<?> BEASTLY =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "beastly"));

    /**
     * Fairy charm. Villagers, taken with the fairy, discount their goods. The
     * mirror of {@link #LANGUAGE_BARRIER}: no Apoli factory reaches a merchant
     * offer, so the power is a marker and {@code VillagerCharmMixin} does the
     * pricing.
     */
    public static final PowerType<?> FAIRY_CHARM =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "fairy_charm"));

    /**
     * Floran growth aura. Apoli has no factory that quickens block growth, so
     * the power is a marker and {@code VerdantGrowth} hastens plants around the
     * bearer each world tick.
     */
    public static final PowerType<?> VERDANT_GROWTH =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "verdant_growth"));

    private MmsOriginsPowers() {
    }
}
