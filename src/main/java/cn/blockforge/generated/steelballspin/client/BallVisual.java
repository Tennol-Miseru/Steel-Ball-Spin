package cn.blockforge.generated.steelballspin.client;

import cn.blockforge.generated.steelballspin.GeneratedMod;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;

/** 高分段经纬球面，以及铁球蓄力与飞行时使用的贴图特效。 */
public final class BallVisual {
    public static final Identifier TEXTURE = GeneratedMod.id("textures/item/steel_surface.png");
    /** 旋转时单独使用的贴图，方便替换成自定义旋转模糊效果。 */
    public static final Identifier SPIN_TEXTURE = GeneratedMod.id("textures/item/steel_surface_spin.png");
    private static final Identifier[] TRAIL_TEXTURES = {
        GeneratedMod.id("textures/effect/trail_1.png"),
        GeneratedMod.id("textures/effect/trail_2.png"),
        GeneratedMod.id("textures/effect/trail_3.png")
    };
    private static final Identifier[] CHARGE_TEXTURES = {
        GeneratedMod.id("textures/effect/charge_1.png"),
        GeneratedMod.id("textures/effect/charge_2.png"),
        GeneratedMod.id("textures/effect/charge_3.png"),
        GeneratedMod.id("textures/effect/charge_4.png")
    };
    private static final int LATITUDE = 16;
    private static final int LONGITUDE = 32;
    private static final float RADIUS = 4f;
    private static final float MODEL_SCALE = 1f / 16f;

    private BallVisual() {}

    public static void draw(MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay,
                            float time, float charge, boolean spinning, boolean spinTexture, boolean showCharge) {
        matrices.push();
        matrices.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        if (spinning) {
            // 横向旋转：绕 Y 轴自转，使纹理在画面中左右扫过，而不是上下翻动。
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * (720 + charge * 1800)));
        }
        Identifier surface = spinTexture ? SPIN_TEXTURE : TEXTURE;
        sphere(matrices, consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(surface)), light, overlay);
        matrices.pop();
        if (showCharge && spinning && charge > .01f) chargeArcs(matrices, consumers, time, charge);
    }

    private static void sphere(MatrixStack matrices, VertexConsumer v, int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();
        var matrix = entry.getPositionMatrix();
        for (int lat = 0; lat < LATITUDE; lat++) {
            double phi0 = -Math.PI / 2 + Math.PI * lat / LATITUDE;
            double phi1 = -Math.PI / 2 + Math.PI * (lat + 1) / LATITUDE;
            for (int lon = 0; lon < LONGITUDE; lon++) {
                double theta0 = Math.PI * 2 * lon / LONGITUDE;
                double theta1 = Math.PI * 2 * (lon + 1) / LONGITUDE;
                // 将整张纹理按经纬度展开到球面，而不是让每个网格面重复显示整张图。
                vertex(v, entry, matrix, phi0, theta0, sphereU(theta0), sphereV(phi0), light, overlay);
                vertex(v, entry, matrix, phi0, theta1, sphereU(theta1), sphereV(phi0), light, overlay);
                vertex(v, entry, matrix, phi1, theta1, sphereU(theta1), sphereV(phi1), light, overlay);
                vertex(v, entry, matrix, phi1, theta0, sphereU(theta0), sphereV(phi1), light, overlay);
            }
        }
    }

    private static float sphereU(double theta) {
        return (float) (theta / (Math.PI * 2));
    }

    private static float sphereV(double phi) {
        return (float) (0.5 - phi / Math.PI);
    }

    private static void vertex(VertexConsumer v, MatrixStack.Entry entry, org.joml.Matrix4f matrix,
                               double phi, double theta, float u, float vv, int light, int overlay) {
        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);
        float nx = cosPhi * (float) Math.cos(theta);
        float ny = sinPhi;
        float nz = cosPhi * (float) Math.sin(theta);
        v.vertex(matrix, nx * RADIUS, ny * RADIUS, nz * RADIUS)
            .color(255, 255, 255, 255)
            .texture(u, vv)
            .overlay(overlay)
            .light(light)
            .normal(entry, nx, ny, nz);
    }

    /** R2 的程序化双层金色弧光：逐段闪烁，并随蓄力增加环数。 */
    private static void chargeArcs(MatrixStack matrices, VertexConsumerProvider consumers,
                                   float time, float charge) {
        VertexConsumer v = consumers.getBuffer(RenderLayer.getLightning());
        int rings = 1 + (int) (charge * 3);
        for (int ring = 0; ring < rings; ring++) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35 + ring * 54));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ring * 61 + time * 3));
            int segments = 26;
            for (int i = 0; i < segments; i++) {
                if ((i + ring + (int) (time * 2)) % 8 > 5) continue;
                double a = i * Math.PI * 2 / segments + time * .22;
                double b = (i + 1) * Math.PI * 2 / segments + time * .22;
                float radius = .29f + ring * .028f;
                float radiusA = radius + (float) Math.sin(i * 13 + Math.floor(time * 2)) * .025f;
                float radiusB = radius + (float) Math.sin((i + 1) * 13 + Math.floor(time * 2)) * .025f;
                float x1 = (float) Math.cos(a) * radiusA;
                float z1 = (float) Math.sin(a) * radiusA;
                float x2 = (float) Math.cos(b) * radiusB;
                float z2 = (float) Math.sin(b) * radiusB;
                float width = .006f + charge * .005f;
                arcQuad(v, matrices, x1, z1, x2, z2, width * 2.5f, 255, 169, 15, 75);
                arcQuad(v, matrices, x1, z1, x2, z2, width, 255, 230, 103, 235);
            }
            matrices.pop();
        }
    }

    private static void arcQuad(VertexConsumer v, MatrixStack matrices, float x1, float z1,
                                float x2, float z2, float width, int r, int g, int b, int alpha) {
        var matrix = matrices.peek().getPositionMatrix();
        v.vertex(matrix, x1, -width, z1).color(r, g, b, alpha);
        v.vertex(matrix, x2, -width, z2).color(r, g, b, alpha);
        v.vertex(matrix, x2, width, z2).color(r, g, b, alpha);
        v.vertex(matrix, x1, width, z1).color(r, g, b, alpha);
    }

    /** 最高蓄力时在玩家周围绘制间歇闪烁、保持静止的 charge 贴图叠层。 */
    public static void drawMaxChargeAura(MatrixStack matrices, VertexConsumerProvider consumers,
                                         float time, float tickDelta, int light) {
        // 让特效短暂出现后熄灭，避免玩家身上的叠层持续旋转造成视觉干扰。
        int flickerPhase = (int) Math.floor(time * 0.75f) % 5;
        if (flickerPhase >= 2) return;

        matrices.push();
        matrices.translate(0, 0.95f, 0);
        matrices.scale(1.15f, 1.15f, 1.15f);
        try {
            // 每个闪烁阶段只显示一张贴图，避免四张 charge 贴图同时叠加。
            int textureIndex = Math.floorMod((int) Math.floor(time * 1.5f), CHARGE_TEXTURES.length);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(textureIndex * 73f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(18f + textureIndex * 27f));
            float layerScale = 1f + textureIndex * 0.08f;
            matrices.scale(layerScale, layerScale, layerScale);
            // Charge 贴图包含透明辉光，必须使用带 alpha 混合的发光层。
            RenderLayer layer = RenderLayer.getEntityTranslucentEmissive(CHARGE_TEXTURES[textureIndex]);
            VertexConsumer v = consumers.getBuffer(layer);
            auraQuad(v, matrices.peek(), light);
        } finally {
            matrices.pop();
        }
    }

    private static void auraQuad(VertexConsumer v, MatrixStack.Entry entry, int light) {
        var matrix = entry.getPositionMatrix();
        auraVertex(v, entry, matrix, -0.72f, 0.72f, 0, 0, light);
        auraVertex(v, entry, matrix, 0.72f, 0.72f, 1, 0, light);
        auraVertex(v, entry, matrix, 0.72f, -0.72f, 1, 1, light);
        auraVertex(v, entry, matrix, -0.72f, -0.72f, 0, 1, light);
    }

    private static void auraVertex(VertexConsumer v, MatrixStack.Entry entry, org.joml.Matrix4f matrix,
                                    float x, float y, float u, float vv, int light) {
        v.vertex(matrix, x, y, 0)
            .color(255, 255, 255, 255)
            .texture(u, vv)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(entry, 0, 0, 1);
    }

    /** 在投射物后方绘制朝向摄像机的贴图带，连续覆盖球体到运动方向。 */
    public static void drawTrail(MatrixStack matrices, VertexConsumerProvider consumers, Vec3d velocity,
                                  Vec3d cameraOffset, float time, float power, int tint) {
        Vec3d axis = velocity.lengthSquared() > .0001 ? velocity.normalize() : new Vec3d(0, 0, 1);
        Vec3d view = cameraOffset.lengthSquared() > .0001 ? cameraOffset.normalize() : new Vec3d(0, 0, 1);
        Vec3d side = view.crossProduct(axis);
        if (side.lengthSquared() < .0001) side = new Vec3d(0, 1, 0).crossProduct(axis);
        side = side.normalize();
        int light = 0xF000F0;
        for (int i = 0; i < 7; i++) {
            float progress = i / 7f;
            double distance = .18 + progress * (.9 + power * 1.45);
            Vec3d center = axis.multiply(-distance);
            float halfLength = .12f + (1f - progress) * (.18f + power * .22f);
            float halfWidth = .11f + (1f - progress) * (.16f + power * .2f);
            Vec3d start = center.subtract(axis.multiply(halfLength));
            Vec3d end = center.add(axis.multiply(halfLength));
            Vec3d leftStart = start.subtract(side.multiply(halfWidth));
            Vec3d rightStart = start.add(side.multiply(halfWidth));
            Vec3d rightEnd = end.add(side.multiply(halfWidth));
            Vec3d leftEnd = end.subtract(side.multiply(halfWidth));
            VertexConsumer v = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(
                TRAIL_TEXTURES[(int) (Math.floor(time * 1.6f) + i) % TRAIL_TEXTURES.length]));
            trailQuad(v, matrices.peek(), leftStart, rightStart, rightEnd, leftEnd, overlayNone(), light, tint);
        }
    }

    private static int overlayNone() { return 0; }

    private static void texturedQuad(VertexConsumer v, MatrixStack.Entry entry, float left, float top,
                                     float right, float bottom, int overlay, int light,
                                     int r, int g, int b, int alpha) {
        var matrix = entry.getPositionMatrix();
        v.vertex(matrix, left, top, 0).color(r, g, b, alpha).texture(0, 0).overlay(overlay).light(light).normal(entry, 0, 0, 1);
        v.vertex(matrix, right, top, 0).color(r, g, b, alpha).texture(1, 0).overlay(overlay).light(light).normal(entry, 0, 0, 1);
        v.vertex(matrix, right, bottom, 0).color(r, g, b, alpha).texture(1, 1).overlay(overlay).light(light).normal(entry, 0, 0, 1);
        v.vertex(matrix, left, bottom, 0).color(r, g, b, alpha).texture(0, 1).overlay(overlay).light(light).normal(entry, 0, 0, 1);
    }

    private static void trailQuad(VertexConsumer v, MatrixStack.Entry entry, Vec3d leftStart, Vec3d rightStart,
                                  Vec3d rightEnd, Vec3d leftEnd, int overlay, int light, int tint) {
        var matrix = entry.getPositionMatrix();
        v.vertex(matrix, (float) leftStart.x, (float) leftStart.y, (float) leftStart.z)
            .color(255, 255, 255, 215).texture(0, 0).overlay(overlay).light(light).normal(entry, 0, 1, 0);
        v.vertex(matrix, (float) rightStart.x, (float) rightStart.y, (float) rightStart.z)
            .color(255, 255, 255, 215).texture(1, 0).overlay(overlay).light(light).normal(entry, 0, 1, 0);
        v.vertex(matrix, (float) rightEnd.x, (float) rightEnd.y, (float) rightEnd.z)
            .color(255, 255, 255, 195).texture(1, 1).overlay(overlay).light(light).normal(entry, 0, 1, 0);
        v.vertex(matrix, (float) leftEnd.x, (float) leftEnd.y, (float) leftEnd.z)
            .color(255, 255, 255, 195).texture(0, 1).overlay(overlay).light(light).normal(entry, 0, 1, 0);
    }
}
