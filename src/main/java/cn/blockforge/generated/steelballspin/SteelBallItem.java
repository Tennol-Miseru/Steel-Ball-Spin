package cn.blockforge.generated.steelballspin;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public final class SteelBallItem extends Item {
    public static final int MAX_CHARGE_TICKS = 60;

    public SteelBallItem(Settings settings) { super(settings); }
    public static float charge(int ticks) { return Math.min(1f, ticks / (float) MAX_CHARGE_TICKS); }
    public static boolean goldenCharge(int ticks) { return ticks >= 40; }

    public static boolean isRidingHorse(LivingEntity user) {
        Entity vehicle = user.getVehicle();
        return vehicle != null && vehicle.isAlive() && vehicle instanceof AbstractHorseEntity;
    }

    private static boolean canUseMode(ItemStack stack, LivingEntity user) {
        return SpinMode.get(stack) != SpinMode.INFINITE || isRidingHorse(user);
    }

    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BLOCK; }

    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!canUseMode(stack, user)) return TypedActionResult.fail(stack);
        user.setCurrentHand(hand);
        if (!world.isClient) world.playSound(null, user.getBlockPos(), ModSounds.CHARGE_START,
            SoundCategory.PLAYERS, ModSounds.volume(.5f), 1.1f);
        return TypedActionResult.consume(stack);
    }

    @Override public void usageTick(World world, LivingEntity user, ItemStack stack, int remaining) {
        if (!canUseMode(stack, user)) {
            user.clearActiveItem();
            return;
        }
        int ticks = getMaxUseTime(stack, user) - remaining;
        if (!world.isClient && (ticks == 20 || ticks == 40 || ticks == 60)) {
            var sound = ticks == 20 ? ModSounds.CHARGE_STAGE_1
                : ticks == 40 ? ModSounds.CHARGE_STAGE_2 : ModSounds.CHARGE_STAGE_3;
            world.playSound(null, user.getBlockPos(), sound, SoundCategory.PLAYERS, ModSounds.volume(.8f), 1f + ticks / 200f);
        }
    }

    @Override public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remaining) {
        if (!(user instanceof PlayerEntity player) || world.isClient || !canUseMode(stack, user)) return;
        int ticks = getMaxUseTime(stack, user) - remaining;
        SpinMode mode = SpinMode.get(stack);
        float power = charge(ticks);
        boolean golden = goldenCharge(ticks);
        SteelBallEntity ball = new SteelBallEntity(world, player, stack.copyWithCount(1), mode, power, golden, player.getAbilities().creativeMode);
        float speed = mode == SpinMode.PIERCING ? 1.05f : 1.45f + power * 2.15f;
        ball.setVelocity(player, player.getPitch(), player.getYaw(), 0, speed, .25f);
        world.spawnEntity(ball);
        if (!player.getAbilities().creativeMode) stack.decrement(1);
        player.getItemCooldownManager().set(this, mode.cooldown);
        world.playSound(null, player.getBlockPos(), golden ? ModSounds.THROW_GOLDEN : ModSounds.THROW,
            SoundCategory.PLAYERS, ModSounds.volume(golden ? 1.2f : 1f), golden ? .9f : .9f + power * .25f);
    }

    @Override public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(SpinMode.get(stack).title().copy().formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("tooltip.steel_ball_spin.controls").formatted(Formatting.GRAY));
    }
}
