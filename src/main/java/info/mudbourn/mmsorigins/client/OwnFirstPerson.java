package info.mudbourn.mmsorigins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Whether a render state is the local player seen from their own first-person camera. Morph forms skip drawing then:
 * Better Combat renders the body in first person during swings, and a phantom or bush would fill the screen.
 */
public final class OwnFirstPerson {
    private OwnFirstPerson() {
    }

    public static boolean is(LivingEntityRenderState state) {
        Minecraft client = Minecraft.getInstance();
        return client.player != null
            && client.options.getCameraType().isFirstPerson()
            && state instanceof AvatarRenderState avatar
            && avatar.id == client.player.getId();
    }
}
