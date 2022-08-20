package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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
	}
}
