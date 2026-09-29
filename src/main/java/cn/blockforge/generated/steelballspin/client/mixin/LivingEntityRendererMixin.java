package cn.blockforge.generated.steelballspin.client.mixin;

import cn.blockforge.generated.steelballspin.GeneratedMod;
import cn.blockforge.generated.steelballspin.client.BallVisual;
import cn.blockforge.generated.steelballspin.SteelBallItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "setupTransforms", at = @At("TAIL"))
    private void steelBallSpin$rotate(LivingEntity entity, MatrixStack matrices, float animationProgress,
                                     float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        var legacy = entity.getStatusEffect(Registries.STATUS_EFFECT.getEntry(GeneratedMod.SPIN_TRANSFER));
        boolean endless = entity.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(GeneratedMod.INFINITE_SPIN))
            || (legacy != null && legacy.isInfinite());
        if (endless) {
            // 无限回旋直接旋转整个模型，防止生物 AI 的朝向修正抵消自旋表现。
            float angle = ((entity.age + tickDelta) * 30f) % 360f;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        }

    }

    @Inject(method = "render", at = @At("TAIL"))
    private void steelBallSpin$drawChargeAura(LivingEntity entity, float yaw, float tickDelta,
                                               MatrixStack matrices, VertexConsumerProvider consumers,
                                               int light, CallbackInfo ci) {
        if (!(entity instanceof net.minecraft.entity.player.PlayerEntity player)) return;
        if (!player.isUsingItem() || !player.getActiveItem().isOf(GeneratedMod.STEEL_BALL)
            || player.getItemUseTime() < SteelBallItem.MAX_CHARGE_TICKS) return;
        matrices.push();
        float time = entity.age + tickDelta;
        BallVisual.drawMaxChargeAura(matrices, consumers, time, tickDelta, 0xF000F0);
        matrices.pop();
    }
}
