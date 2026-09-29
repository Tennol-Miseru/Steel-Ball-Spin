package cn.blockforge.generated.steelballspin;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public final class ModSounds {
    private static final float VOLUME_MULTIPLIER = .35f;

    public static final SoundEvent CHARGE_START = register("charge_start");
    public static final SoundEvent CHARGE_STAGE_1 = register("charge_stage_1");
    public static final SoundEvent CHARGE_STAGE_2 = register("charge_stage_2");
    public static final SoundEvent CHARGE_STAGE_3 = register("charge_stage_3");
    public static final SoundEvent THROW = register("throw");
    public static final SoundEvent THROW_GOLDEN = register("throw_golden");
    public static final SoundEvent IMPACT_ENTITY = register("impact_entity");
    public static final SoundEvent IMPACT_GOLDEN = register("impact_golden");
    public static final SoundEvent IMPACT_BLOCK = register("impact_block");
    public static final SoundEvent TRANSFER = register("transfer");
    public static final SoundEvent VORTEX_START = register("vortex_start");
    public static final SoundEvent SHOCKWAVE = register("shockwave");
    public static final SoundEvent PIZZA_EAT = register("pizza_eat");

    private ModSounds() {}

    private static SoundEvent register(String path) {
        var id = GeneratedMod.id(path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init() {}

    public static float volume(float baseVolume) {
        return baseVolume * VOLUME_MULTIPLIER;
    }
}
