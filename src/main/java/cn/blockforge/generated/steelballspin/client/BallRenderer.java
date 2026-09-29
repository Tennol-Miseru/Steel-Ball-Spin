package cn.blockforge.generated.steelballspin.client;

import cn.blockforge.generated.steelballspin.SteelBallEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class BallRenderer extends EntityRenderer<SteelBallEntity> {
    public BallRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = .2f;
    }

    @Override public Identifier getTexture(SteelBallEntity entity) { return BallVisual.TEXTURE; }

    @Override public void render(SteelBallEntity entity, float yaw, float delta, MatrixStack matrices,
                                 VertexConsumerProvider consumers, int light) {
        matrices.push();
        matrices.translate(0, .25, 0);
        BallVisual.draw(matrices, consumers, light, OverlayTexture.DEFAULT_UV,
            entity.age + delta, entity.power(), true, false, false);
        matrices.pop();
        super.render(entity, yaw, delta, matrices, consumers, light);
    }
}
