package info.mudbourn.mmsorigins.client;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;

/**
 * Tracks whether the client's eyes are inside lava, so the lava-surface mixins
 * fade it only while submerged and leave it solid when viewed from above. Fluid
 * meshes are chunk-baked and cannot react to the camera per frame, so a change
 * here re-tesselates lava to swap it between its solid and see-through forms.
 * Only the render sections around the viewer are flagged, through the same
 * incremental dirty pipeline that block edits use, so the swap streams in rather
 * than tearing down and rebuilding the whole world at once.
 */
public final class LavaViewState {

    private static final int VERTICAL_SECTION_RADIUS = 4;

    private static final Identifier FIRE_POWER =
        Identifier.fromNamespaceAndPath("originstweaks", "fire_power");

    private static volatile boolean submerged;

    public static boolean isSubmerged() {
        return submerged;
    }

    public static void tick(Minecraft client) {
        Player player = client.player;
        boolean now = player != null
            && player.isEyeInFluid(FluidTags.LAVA)
            && hasFirePower(player);
        if (now != submerged) {
            submerged = now;
            markNearbySectionsDirty(client, player);
        }
    }

    private static void markNearbySectionsDirty(Minecraft client, Player player) {
        if (player == null || client.level == null) {
            return;
        }
        int radius = client.options.getEffectiveRenderDistance();
        int centerX = SectionPos.blockToSectionCoord(player.getBlockX());
        int centerY = SectionPos.blockToSectionCoord(player.getBlockY());
        int centerZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        int minY = Math.max(client.level.getMinSectionY(), centerY - VERTICAL_SECTION_RADIUS);
        int maxY = Math.min(client.level.getMaxSectionY(), centerY + VERTICAL_SECTION_RADIUS);
        client.levelRenderer.setSectionRangeDirty(
            centerX - radius,
            minY,
            centerZ - radius,
            centerX + radius,
            maxY,
            centerZ + radius);
    }

    private static boolean hasFirePower(Player player) {
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(FIRE_POWER)) {
                return true;
            }
        }
        return false;
    }

    private LavaViewState() {
    }
}
