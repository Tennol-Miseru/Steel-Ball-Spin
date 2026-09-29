package cn.blockforge.generated.steelballspin;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class SteelBallEntity extends ThrownItemEntity {
    private static final TrackedData<Integer> MODE = DataTracker.registerData(SteelBallEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> POWER = DataTracker.registerData(SteelBallEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> GOLDEN = DataTracker.registerData(SteelBallEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final Set<Integer> hitEntities = new HashSet<>();
    private boolean returning;
    private boolean creative;
    private int lifetime;
    private int vortexTicks;
    private int vortexAge;
    private boolean vortexStarted;
    private int blocksBroken;

    public SteelBallEntity(EntityType<? extends SteelBallEntity> type, World world) { super(type, world); }

    public SteelBallEntity(World world, LivingEntity owner, ItemStack stack, SpinMode mode, float power, boolean golden, boolean creative) {
        super(GeneratedMod.BALL, owner, world);
        setItem(stack);
        dataTracker.set(MODE, mode.ordinal());
        dataTracker.set(POWER, power);
        dataTracker.set(GOLDEN, golden);
        this.creative = creative;
        setNoGravity(true);
    }

    @Override protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MODE, 0);
        builder.add(POWER, 0f);
        builder.add(GOLDEN, false);
    }

    @Override protected Item getDefaultItem() { return GeneratedMod.STEEL_BALL; }
    public SpinMode mode() { return SpinMode.from(dataTracker.get(MODE)); }
    public float power() { return dataTracker.get(POWER); }
    public boolean golden() { return dataTracker.get(GOLDEN); }
    @Override protected double getGravity() { return 0; }

    @Override protected boolean canHit(Entity entity) {
        return !returning && vortexTicks == 0 && !hitEntities.contains(entity.getId()) && super.canHit(entity);
    }

    @Override public void tick() {
        if (getWorld().isClient) spawnTrailParticles();
        if (!getWorld().isClient) {
            lifetime++;
            if (getY() < getWorld().getBottomY() - 16) { finishReturn(); return; }
            if (vortexTicks > 0 || vortexStarted) {
                setVelocity(Vec3d.ZERO);
                vortexAge++;
                vortex();
                if (mode() != SpinMode.INFINITE && --vortexTicks == 0) returning = true;
            } else if (lifetime > 38 && !returning) returning = true;
            if (returning) {
                Entity owner = getOwner();
                if (owner == null || !owner.isAlive() || owner.getWorld() != getWorld()) { finishReturn(); return; }
                Vec3d delta = owner.getEyePos().add(0, -.5, 0).subtract(getPos());
                if (delta.lengthSquared() < 2.25) { finishReturn(); return; }
                setVelocity(delta.normalize().multiply(1.5 + power()));
                noClip = true;
            }
        }
        super.tick();
    }

    private boolean canAffect(Entity target) {
        if (!(target instanceof LivingEntity living) || target == getOwner() || !living.isAlive() || target.isSpectator()) return false;
        if (getOwner() != null && getOwner().isTeammate(target)) return false;
        return !(target instanceof PlayerEntity p && getOwner() instanceof PlayerEntity owner && !owner.shouldDamagePlayer(p));
    }

    @Override protected void onEntityHit(EntityHitResult hit) {
        if (getWorld().isClient || returning || vortexTicks > 0) return;
        Entity target = hit.getEntity();
        setPosition(hit.getPos());
        hitEntities.add(target.getId());
        if (canAffect(target)) {
            float hitDamage = mode() == SpinMode.PIERCING ? 8f + power() * 18f : mode().damage * (.35f + .65f * power());
            target.damage(getDamageSources().thrown(this, getOwner()), hitDamage);
            Vec3d direction = getVelocity().lengthSquared() > .001 ? getVelocity().normalize() : getRotationVector();
            if (mode() != SpinMode.TRANSFER && mode() != SpinMode.INFINITE) {
                target.addVelocity(direction.multiply(mode() == SpinMode.PIERCING ? 1.4 + power() * 1.4 : .4 + power() * .8));
            }
            if (target instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, mode() == SpinMode.PIERCING ? 80 : 50,
                    mode() == SpinMode.PIERCING ? 2 : 1));
                if (mode() == SpinMode.TRANSFER) applyTransferSpin(living);
                if (mode() == SpinMode.INFINITE) applyEndlessSpin(living);
            }
        }
        impact(true);
        if (mode() == SpinMode.INFINITE) startVortex();
        else if (mode() != SpinMode.PIERCING && mode() != SpinMode.TRANSFER) returning = true;
    }

    @Override protected void onBlockHit(BlockHitResult hit) {
        if (getWorld().isClient || returning || vortexTicks > 0) return;
        setPosition(hit.getPos());
        impact(false);
        if (mode() == SpinMode.PIERCING) {
            int burst = power() >= .66f ? 8 : 5;
            int radius = power() >= .8f ? 1 : 0;
            boolean broken = false;
            for (int i = 0; i < burst; i++) {
                BlockPos drill = hit.getBlockPos().offset(hit.getSide(), i);
                broken |= breakOne(drill);
                if (radius > 0) {
                    for (int a = -radius; a <= radius; a++) for (int b = -radius; b <= radius; b++) {
                        BlockPos side = hit.getBlockPos().offset(hit.getSide(), i).add(a, b, 0);
                        broken |= breakOne(side);
                    }
                }
            }
            if (!broken || blocksBroken >= 160) returning = true;
            else setPosition(getPos().add(hit.getSide().getOffsetX() * .7, hit.getSide().getOffsetY() * .7, hit.getSide().getOffsetZ() * .7));
        } else if (mode() == SpinMode.INFINITE) {
            setPosition(hit.getPos().add(hit.getSide().getOffsetX() * .35, hit.getSide().getOffsetY() * .35, hit.getSide().getOffsetZ() * .35));
            startVortex();
        } else if (golden()) {
            destroySphere(hit.getBlockPos(), power() >= .66f ? 3 : 2);
            returning = true;
        } else returning = true;
    }

    private void applyTransferSpin(LivingEntity living) {
        int duration = 160 + (int) (power() * 100);
        living.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(GeneratedMod.SPIN_TRANSFER), duration,
            power() >= .66f ? 2 : 1, false, true, true));
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getBodyY(.5), living.getZ(), 8, .3, .4, .3, .04);
            getWorld().playSound(null, living.getX(), living.getY(), living.getZ(), ModSounds.TRANSFER, SoundCategory.PLAYERS, ModSounds.volume(.9f), 1.8f);
        }
    }

    private void applyEndlessSpin(LivingEntity living) {
        // 将旧版本永久回旋转为独立状态，避免两个状态重复扣血。
        var legacy = living.getStatusEffect(Registries.STATUS_EFFECT.getEntry(GeneratedMod.SPIN_TRANSFER));
        if (legacy != null && legacy.isInfinite()) living.removeStatusEffect(Registries.STATUS_EFFECT.getEntry(GeneratedMod.SPIN_TRANSFER));
        living.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(GeneratedMod.INFINITE_SPIN),
            StatusEffectInstance.INFINITE, 0, false, false, true));
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getBodyY(.5), living.getZ(),
                18, .45, .65, .45, .06);
        }
    }

    private void startVortex() {
        if (vortexStarted) return;
        vortexStarted = true;
        vortexTicks = mode() == SpinMode.INFINITE ? Integer.MAX_VALUE : 50 + (int) (power() * 90);
        vortexAge = 0;
        setVelocity(Vec3d.ZERO);
        if (getWorld() instanceof ServerWorld) getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.VORTEX_START, SoundCategory.PLAYERS, ModSounds.volume(.8f), 1.2f);
    }

    private void shockwave() {
        double radius = 2.5 + power() * 2.8;
        for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(radius), this::canAffect)) {
            if (e.squaredDistanceTo(this) > radius * radius) continue;
            if (!hitEntities.contains(e.getId())) e.damage(getDamageSources().thrown(this, getOwner()), 10 + power() * 12);
            Vec3d push = e.getPos().subtract(getPos());
            if (push.lengthSquared() > .001) e.addVelocity(push.normalize().multiply(1.05 + power()).add(0, .5, 0));
        }
        if (getWorld() instanceof ServerWorld sw) {
            for (int i = 0; i < 36; i++) {
                double a = i * Math.PI / 18;
                sw.spawnParticles(ParticleTypes.WAX_ON, getX() + Math.cos(a) * radius, getY() + .15, getZ() + Math.sin(a) * radius,
                    1, 0, .08, 0, .02);
            }
            getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.SHOCKWAVE, SoundCategory.PLAYERS, ModSounds.volume(1.05f), .65f);
        }
    }

    private void vortex() {
        double radius = 2.5 + power() * 2.5;
        for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(radius), this::canAffect)) {
            Vec3d delta = getPos().subtract(e.getPos());
            if (delta.lengthSquared() > radius * radius || delta.lengthSquared() < .001) continue;
            e.addVelocity(delta.normalize().multiply(.14 + power() * .1).add(-delta.z * .035, .035, delta.x * .035));
            if (vortexAge % 8 == 0) {
                e.damage(getDamageSources().thrown(this, getOwner()), 4 + power() * 5);
                if (e instanceof LivingEntity living) living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 2));
            }
        }
        if (getWorld() instanceof ServerWorld sw && vortexAge % 2 == 0) {
            for (int i = 0; i < 24; i++) {
                double a = i * .52 + lifetime * .4;
                double r = .35 + i * radius / 24;
                sw.spawnParticles(ParticleTypes.WAX_ON, getX() + Math.cos(a) * r, getY() + i * .08, getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
                sw.spawnParticles(ParticleTypes.PORTAL, getX() + Math.sin(a) * r, getY() + .4, getZ() + Math.cos(a) * r, 1, 0, .1, 0, .04);
            }
            if (vortexAge % 16 == 0) destroySphere(getBlockPos(), power() > .8 ? 2 : 1);
        }
    }

    private void spawnTrailParticles() {
        Vec3d velocity = getVelocity();
        double distance = velocity.length();
        if (distance < .01) return;
        Vec3d direction = velocity.normalize();
        Vec3d previous = getPos().subtract(velocity);
        int samples = Math.max(1, Math.min(8, (int) Math.ceil(distance / .22)));
        for (int i = 0; i < samples; i++) {
            double progress = (i + .5) / samples;
            Vec3d point = previous.lerp(getPos(), progress);
            getWorld().addParticle(GeneratedMod.STEEL_TRAIL, true, point.x, point.y + .25, point.z, 0, 0, 0);
        }
    }

    private boolean breakOne(BlockPos pos) {
        if (!(getOwner() instanceof PlayerEntity player) || !player.getAbilities().allowModifyWorld || blocksBroken >= 160) return false;
        if (!getWorld().canPlayerModifyAt(player, pos)) return false;
        var state = getWorld().getBlockState(pos);
        float hardness = state.getHardness(getWorld(), pos);
        if (state.isAir() || state.hasBlockEntity() || hardness < 0 || hardness > 8 || !state.getFluidState().isEmpty()) return false;
        if (getWorld().breakBlock(pos, true, player, 512)) { blocksBroken++; return true; }
        return false;
    }

    private void destroySphere(BlockPos center, int radius) {
        if (power() < .25f) return;
        for (BlockPos p : BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius)))
            if (p.getSquaredDistance(center) <= radius * radius) breakOne(p);
    }

    private void impact(boolean entityHit) {
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 36, .4, .4, .4, .2);
            sw.spawnParticles(ParticleTypes.WAX_ON, getX(), getY(), getZ(), 36, .4, .4, .4, .2);
            if (entityHit) {
                getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.IMPACT_ENTITY,
                    SoundCategory.PLAYERS, ModSounds.volume(1.1f), .9f);
            } else if (golden()) {
                getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.IMPACT_GOLDEN,
                    SoundCategory.PLAYERS, ModSounds.volume(1.1f), .7f);
            } else {
                getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.IMPACT_BLOCK,
                    SoundCategory.PLAYERS, ModSounds.volume(.9f), .55f);
            }
        }
    }

    private void finishReturn() {
        if (!creative) {
            ItemStack stack = getStack().copy();
            if (getOwner() instanceof PlayerEntity p && p.isAlive() && p.getWorld() == getWorld()) {
                if (!p.getInventory().insertStack(stack)) p.dropItem(stack, false);
            } else dropStack(stack);
        }
        discard();
    }

    @Override public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SpinMode", mode().ordinal());
        nbt.putFloat("Power", power());
        nbt.putBoolean("Golden", golden());
        nbt.putBoolean("Creative", creative);
        nbt.putInt("Lifetime", lifetime);
        nbt.putBoolean("Returning", returning);
        nbt.putInt("Vortex", vortexTicks);
        nbt.putInt("VortexAge", vortexAge);
        nbt.putBoolean("VortexStarted", vortexStarted);
        nbt.putInt("Broken", blocksBroken);
    }

    @Override public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(MODE, SpinMode.from(nbt.getInt("SpinMode")).ordinal());
        dataTracker.set(POWER, Math.max(0, Math.min(1, nbt.getFloat("Power"))));
        dataTracker.set(GOLDEN, nbt.getBoolean("Golden"));
        creative = nbt.getBoolean("Creative");
        lifetime = nbt.getInt("Lifetime");
        returning = nbt.getBoolean("Returning");
        vortexTicks = nbt.getInt("Vortex");
        vortexAge = nbt.getInt("VortexAge");
        vortexStarted = nbt.getBoolean("VortexStarted");
        blocksBroken = nbt.getInt("Broken");
        setNoGravity(true);
    }
}
