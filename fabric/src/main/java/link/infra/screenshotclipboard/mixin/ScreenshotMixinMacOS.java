package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.common.MacOSCompat;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixinMacOS {
	// Inject after saving the image
	@Inject(at = @At("TAIL"), method = "method_1661")
	private static void screenshotCapturedMac(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file.getAbsolutePath());
	}
}