package cn.blockforge.generated.steelballspin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class GeneratedMod implements ModInitializer {
    public static final String MOD_ID = "steel_ball_spin";
    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }
    public static final Item STEEL_BALL = Registry.register(Registries.ITEM, id("steel_ball"), new SteelBallItem(new Item.Settings().maxCount(1)));
    public static final Item PIZZA_SLICE = Registry.register(Registries.ITEM, id("pizza_slice"),
        new PizzaSliceItem(new Item.Settings().food(new FoodComponent.Builder().nutrition(6).saturationModifier(.7f).build())));
    public static final StatusEffect SPIN_TRANSFER = Registry.register(Registries.STATUS_EFFECT, id("spin_transfer"), new SpinTransferEffect());
    public static final StatusEffect INFINITE_SPIN = Registry.register(Registries.STATUS_EFFECT, id("infinite_spin"), new InfiniteSpinEffect());
    public static final net.minecraft.particle.SimpleParticleType STEEL_TRAIL = Registry.register(
        Registries.PARTICLE_TYPE, id("steel_trail"), FabricParticleTypes.simple(true));
    public static final EntityType<SteelBallEntity> BALL = Registry.register(Registries.ENTITY_TYPE, id("steel_ball"),
        FabricEntityTypeBuilder.<SteelBallEntity>create(SpawnGroup.MISC, SteelBallEntity::new)
            .dimensions(EntityDimensions.fixed(.5f, .5f)).trackRangeBlocks(96).trackedUpdateRate(1).build());

    @Override public void onInitialize() {
        ModSounds.init();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(STEEL_BALL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> entries.add(PIZZA_SLICE));
        PayloadTypeRegistry.playC2S().register(ModePayload.ID, ModePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModePayload.ID, (payload, context) -> context.server().execute(() -> {
            var player = context.player();
            if (payload.mode() < 0 || payload.mode() > 3 || player.isUsingItem()) return;
            ItemStack stack = player.getMainHandStack().isOf(STEEL_BALL) ? player.getMainHandStack() : player.getOffHandStack();
            if (!stack.isOf(STEEL_BALL)) return;
            SpinMode.set(stack, payload.mode());
            player.sendMessage(SpinMode.from(payload.mode()).title(), true);
        }));
    }
}
