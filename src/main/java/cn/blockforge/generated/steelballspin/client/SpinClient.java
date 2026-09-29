package cn.blockforge.generated.steelballspin.client;

import cn.blockforge.generated.steelballspin.GeneratedMod;
import cn.blockforge.generated.steelballspin.SpinMode;
import cn.blockforge.generated.steelballspin.SteelBallItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class SpinClient implements ClientModInitializer {
    public static KeyBinding wheelKey;

    public static ItemStack held(MinecraftClient client) {
        if (client.player == null) return ItemStack.EMPTY;
        return client.player.getMainHandStack().isOf(GeneratedMod.STEEL_BALL) ? client.player.getMainHandStack() :
            client.player.getOffHandStack().isOf(GeneratedMod.STEEL_BALL) ? client.player.getOffHandStack() : ItemStack.EMPTY;
    }

    @Override public void onInitializeClient() {
        wheelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.steel_ball_spin.wheel",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.steel_ball_spin"));
        EntityRendererRegistry.register(GeneratedMod.BALL, BallRenderer::new);
        ParticleFactoryRegistry.getInstance().register(GeneratedMod.STEEL_TRAIL,
            sprites -> new SteelTrailParticle.Factory(sprites));
        BuiltinItemRendererRegistry.INSTANCE.register(GeneratedMod.STEEL_BALL, (stack, mode, matrices, consumers, light, overlay) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean hand = mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND ||
                mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
            boolean charging = false;
            int ticks = 0;
            if (hand && client.player != null && client.player.isUsingItem()
                && client.player.getActiveItem().isOf(GeneratedMod.STEEL_BALL)) {
                charging = true;
                ticks = client.player.getItemUseTime();
            }
            float delta = client.getRenderTickCounter().getTickDelta(false);
            matrices.push();
            matrices.translate(.5, .5, .5);
            if (mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
                matrices.translate(0, .04, -.08);
            }
            BallVisual.draw(matrices, consumers, light, overlay, ticks + delta,
                SteelBallItem.charge(ticks), charging, charging, charging);
            matrices.pop();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (wheelKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null && !client.player.isUsingItem() && !held(client).isEmpty())
                    client.setScreen(new SpinWheelScreen());
            }
        });
        HudRenderCallback.EVENT.register((draw, counter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ItemStack stack = held(client);
            if (stack.isEmpty() || client.options.hudHidden || client.currentScreen != null) return;
            SpinMode mode = SpinMode.get(stack);
            int x = draw.getScaledWindowWidth() / 2;
            int y = draw.getScaledWindowHeight() - 70;
            boolean using = client.player != null && client.player.isUsingItem() && client.player.getActiveItem().isOf(GeneratedMod.STEEL_BALL);
            if (using) {
                int ticks = client.player.getItemUseTime();
                float p = SteelBallItem.charge(ticks);
                if (ticks >= SteelBallItem.MAX_CHARGE_TICKS) {
                    String readyMessage = mode == SpinMode.INFINITE
                        ? "hud.steel_ball_spin.perfect_golden"
                        : "hud.steel_ball_spin.golden_ready";
                    draw.drawCenteredTextWithShadow(client.textRenderer,
                        net.minecraft.text.Text.translatable(readyMessage), x, y - 2, 0xFFFFD35E);
                }
                draw.fill(x - 62, y + 12, x + 62, y + 19, 0xC0111B18);
                draw.fill(x - 60, y + 14, x - 60 + (int) (120 * p), y + 17,
                    SteelBallItem.goldenCharge(ticks) ? 0xFFFFD35E : 0xFF75DEA0);
                draw.fill(x - 20, y + 12, x - 19, y + 19, 0xFF637A69);
                draw.fill(x + 20, y + 12, x + 21, y + 19, 0xFF637A69);
            } else {
                draw.drawCenteredTextWithShadow(client.textRenderer, mode.title(), x, y, mode.color);
            }
        });
    }
}
