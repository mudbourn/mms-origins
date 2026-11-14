package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Both ways: an arachnid's rope changed length. The owner sends it to the server, throttled,
 * and the server relays it to the players tracking the owner so their lines sag to match.
 * The owner leaves {@code ownerId} unset; the server fills it in when relaying.
 */
public record WebRopePayload(int ownerId, float rope) implements CustomPacketPayload {

    public static final Type<WebRopePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "web_rope"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WebRopePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        WebRopePayload::ownerId,
        ByteBufCodecs.FLOAT,
        WebRopePayload::rope,
        WebRopePayload::new);

    @Override
    public Type<WebRopePayload> type() {
        return TYPE;
    }
}
