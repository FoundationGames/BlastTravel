package foundationgames.blasttravel.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccess {
	@Accessor("noDrag")
	void blasttravel$setNoDrag(boolean noDrag);
}
