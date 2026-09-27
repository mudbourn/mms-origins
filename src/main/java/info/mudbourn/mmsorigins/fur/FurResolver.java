package info.mudbourn.mmsorigins.fur;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.TogglePower;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

// Resolves the fur an origin bearer wears, including the piglin-zombie, beastfolk-collar, beastfolk-tail, and fairy-wing variants. Shared so the player renderer and the logout-body bridge resolve identically.
public final class FurResolver {

    private static final int ZOMBIE_METER_CAP = 600;

    private static final Identifier BASE_LAYER =
        Identifier.fromNamespaceAndPath("origins", "origin");
    private static final Identifier PIGLIN_ZOMBIE_FUR =
        Identifier.fromNamespaceAndPath("mms_origins", "piglin_zombie");
    private static final Identifier FAIRY_OPTIONS =
        Identifier.fromNamespaceAndPath("mms_origins", "fairy_wings");
    private static final Identifier ELYTRIAN_OPTIONS =
        Identifier.fromNamespaceAndPath("originstweaks", "elytrian_options");
    private static final Identifier BEASTFOLK_OPTIONS =
        Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_options");
    private static final Identifier BEASTFOLK_TAIL =
        Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_tail");
    private static final Identifier BEASTFOLK_COLLAR =
        Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_collar");
    private static final Identifier BEASTFOLK_NO_COLLAR =
        Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_no_collar");
    private static final Identifier BEASTFOLK_NOCOLLAR_FUR =
        Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_nocollar");
    private static final Map<String, Identifier> FAIRY_WING_FURS = Map.of(
        "cirno_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_cirno"),
        "pixie_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_pixie"),
        "slime_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_slime"));

    private FurResolver() {
    }

    // The fur id for this bearer, or null when they have no origin or no fur.
    public static Identifier resolve(Avatar bearer) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(bearer).orElse(null);
        if (component == null) {
            return null;
        }
        OriginLayer layer = OriginLayers.getLayer(BASE_LAYER);
        if (layer == null || !component.hasOrigin(layer)) {
            return null;
        }
        Origin origin = component.getOrigin(layer);
        if (origin == null) {
            return null;
        }
        Identifier id = origin.getIdentifier();
        if ("piglin".equals(id.getPath()) && zombified(bearer)) {
            return PIGLIN_ZOMBIE_FUR;
        }
        if ("beastfolk".equals(id.getPath()) && hasNoCollar(component)) {
            return BEASTFOLK_NOCOLLAR_FUR;
        }
        if ("fairy".equals(id.getPath())) {
            Identifier variant = fairyWingFur(component);
            if (variant != null) {
                return variant;
            }
        }
        return id;
    }

    // The bearer's chosen wing option, or null when they are not a winged origin.
    public static Identifier wingOption(Avatar bearer) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(bearer).orElse(null);
        if (component == null) {
            return null;
        }
        OriginLayer base = OriginLayers.getLayer(BASE_LAYER);
        if (base == null || !component.hasOrigin(base)) {
            return null;
        }
        Origin origin = component.getOrigin(base);
        if (origin == null) {
            return null;
        }
        Identifier optionsLayer = switch (origin.getIdentifier().getPath()) {
            case "elytrian" -> ELYTRIAN_OPTIONS;
            case "fairy" -> FAIRY_OPTIONS;
            default -> null;
        };
        if (optionsLayer == null) {
            return null;
        }
        OriginLayer options = OriginLayers.getLayer(optionsLayer);
        if (options == null || !component.hasOrigin(options)) {
            return null;
        }
        Origin option = component.getOrigin(options);
        return option == null ? null : option.getIdentifier();
    }

    // The bearer's chosen beastfolk tail option, or null when they are not a beastfolk or chose none.
    public static Identifier tailOption(Avatar bearer) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(bearer).orElse(null);
        if (component == null) {
            return null;
        }
        OriginLayer base = OriginLayers.getLayer(BASE_LAYER);
        if (base == null || !component.hasOrigin(base)) {
            return null;
        }
        Origin origin = component.getOrigin(base);
        if (origin == null || !"beastfolk".equals(origin.getIdentifier().getPath())) {
            return null;
        }
        OriginLayer tails = OriginLayers.getLayer(BEASTFOLK_TAIL);
        if (tails == null || !component.hasOrigin(tails)) {
            return null;
        }
        Origin option = component.getOrigin(tails);
        return option == null ? null : option.getIdentifier();
    }

    // Whether the bearer holds beastfolk claw mode and has it switched on.
    public static boolean clawsOut(Avatar bearer) {
        Power power = MmsOriginsPowers.CLAW_MODE.get(bearer);
        return power instanceof TogglePower toggle && toggle.isActive();
    }

    // Whether the bearer's zombification has reached its cap and the rot has set in.
    private static boolean zombified(Avatar bearer) {
        Power power = MmsOriginsPowers.ZOMBIE_METER.get(bearer);
        return power instanceof VariableIntPower meter && meter.getValue() >= ZOMBIE_METER_CAP;
    }

    // Whether the bearer picked the collar option in the beastfolk options layer.
    public static boolean wearsCollar(Avatar bearer) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(bearer).orElse(null);
        return component != null && BEASTFOLK_COLLAR.equals(beastfolkOption(component));
    }

    // Whether the bearer picked the collarless option in the beastfolk options layer.
    private static boolean hasNoCollar(OriginComponent component) {
        return BEASTFOLK_NO_COLLAR.equals(beastfolkOption(component));
    }

    // The bearer's pick in the beastfolk options layer, or null when they have none.
    private static Identifier beastfolkOption(OriginComponent component) {
        OriginLayer layer = OriginLayers.getLayer(BEASTFOLK_OPTIONS);
        if (layer == null || !component.hasOrigin(layer)) {
            return null;
        }
        Origin option = component.getOrigin(layer);
        return option == null ? null : option.getIdentifier();
    }

    // The fur variant for the fairy's chosen wing, or null when the choice has no variant.
    private static Identifier fairyWingFur(OriginComponent component) {
        OriginLayer layer = OriginLayers.getLayer(FAIRY_OPTIONS);
        if (layer == null || !component.hasOrigin(layer)) {
            return null;
        }
        Origin option = component.getOrigin(layer);
        if (option == null) {
            return null;
        }
        return FAIRY_WING_FURS.get(option.getIdentifier().getPath());
    }
}
