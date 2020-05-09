package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.MacOSCompat;
import link.infra.screenshotclipboard.ScreenshotToClipboard;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotUtils.class)
public class ScreenshotMixin {
	// Lambda in method_1662 is called method_1661

	// Inject before it starts saving the file (HEAD is the safest place to do this)
	@Inject(at = @At(value = "HEAD"), method = "method_1661")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCaptured")
	private static void screenshotCaptured(NativeImage nativeImage_1, File file_1, Consumer<Text> consumer_1, CallbackInfo ci) {
		ScreenshotToClipboard.handleScreenshot(nativeImage_1);
	}

	// Inject after saving the image, before accepting the consumer
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0, remap = false), method = "method_1661")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCapturedMac")
	private static void screenshotCapturedMac(NativeImage nativeImage_1, File file_1, Consumer<Text> consumer_1, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file_1.getAbsolutePath());
	}

	// Inject before it starts saving the file (HEAD is the safest place to do this)
	@Inject(at = @At(value = "HEAD"), method = "lambda$saveScreenshotRaw$2")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCaptured")
	private static void screenshotCapturedOptifine(NativeImage nativeImage_1, File file_1, Object o, Consumer<Text> consumer_1, CallbackInfo ci) {
		ScreenshotToClipboard.handleScreenshot(nativeImage_1);
	}

	// Inject after saving the image, before accepting the consumer
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1, remap = false), method = "lambda$saveScreenshotRaw$2")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCapturedMac")
	private static void screenshotCapturedMacOptifine(NativeImage nativeImage_1, File file_1, Object o, Consumer<Text> consumer_1, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file_1.getAbsolutePath());
	}
}