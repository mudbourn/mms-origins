package info.mudbourn.mmsorigins.fur;

import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Mannequin;

// Dresses an mms-dungeons corpse mannequin in its owner's fur, tail, and wings when it first enters the world.
public final class CorpseFurBridge {

    private static final String OWNER_TAG_PREFIX = "mms_dungeons_corpse_owner_";

    private CorpseFurBridge() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(CorpseFurBridge::onLoad);
    }

    private static void onLoad(Entity entity, ServerLevel level) {
        if (!(entity instanceof Mannequin body) || body.hasAttached(BodyFurAttachment.TYPE)) {
            return;
        }
        UUID owner = ownerOf(body);
        if (owner == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return;
        }
        Identifier fur = FurResolver.resolve(player);
        if (fur != null) {
            body.setAttached(BodyFurAttachment.TYPE, fur);
        }
        Identifier tail = FurResolver.tailOption(player);
        if (tail != null) {
            body.setAttached(BodyTailAttachment.TYPE, tail);
        }
        Identifier wing = FurResolver.wingOption(player);
        if (wing != null) {
            body.setAttached(BodyWingAttachment.TYPE, wing);
        }
    }

    private static UUID ownerOf(Entity entity) {
        for (String tag : entity.entityTags()) {
            if (tag.startsWith(OWNER_TAG_PREFIX)) {
                try {
                    return UUID.fromString(tag.substring(OWNER_TAG_PREFIX.length()));
                } catch (IllegalArgumentException malformed) {
                    return null;
                }
            }
        }
        return null;
    }
}
