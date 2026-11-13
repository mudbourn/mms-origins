package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client to server: a flying phantom hit something and lost {@code speedLost} blocks per
 * tick of speed in the impact.
 */
public record PhantomCrashPayload(float speedLost) implements CustomPacketPayload {

    public static final Type<PhantomCrashPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "phantom_crash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PhantomCrashPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        PhantomCrashPayload::speedLost,
        PhantomCrashPayload::new);

    @Override
    public Type<PhantomCrashPayload> type() {
        return TYPE;
    }
}
