package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client to server: whether the arachnid is holding use with an empty hand to reel in
 * its web.
 */
public record WebReelPayload(boolean reeling) implements CustomPacketPayload {

    public static final Type<WebReelPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "web_reel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WebReelPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        WebReelPayload::reeling,
        WebReelPayload::new);

    @Override
    public Type<WebReelPayload> type() {
        return TYPE;
    }
}
