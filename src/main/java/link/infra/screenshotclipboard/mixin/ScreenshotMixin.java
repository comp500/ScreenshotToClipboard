package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.MacOSCompat;
import link.infra.screenshotclipboard.ScreenshotToClipboard;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotUtils.class)
public class ScreenshotMixin {
	// Inject before it starts the file save thread (which would deallocate the NativeImage)
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Executor;execute(Ljava/lang/Runnable;)V", remap = false), method = "method_1662", locals = LocalCapture.CAPTURE_FAILHARD)
	private static void screenshotCaptured(File file_1, String string_1, int int_1, int int_2, GlFramebuffer glFramebuffer_1, Consumer consumer_1, CallbackInfo ci, NativeImage nativeImage_1, File file_2, File file_4) {
		ScreenshotToClipboard.handleScreenshot(nativeImage_1);
	}

	// Inject after saving the image
	// Lambda in method_1662 is called method_1661
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0), method = "method_1661")
	private static void screenshotCapturedMac(NativeImage nativeImage_1, File file_1, Consumer consumer_1, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file_1.getAbsolutePath());
	}
}