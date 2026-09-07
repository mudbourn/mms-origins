package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.client.fur.FurFeatureRenderer;
import info.mudbourn.mmsorigins.client.fur.FurModels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

/**
 * Client entrypoint for the ported Origin Furs model layer.
 *
 * <p>mms-origins is otherwise a datapack plus a server-side mixin; this exists only
 * because a fur is a player-model addition keyed by origin, which no Apoli power can
 * express. The fur is attached to every player renderer as a feature layer, and the
 * origin it draws is stamped onto the render state by {@code PlayerRendererMixin}.
 */
public class MmsOriginsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FurModels.init();
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, renderer, helper, context) -> {
                    if (renderer instanceof AvatarRenderer player) {
                        helper.register(new FurFeatureRenderer(player));
                    }
                });
    }
}
