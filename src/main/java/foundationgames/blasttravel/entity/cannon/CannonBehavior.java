package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CannonBehavior {
	protected static final Vec3f WHITE = new Vec3f(1, 1, 1);
	private static final List<CannonBehavior> ID_TO_BEHAVIOR = new ArrayList<>();
	private static final Object2IntMap<Predicate<ItemStack>> FILTER_TO_BEHAVIOR_ID = new Object2IntOpenHashMap<>();

	public final Item icon;
	public final Predicate<ItemStack> filter;
	private final Identifier texture;

	public CannonBehavior(Item item, Identifier texture) {
		this(item, stack -> stack.getItem() == item, texture);
	}

	public CannonBehavior(Item icon, Predicate<ItemStack> filter) {
		this(icon, filter, BlastTravel.id("textures/entity/cannon/regular.png"));
	}

	public CannonBehavior(Item icon, Predicate<ItemStack> filter, Identifier texture) {
		this.icon = icon;
		this.filter = filter;
		this.texture = texture;
	}

	public CannonBehavior register() {
		FILTER_TO_BEHAVIOR_ID.put(this.filter, ID_TO_BEHAVIOR.size());
		ID_TO_BEHAVIOR.add(this);

		return this;
	}

	@Environment(EnvType.CLIENT)
	public boolean displayHead(CannonEntity entity) {
		return entity.getClientPlayer() != null;
	}

	public boolean occupiesCannon(ItemStack behaviorStack) {
		return false;
	}

	public void onFired(CannonEntity cannon, ItemStack behaviorStack, Vec3d velocity) {
	}

	public static CannonBehavior byId(int id) {
		return ID_TO_BEHAVIOR.get(id);
	}

	public static int idForStack(ItemStack stack) {
		for (var e : FILTER_TO_BEHAVIOR_ID.object2IntEntrySet()) {
			if (e.getKey().test(stack)) {
				return e.getIntValue();
			}
		}
		return 0;
	}

	public static boolean isValidBehaviorStack(ItemStack stack) {
		return FILTER_TO_BEHAVIOR_ID.object2IntEntrySet().stream().anyMatch(e -> e.getKey().test(stack));
	}

	public static Collection<CannonBehavior> allBehaviors() {
		return ID_TO_BEHAVIOR;
	}

	@Environment(EnvType.CLIENT)
	public Identifier texture(ItemStack stack) {
		return texture;
	}

	@Environment(EnvType.CLIENT)
	public Identifier headTexture(CannonEntity entity) {
		var player = entity.getClientPlayer();
		if (player == null) {
			return new Identifier("missing");
		}

		return player.getSkinTexture();
	}

	@Environment(EnvType.CLIENT)
	public Vec3f headColor(CannonEntity entity) {
		return WHITE;
	}

	@Environment(EnvType.CLIENT)
	public @Nullable Vec3f fireColor(CannonEntity entity) {
		return null;
	}
}
