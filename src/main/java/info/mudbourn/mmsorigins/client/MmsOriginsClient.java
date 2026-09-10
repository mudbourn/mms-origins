package info.mudbourn.mmsorigins.client;

import info.mudbourn.mmsorigins.client.fur.FurFeatureRenderer;
import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.wings.ButterflyFlap;
import info.mudbourn.mmsorigins.client.wings.ButterflyWingsFeatureRenderer;
import info.mudbourn.mmsorigins.client.wings.ButterflyWingsModel;
import info.mudbourn.mmsorigins.client.wings.WingsFeatureRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

/**
 * Client entrypoint for the ported Origin Furs and Icarus Wings model layers.
 *
 * <p>mms-origins is otherwise a datapack plus a server-side mixin; these exist only
 * because a fur and a custom wing are player-model additions keyed by origin, which no
 * Apoli power can express. Both are attached to every player renderer as feature layers,
 * and the origin and wing choice they draw are stamped onto the render state by
 * {@code PlayerRendererMixin}.
 */
public class MmsOriginsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FurModels.init();
        EntityModelLayerRegistry.registerModelLayer(ButterflyWingsModel.LAYER, ButterflyWingsModel::createLayer);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, renderer, helper, context) -> {
                    if (renderer instanceof AvatarRenderer player) {
                        helper.register(new FurFeatureRenderer(player));
                        helper.register(new WingsFeatureRenderer(player,
                                context.bakeLayer(ModelLayers.ELYTRA)));
                        helper.register(new ButterflyWingsFeatureRenderer(player,
                                context.bakeLayer(ButterflyWingsModel.LAYER)));
                    }
                });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                ButterflyFlap.tickAll(client.level.players());
            }
        });
    }
}
