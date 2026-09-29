package cn.blockforge.generated.steelballspin;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ModePayload(int mode) implements CustomPayload {
    public static final Id<ModePayload> ID = new Id<>(GeneratedMod.id("select_mode"));
    public static final PacketCodec<RegistryByteBuf, ModePayload> CODEC = CustomPayload.codecOf(
        (payload, buf) -> buf.writeVarInt(payload.mode()), buf -> new ModePayload(buf.readVarInt()));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
