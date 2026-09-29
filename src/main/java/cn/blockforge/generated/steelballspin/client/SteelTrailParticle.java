package cn.blockforge.generated.steelballspin.client;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

/** 沿铁球飞行路径短暂滞留的发光尾迹粒子。 */
public final class SteelTrailParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;
    private final float startScale;

    private SteelTrailParticle(ClientWorld world, double x, double y, double z, SpriteProvider sprites) {
        super(world, x, y, z);
        this.sprites = sprites;
        this.red = 1f;
        this.green = 1f;
        this.blue = 1f;
        this.alpha = .82f;
        this.startScale = .11f + random.nextFloat() * .07f;
        this.scale = startScale;
        setSprite(sprites.getSprite(random));
        setMaxAge(14 + random.nextInt(10));
        setBoundingBoxSpacing(.01f, .01f);
    }

    @Override public void tick() {
        super.tick();
        if (!dead) {
            float progress = (age + 1f) / maxAge;
            alpha = .82f * (1f - progress) * (1f - progress);
            scale = startScale * (.92f + progress * .2f);
            if (age % 3 == 0) setSpriteForAge(sprites);
        }
    }

    @Override public int getBrightness(float tint) { return 0xF000F0; }

    @Override public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }

    public static final class Factory implements net.minecraft.client.particle.ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) { this.sprites = sprites; }

        @Override public SteelTrailParticle createParticle(SimpleParticleType parameters, ClientWorld world,
                                                            double x, double y, double z,
                                                            double velocityX, double velocityY, double velocityZ) {
            return new SteelTrailParticle(world, x, y, z, sprites);
        }
    }
}
