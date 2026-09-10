package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.client.ZombieShakeState;
import info.mudbourn.mmsorigins.client.fur.FurState;
import info.mudbourn.mmsorigins.client.wings.ButterflyFlap;
import info.mudbourn.mmsorigins.client.wings.WingState;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.player.Player;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Reads the player's origin during extraction and stamps it onto the render state.
 *
 * <p>The origin lives on the player entity, which the fur feature layer never sees;
 * this is the one place both are in scope. The lookup is null-guarded end to end so a
 * player with no origin, or a world where the layer has not synced, simply carries no
 * fur rather than crashing the entity render.
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("RETURN"))
    private void mmsOrigins$stampOrigin(Avatar player,
                                        AvatarRenderState state,
                                        float partialTick,
                                        CallbackInfo ci) {
        ((FurState) state).mmsOrigins$setFurOrigin(mmsOrigins$originOf(player));
        ((WingState) state).mmsOrigins$setWingOption(mmsOrigins$wingOf(player));
        ((ZombieShakeState) state).mmsOrigins$setZombieShaking(mmsOrigins$zombieShaking(player));
        if (player instanceof Player concrete) {
            ((WingState) state).mmsOrigins$setWingFlapDegrees(
                    ButterflyFlap.degreesFor(concrete, partialTick));
        }
    }

    /** Whether the player's zombification has crept past the halfway mark. */
    private static boolean mmsOrigins$zombieShaking(Avatar player) {
        Power power = MmsOriginsPowers.ZOMBIE_METER.get(player);
        return power instanceof VariableIntPower meter
                && meter.getValue() >= MmsOriginsPowers.ZOMBIE_SHAKE_THRESHOLD;
    }

    private static final Identifier FAIRY_OPTIONS =
            Identifier.fromNamespaceAndPath("mms_origins", "fairy_wings");

    private static final Identifier ELYTRIAN_OPTIONS =
            Identifier.fromNamespaceAndPath("originstweaks", "elytrian_options");
    private static final Identifier FELINE_OPTIONS =
            Identifier.fromNamespaceAndPath("originstweaks", "feline_options");
    private static final Identifier FELINE_NO_COLLAR =
            Identifier.fromNamespaceAndPath("originstweaks", "feline_no_collar");
    private static final Identifier FELINE_NOCOLLAR_FUR =
            Identifier.fromNamespaceAndPath("originstweaks", "feline_nocollar");
    private static final Map<String, Identifier> FAIRY_WING_FURS = Map.of(
            "cirno_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_cirno"),
            "pixie_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_pixie"),
            "slime_wings", Identifier.fromNamespaceAndPath("mms_origins", "fairy_slime"));

    private static Identifier mmsOrigins$originOf(Avatar player) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(player).orElse(null);
        if (component == null) {
            return null;
        }
        OriginLayer layer = OriginLayers.getLayer(Identifier.fromNamespaceAndPath("origins", "origin"));
        if (layer == null || !component.hasOrigin(layer)) {
            return null;
        }
        Origin origin = component.getOrigin(layer);
        if (origin == null) {
            return null;
        }
        Identifier id = origin.getIdentifier();
        if ("feline".equals(id.getPath()) && mmsOrigins$hasNoCollar(component)) {
            return FELINE_NOCOLLAR_FUR;
        }
        if ("fairy".equals(id.getPath())) {
            Identifier variant = mmsOrigins$fairyWingFur(component);
            if (variant != null) {
                return variant;
            }
        }
        return id;
    }

    /** The fur variant for the fairy's chosen wing, or null if the choice has no variant. */
    private static Identifier mmsOrigins$fairyWingFur(OriginComponent component) {
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

    /** Whether the player picked the collarless option in the feline options layer. */
    private static boolean mmsOrigins$hasNoCollar(OriginComponent component) {
        OriginLayer layer = OriginLayers.getLayer(FELINE_OPTIONS);
        if (layer == null || !component.hasOrigin(layer)) {
            return false;
        }
        Origin option = component.getOrigin(layer);
        return option != null && FELINE_NO_COLLAR.equals(option.getIdentifier());
    }

    /** The elytrian's chosen wing option, or null if the player is not an elytrian. */
    private static Identifier mmsOrigins$wingOf(Avatar player) {
        OriginComponent component = ModComponents.ORIGIN.maybeGet(player).orElse(null);
        if (component == null) {
            return null;
        }
        OriginLayer base = OriginLayers.getLayer(Identifier.fromNamespaceAndPath("origins", "origin"));
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
}
