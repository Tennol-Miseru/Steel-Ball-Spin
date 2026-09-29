package cn.blockforge.generated.steelballspin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class SpinTransferEffect extends StatusEffect {
    public SpinTransferEffect() { super(StatusEffectCategory.HARMFUL, 0x57DDF2); }

    @Override public boolean canApplyUpdateEffect(int duration, int amplifier) { return true; }

    @Override public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return true;
        // 旧存档中复用此效果的永久回旋仍按无限回旋结算。
        StatusEffectInstance effect = entity.getStatusEffect(Registries.STATUS_EFFECT.getEntry(GeneratedMod.SPIN_TRANSFER));
        if (effect != null && effect.isInfinite()) {
            InfiniteSpinEffect.tickSpin(entity);
            return true;
        }
        double angle = (entity.age + entity.getId() * 17) * .42;
        Vec3d swirl = new Vec3d(Math.cos(angle) * (.075 + amplifier * .018), 0, Math.sin(angle) * (.075 + amplifier * .018));
        entity.addVelocity(swirl);
        entity.setVelocity(entity.getVelocity().multiply(.78, .2, .78).add(swirl));
        if (entity.age % 8 == 0) {
            entity.damage(entity.getDamageSources().magic(), 1.5f + amplifier * .75f);
            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getBodyY(.5), entity.getZ(),
                    5 + amplifier * 2, .25, .35, .25, .035);
                sw.spawnParticles(ParticleTypes.WAX_ON, entity.getX(), entity.getBodyY(.5), entity.getZ(),
                    2 + amplifier, .2, .25, .2, .01);
            }
        }
        return true;
    }
}
