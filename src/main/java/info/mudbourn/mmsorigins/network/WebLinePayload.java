package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server to client: an arachnid's web caught a block or a creature, or let go when
 * {@code attached} is false. Sent only when the web changes, to the owner and the players
 * tracking it. The reel tuning is filled in only for the owner, who runs the swing.
 */
public record WebLinePayload(
        int ownerId,
        boolean attached,
        double x,
        double y,
        double z,
        int entityId,
        float rope,
        float maxRope,
        float reelSpeed,
        float maxReelSpeed,
        int rampDelay,
        int rampTicks) implements CustomPacketPayload {

    /** The {@code entityId} of a web anchored to a block. */
    public static final int NO_ENTITY = -1;

    public static final Type<WebLinePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "web_line"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WebLinePayload> CODEC =
        StreamCodec.of(WebLinePayload::write, WebLinePayload::read);

    /** The payload that tells clients this owner's web let go. */
    public static WebLinePayload detached(int ownerId) {
        return new WebLinePayload(ownerId, false, 0.0, 0.0, 0.0, NO_ENTITY, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0);
    }

    /** This line with the owner-only reel tuning stripped, for other players. */
    public WebLinePayload forViewers() {
        return new WebLinePayload(ownerId, attached, x, y, z, entityId, rope, maxRope, 0.0F, 0.0F, 0, 0);
    }

    /** This line with its rope paid out or reeled in to a new length. */
    public WebLinePayload withRope(float length) {
        return new WebLinePayload(
            ownerId,
            attached,
            x,
            y,
            z,
            entityId,
            length,
            maxRope,
            reelSpeed,
            maxReelSpeed,
            rampDelay,
            rampTicks);
    }

    private static void write(RegistryFriendlyByteBuf buf, WebLinePayload payload) {
        buf.writeVarInt(payload.ownerId);
        buf.writeBoolean(payload.attached);
        if (!payload.attached) {
            return;
        }
        buf.writeDouble(payload.x);
        buf.writeDouble(payload.y);
        buf.writeDouble(payload.z);
        buf.writeVarInt(payload.entityId);
        buf.writeFloat(payload.rope);
        buf.writeFloat(payload.maxRope);
        buf.writeFloat(payload.reelSpeed);
        buf.writeFloat(payload.maxReelSpeed);
        buf.writeVarInt(payload.rampDelay);
        buf.writeVarInt(payload.rampTicks);
    }

    private static WebLinePayload read(RegistryFriendlyByteBuf buf) {
        int ownerId = buf.readVarInt();
        if (!buf.readBoolean()) {
            return detached(ownerId);
        }
        return new WebLinePayload(
            ownerId,
            true,
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readVarInt(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readVarInt(),
            buf.readVarInt());
    }

    @Override
    public Type<WebLinePayload> type() {
        return TYPE;
    }
}
