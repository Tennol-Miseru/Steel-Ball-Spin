package cn.blockforge.generated.steelballspin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

public final class InfiniteSpinEffect extends StatusEffect {
    public InfiniteSpinEffect() { super(StatusEffectCategory.HARMFUL, 0xD9ACFF); }

    @Override public boolean canApplyUpdateEffect(int duration, int amplifier) { return true; }

    @Override public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) tickSpin(entity);
        return true;
    }

    static void tickSpin(LivingEntity entity) {
        // 状态保存在目标身上，不引用铁球；无限时长也不会阻止每 20 tick 结算。
        if (entity.age % 20 == 0) entity.damage(entity.getDamageSources().magic(), 2f);
        spawnHelix(entity);
    }

    private static void spawnHelix(LivingEntity entity) {
        if (!(entity.getWorld() instanceof ServerWorld world) || entity.age % 2 != 0) return;
        double phase = entity.age * .55 + entity.getId();
        double radius = entity.getWidth() * .55 + .18;
        double height = Math.max(.8, entity.getHeight());
        for (int i = 0; i < 10; i++) {
            double fraction = i / 9.0;
            double angle = phase + fraction * Math.PI * 4;
            world.spawnParticles(ParticleTypes.WAX_ON,
                entity.getX() + Math.cos(angle) * radius,
                entity.getY() + fraction * height,
                entity.getZ() + Math.sin(angle) * radius,
                1, 0, 0, 0, 0);
        }
    }
}
