package info.mudbourn.mmsorigins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;

/**
 * Tracks whether the client's eyes are inside lava, so the lava-surface mixins
 * fade it only while submerged and leave it solid when viewed from above. Fluid
 * meshes are chunk-baked and cannot react to the camera per frame, so a change
 * here forces a re-tesselation to swap lava between its solid and see-through
 * forms.
 */
public final class LavaViewState {

    private static volatile boolean submerged;

    public static boolean isSubmerged() {
        return submerged;
    }

    public static void tick(Minecraft client) {
        boolean now = client.player != null && client.player.isEyeInFluid(FluidTags.LAVA);
        if (now != submerged) {
            submerged = now;
            client.levelRenderer.allChanged();
        }
    }

    private LavaViewState() {
    }
}
