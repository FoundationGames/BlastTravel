package foundationgames.blasttravel;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.item.CannonItem;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlastTravel implements ModInitializer {
	public static final String MOD_ID = "blasttravel";
	public static final Logger LOG = LoggerFactory.getLogger("Blast Travel");

	public static final EntityType<CannonEntity> CANNON = Registry.register(Registry.ENTITY_TYPE, id("cannon"),
			FabricEntityTypeBuilder.<CannonEntity>create()
					.entityFactory(CannonEntity::new)
					.dimensions(EntityDimensions.fixed(1, 0.8f))
					.build());

	public static final ScreenHandlerType<CannonScreenHandler> CANNON_SCREEN_HANDLER = Registry.register(
			Registry.SCREEN_HANDLER, id("cannon"), new ScreenHandlerType<>(CannonScreenHandler::new));

	public static final Item CANNON_ITEM = Registry.register(Registry.ITEM, id("cannon"),
			new CannonItem(new Item.Settings().group(ItemGroup.TRANSPORTATION)));

	public static final DefaultParticleType CANNON_BLAST = Registry.register(Registry.PARTICLE_TYPE,
			id("cannon_blast"), FabricParticleTypes.simple(true));

	public static final TrackedDataHandler<ItemStack> ITEM_STACK_HANDLER = TrackedDataHandler.create(PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);

	@Override
	public void onInitialize(ModContainer mod) {
		BTNetworking.init();

		TrackedDataHandlerRegistry.register(ITEM_STACK_HANDLER);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
