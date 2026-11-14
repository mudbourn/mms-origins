package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client to server: the arachnid pressed its web key afresh, so any web it still holds
 * lets go and a new one fires.
 */
public record WebFirePayload() implements CustomPacketPayload {

    public static final WebFirePayload INSTANCE = new WebFirePayload();

    public static final Type<WebFirePayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "web_fire"));

    public static final StreamCodec<ByteBuf, WebFirePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<WebFirePayload> type() {
        return TYPE;
    }
}
