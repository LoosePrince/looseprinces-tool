package com.tool.looseprince.network.payload;

import com.tool.looseprince.LoosePrincesTool;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CreatorRequestPayload(String itemId) implements CustomPayload {
    public static final CustomPayload.Id<CreatorRequestPayload> ID = new CustomPayload.Id<>(
            Identifier.of(LoosePrincesTool.MOD_ID, "creator_request")
    );

    public static final PacketCodec<PacketByteBuf, CreatorRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, CreatorRequestPayload::itemId,
            CreatorRequestPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void registerTypeC2S() {
        try {
            PayloadTypeRegistry.playC2S().register(ID, CODEC);
        } catch (Exception ignored) {}
    }
}


