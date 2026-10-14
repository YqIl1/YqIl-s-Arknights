package com.YqIl1.yqils_arknights.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

// Purple four-point star (textures/particle/typhon_star.png). Full bright, drifts slowly, and shrinks +
// fades out over its short life so a stream of them reads as a soft twinkle.
public class TyphonStarParticle extends TextureSheetParticle {
    private final float startSize;

    protected TyphonStarParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        super(level, x, y, z, dx, dy, dz);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.friction = 0.85F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 10 + this.random.nextInt(8);
        this.startSize = 0.06F + this.random.nextFloat() * 0.06F;
        this.quadSize = this.startSize;
        this.roll = 0.0F;
        this.oRoll = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / (float) this.lifetime;
        this.quadSize = this.startSize * (1.0F - life * 0.7F);
        this.alpha = 1.0F - life * life;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0; // always full bright
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            TyphonStarParticle particle = new TyphonStarParticle(level, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
