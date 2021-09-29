package link.infra.screenshotclipboard.mixin;

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
public class ScreenshotMixinAWT {
	// Lambda in method_1662 is called method_1661
	// Inject before it starts saving the file (HEAD is the safest place to do this)

	@Inject(at = @At(value = "HEAD"), method = "method_1661")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCaptured")
	private static void screenshotCaptured(NativeImage nativeImage_1, File file_1, Consumer<Text> consumer_1, CallbackInfo ci) {
		ScreenshotToClipboard.handleScreenshotAWT(nativeImage_1);
	}

	// A specific Optifine mixin is required on Fabric, as Optifabric messes with the method name
	@Inject(at = @At(value = "HEAD"), method = "lambda$saveScreenshotRaw$2")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCaptured")
	private static void screenshotCapturedOptifine(NativeImage nativeImage_1, File file_1, Object o, Consumer<Text> consumer_1, CallbackInfo ci) {
		ScreenshotToClipboard.handleScreenshotAWT(nativeImage_1);
	}
}