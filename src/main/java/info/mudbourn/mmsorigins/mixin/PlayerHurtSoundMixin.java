package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Silences the vanilla player hurt sound for players who have taken on an origin.
 *
 * <p>Every non-human origin ships a sounds power whose {@code hurt_death} plays a
 * species hurt sound when the player is hit, so the vanilla {@code entity.player.hurt}
 * would double up over it. This cancels the vanilla sound whenever the player has an
 * origin other than {@code origins:human}; a human player, or one whose origin layer
 * has not synced, keeps the vanilla sound.
 */
@Mixin(LivingEntity.class)
public abstract class PlayerHurtSoundMixin {

    private static final Identifier HUMAN = Identifier.fromNamespaceAndPath("origins", "human");

    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$silenceOriginHurtSound(DamageSource source, CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) {
            return;
        }
        Identifier origin = mmsOrigins$originOf(player);
        if (origin != null && !origin.equals(HUMAN)) {
            ci.cancel();
        }
    }

    private static Identifier mmsOrigins$originOf(Player player) {
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
