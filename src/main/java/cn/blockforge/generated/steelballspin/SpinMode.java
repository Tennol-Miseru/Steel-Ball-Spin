package cn.blockforge.generated.steelballspin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public enum SpinMode {
    BASIC("basic", 0x75DEA0, 9, 12),
    PIERCING("piercing", 0x6BE6F5, 18, 22),
    INFINITE("infinite", 0xD9ACFF, 22, 60),
    TRANSFER("transfer", 0x57DDF2, 15, 36);

    public final String key;
    public final int color;
    public final float damage;
    public final int cooldown;

    SpinMode(String key, int color, float damage, int cooldown) {
        this.key = key;
        this.color = color;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    public Text title() { return Text.translatable("spin.steel_ball_spin." + key); }
    public static SpinMode from(int id) { return values()[Math.max(0, Math.min(values().length - 1, id))]; }

    public static SpinMode get(ItemStack stack) {
        return from(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("SpinMode"));
    }

    public static void set(ItemStack stack, int id) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt("SpinMode", from(id).ordinal()));
    }
}
