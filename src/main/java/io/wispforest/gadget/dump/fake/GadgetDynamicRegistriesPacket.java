package io.wispforest.gadget.dump.fake;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.wispforest.gadget.mixin.TagNetworkSerializationAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public record GadgetDynamicRegistriesPacket(
    Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements,
    Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags
) implements FakeGadgetPacket {
    public static final int ID = -3;

    private static final StreamCodec<ByteBuf, ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = Identifier.STREAM_CODEC
        .map(ResourceKey::createRegistryKey, ResourceKey::identifier);

    private static final StreamCodec<FriendlyByteBuf, TagNetworkSerialization.NetworkPayload> TAG_SERIALIZED_CODEC =
        TagNetworkSerialization.NetworkPayload.STREAM_CODEC.cast();

    public static final StreamCodec<FriendlyByteBuf, GadgetDynamicRegistriesPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(
            HashMap::new,
            REGISTRY_KEY_CODEC,
            RegistrySynchronization.PackedRegistryEntry.STREAM_CODEC.apply(ByteBufCodecs.list())
        ),
        GadgetDynamicRegistriesPacket::elements,
        ByteBufCodecs.map(
            HashMap::new,
            REGISTRY_KEY_CODEC,
            TAG_SERIALIZED_CODEC
        ),
        GadgetDynamicRegistriesPacket::tags,
        GadgetDynamicRegistriesPacket::new
    );

    public static GadgetDynamicRegistriesPacket fromRegistries(RegistryAccess.Frozen registries) {
        DynamicOps<Tag> dynamicOps = registries.createSerializationContext(NbtOps.INSTANCE);
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap<>();

        Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = registries.registries()
            .map(registry -> Pair.of(registry.key(), TagNetworkSerializationAccessor.serializeToNetwork(registry.value())))
            .filter(pair -> !pair.getSecond().isEmpty())
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        RegistrySynchronization.packRegistries(
            dynamicOps,
            registries,
            Set.of(),
            elements::put
        );

        return new GadgetDynamicRegistriesPacket(elements, tags);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, GadgetDynamicRegistriesPacket> codec() {
        return CODEC;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
