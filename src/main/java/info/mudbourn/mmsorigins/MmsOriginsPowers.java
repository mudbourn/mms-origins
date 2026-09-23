package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * References to powers this mod ships that also need a Java-side check.
 *
 * <p>Apoli resolves a {@link PowerTypeReference} lazily by id, so these are safe
 * to create before the power datapack has loaded. Only powers that no Apoli
 * factory can express belong here; everything else stays pure data.
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
     * Beastfolk trade markup. Villagers charge the beast-tongued more, the same
     * markup a fishman's {@link #LANGUAGE_BARRIER} draws but without its crop
     * penalty, so {@code VillagerSwindleMixin} reads this too.
     */
    public static final PowerType<?> BEAST_TONGUE =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "beast_tongue"));

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
     * Fairy allure. The mystique that charms villagers draws monsters too: they
     * hunt a fairy from farther off and in preference to others. Mob targeting
     * has no Apoli hook, so the power is a marker and {@code FairyAllureMixin}
     * draws hostile mobs to the bearer.
     */
    public static final PowerType<?> FAIRY_ALLURE =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "fairy_allure"));

    /**
     * Floran growth aura. Apoli has no factory that quickens block growth, so
     * the power is a marker and {@code VerdantGrowth} hastens plants around the
     * bearer each world tick.
     */
    public static final PowerType<?> VERDANT_GROWTH =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "verdant_growth"));

    /**
     * Enderian gaze kinship. Endermen aggro when a player stares at them, a
     * check baked into the mob with no Apoli hook, so the power is a marker and
     * {@code EndermanKinshipMixin} spares the bearer from it.
     */
    public static final PowerType<?> ENDER_KINSHIP =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath("originstweaks", "ender_kinship"));

    /**
     * Guardian kinship. Guardians and elder guardians take a fishman for one of
     * their own and never hunt them. Their targeting carries no Apoli hook, so
     * the power is a marker and {@code GuardianKinshipMixin} and
     * {@code GuardianForgetTargetMixin} spare the bearer while unprovoked.
     */
    public static final PowerType<?> GUARDIAN_KINSHIP =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "guardian_kinship"));

    /**
     * Avian peace. The overworld's hostile mobs leave the beloved bird-folk be,
     * a truce baked into mob targeting with no Apoli hook, so the power is a
     * marker and {@code AvianPeaceMixin} and {@code AvianForgetTargetMixin} spare
     * the bearer while they have not struck first.
     */
    public static final PowerType<?> AVIAN_PEACE =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath("originstweaks", "avian_peace"));

    /**
     * The fairy's anxious-heart cooldown. Apoli's {@code action_when_hit} sees
     * only the raw incoming damage, so {@code FairyStartleMixin} measures the
     * true health lost to a single blow and triggers this cooldown when it
     * reaches the faint threshold; {@code fairy_flight} is gated on it.
     */
    public static final PowerType<CooldownPower> ANXIOUS_HEART_TIMER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "anxious_heart_timer"));

    /** Health lost to one blow, at or above which a fairy is startled from the air. */
    public static final float STARTLE_DAMAGE_THRESHOLD = 8.0F;

    /**
     * Beastfolk dread. A predator's mien sends the overworld's timid creatures
     * bolting, a flight response baked into mob AI with no Apoli hook, so the
     * power is a marker and {@code ViciousAppearanceMixin} drives the nearby
     * quarry away.
     */
    public static final PowerType<?> VICIOUS_APPEARANCE =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath("originstweaks", "vicious_appearance"));

    /**
     * Blazeborn fire power, the resource bar a bearer spends to fly. Holding it
     * lets {@code LavaSwimMixin} swim lava like water and lowers the fire overlay.
     */
    public static final PowerType<?> FIRE_POWER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath("originstweaks", "fire_power"));

    /** Range, in blocks, at which a beastfolk's viciousness spooks timid mobs. */
    public static final double VICIOUS_APPEARANCE_RANGE = 8.0;

    /**
     * Share of a beastfolk's combat damage a single player must have dealt for
     * their death to read as a fall in worthy battle and earn the drawn-out howl.
     */
    public static final float WORTHY_KILL_DAMAGE_SHARE = 0.8F;

    /**
     * Damage a single killing blow must land to earn the howl on its own, the
     * mark of a boss-tier hit rather than an ordinary mob's swing.
     */
    public static final float WORTHY_HEAVY_BLOW = 12.0F;

    /** Whether the entity holds the power at all, active or not, by a single map lookup. */
    public static boolean holds(Entity entity, PowerType<?> type) {
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        return component != null && component.hasPower(type);
    }

    private MmsOriginsPowers() {
    }
}
