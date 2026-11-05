package info.mudbourn.mmsorigins.network;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Starts or stops the guardian beam drawn from a fishman to their locked target.
 *
 * <p>A {@code targetId} of {@link #NONE} stops the beam; any other value starts a
 * fresh charge of {@code chargeTicks} ticks toward that entity.
 */
public record GuardianBeamPayload(int casterId, int targetId, int chargeTicks) implements CustomPacketPayload {

    public static final int NONE = -1;

    public static final Type<GuardianBeamPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "guardian_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuardianBeamPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        GuardianBeamPayload::casterId,
        ByteBufCodecs.VAR_INT,
        GuardianBeamPayload::targetId,
        ByteBufCodecs.VAR_INT,
        GuardianBeamPayload::chargeTicks,
        GuardianBeamPayload::new);

    @Override
    public Type<GuardianBeamPayload> type() {
        return TYPE;
    }
}
