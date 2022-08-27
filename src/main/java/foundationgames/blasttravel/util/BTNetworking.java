package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public enum BTNetworking {;
	@Environment(EnvType.CLIENT)
	public static void c2sRequestFire(CannonEntity entity) {
		var buf = PacketByteBufs.create();
		buf.writeInt(entity.getId());
		ClientPlayNetworking.send(BlastTravel.id("request_fire"), buf);
	}

	@Environment(EnvType.CLIENT)
	public static void c2sStopCannonFlight(boolean thud) {
		var buf = PacketByteBufs.create();
		buf.writeBoolean(thud);
		ClientPlayNetworking.send(BlastTravel.id("stop_cannon_flight"), buf);
	}

	public static void s2cFireCannon(ServerPlayerEntity to, CannonEntity cannon, @Nullable PlayerEntity launched, Vec3d velocity) {
		var buf = PacketByteBufs.create();
		buf.writeInt(cannon.getId());
		buf.writeDouble(velocity.x);
		buf.writeDouble(velocity.y);
		buf.writeDouble(velocity.z);
		buf.writeBoolean(launched != null);
		if (launched != null) {
			buf.writeInt(launched.getId());
		}
		ServerPlayNetworking.send(to, BlastTravel.id("fire_cannon"), buf);
	}

	public static void s2cStopCannonFlight(ServerPlayerEntity to, PlayerEntity flying) {
		var buf = PacketByteBufs.create();
		buf.writeInt(flying.getId());
		ServerPlayNetworking.send(to, BlastTravel.id("stop_cannon_flight"), buf);
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(BlastTravel.id("request_fire"), (server, player, handler, buf, responseSender) -> {
			int id = buf.readInt();
			server.execute(() -> {
				var entity = player.getWorld().getEntityById(id);
				if (entity instanceof CannonEntity cannon) {
					cannon.fireServer();
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(BlastTravel.id("stop_cannon_flight"), (server, launched, handler, buf, responseSender) -> {
			boolean thud = buf.readBoolean();
			server.execute(() -> {
				((PlayerEntityDuck)launched).blasttravel$setCannonFlight(false);
				if (launched.world instanceof ServerWorld world) {
					world.getPlayers().forEach(p -> s2cStopCannonFlight(p, launched));
				}

				if (thud) {
					launched.world.playSound(null, launched.getX(), launched.getY(), launched.getZ(),
							SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.PLAYERS, 1, 0.78f);
				}
			});
		});
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(BlastTravel.id("fire_cannon"), (client, handler, buf, responseSender) -> {
			int cannonId = buf.readInt();
			var vel = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			boolean hasPlayer = buf.readBoolean();
			int playerId = -1;
			if (hasPlayer) {
				playerId = buf.readInt();
			}
			int playerIdButJavaWontComplainAboutItNotBeingEffectivelyFinal = playerId;
			client.execute(() -> {
				if (hasPlayer) {
					var entity = client.world.getEntityById(playerIdButJavaWontComplainAboutItNotBeingEffectivelyFinal);
					if (entity instanceof PlayerEntity player) {
						player.getAbilities().flying = false;
						player.setVelocity(vel);
						((PlayerEntityDuck)player).blasttravel$setCannonFlight(true);
					}
				}

				var entity = client.world.getEntityById(cannonId);
				if (entity instanceof CannonEntity cannon) {
					cannon.fireClient();
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(BlastTravel.id("stop_cannon_flight"), (client, handler, buf, responseSender) -> {
			int playerId = buf.readInt();
			client.execute(() -> {
				var entity = client.world.getEntityById(playerId);
				if (entity instanceof PlayerEntityDuck player && entity != client.player) {
					player.blasttravel$setCannonFlight(false);
				}
			});
		});
	}
}
