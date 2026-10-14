package com.YqIl1.yqils_arknights.particle;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Central place to register every ParticleType under the "yqils_arknights" namespace.
// Each one also needs particles/<name>.json + textures/particle/<name>.png and a client provider
// (registered in Yqils_arknights.ClientModEvents#registerParticleProviders).
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Yqils_arknights.MODID);

    // Purple four-point star that gathers in front of Typhon's Greatbow while it's drawn during the skill.
    public static final RegistryObject<SimpleParticleType> TYPHON_STAR =
            PARTICLE_TYPES.register("typhon_star", () -> new SimpleParticleType(false));
}
