package foundationgames.blasttravel.client.particle;

import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class CannonBlastParticle extends AbstractSlowingParticle {
	private final SpriteProvider sprites;

	public CannonBlastParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
		super(world, x, y, z, vx, vy, vz);
		this.sprites = sprites;
		this.age = world.random.nextInt(2);
		this.maxAge = 16;
		this.velocityMultiplier = 0.76f;
		this.scale(1.5f + world.random.nextFloat() * 0.2f);
		this.setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteForAge(this.sprites);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}

	public static class Factory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider sprites;

		public Factory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
			return new CannonBlastParticle(world, x, y, z, vx, vy, vz, this.sprites);
		}
	}
}
