package link.infra.screenshotclipboard.mixin;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public interface NativeImagePointerAccessor {
	@Accessor("pointer")
	long getPointer();
}
