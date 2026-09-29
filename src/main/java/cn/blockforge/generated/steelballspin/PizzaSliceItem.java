package cn.blockforge.generated.steelballspin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

public final class PizzaSliceItem extends Item {
    public PizzaSliceItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!world.isClient) {
            world.playSound(null, user.getBlockPos(), ModSounds.PIZZA_EAT,
                SoundCategory.PLAYERS, ModSounds.volume(1f), .95f + world.random.nextFloat() * .1f);
        }
        return result;
    }
}
