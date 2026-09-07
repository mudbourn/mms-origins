package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.fur.FurState;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Reads the player's origin during extraction, stamps it onto the render state, and
 * swaps in the fur's body overlay.
 *
 * <p>The origin lives on the player entity, which the fur feature layer never sees;
 * this is the one place both are in scope. The stamp feeds the geo fins; the overlay,
 * when the fur has one, replaces the state's body skin texture so the vanilla model
 * renders as the fur body rather than the player's own skin. The lookup is null-guarded
 * end to end so a player with no origin, or a world where the layer has not synced,
 * simply carries no fur rather than crashing the entity render.
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("RETURN"))
    private void mmsOrigins$stampOrigin(Avatar player,
                                        AvatarRenderState state,
                                        float partialTick,
                                        CallbackInfo ci) {
        Identifier origin = mmsOrigins$originOf(player);
        ((FurState) state).mmsOrigins$setFurOrigin(origin);
        mmsOrigins$applyOverlay(state, FurModels.resolve(origin));
    }

    private static void mmsOrigins$applyOverlay(AvatarRenderState state, FurModels.Fur fur) {
        if (fur == null || state.skin == null) {
            return;
        }
        Identifier overlay = mmsOrigins$overlayFor(fur, state);
        if (overlay == null) {
            return;
        }
        ClientAsset.ResourceTexture body = new ClientAsset.ResourceTexture(overlay, overlay);
        state.skin = state.skin.with(PlayerSkin.Patch.create(
                Optional.of(body),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));
    }

    private static Identifier mmsOrigins$overlayFor(FurModels.Fur fur, AvatarRenderState state) {
        if (fur.overlaySlim() != null && state.skin.model() == PlayerModelType.SLIM) {
            return fur.overlaySlim();
        }
        return fur.overlay();
    }

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
        return origin == null ? null : origin.getIdentifier();
    }
}
