package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.common.MacOSCompat;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixinMacOS {
	// Inject after saving the image
	@Inject(at = @At("TAIL"), method = {
		"method_22691(Lnet/minecraft/client/texture/NativeImage;Ljava/io/File;Lnet/neoforged/neoforge/client/event/ScreenshotEvent;Ljava/util/function/Consumer;)V",
		// Remapping lambda methods in Forge/Architectury is pain :(
		"lambda$grab$1(Lnet/minecraft/client/texture/NativeImage;Ljava/io/File;Lnet/neoforged/neoforge/client/event/ScreenshotEvent;Ljava/util/function/Consumer;)V"
	})
	private static void screenshotCapturedMac(NativeImage nativeImage, File file1, ScreenshotEvent event, Consumer<Text> consumer, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(event.getScreenshotFile().getAbsolutePath());
	}
}
