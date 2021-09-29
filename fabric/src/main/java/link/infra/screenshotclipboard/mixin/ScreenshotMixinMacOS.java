package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.MacOSCompat;
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
public class ScreenshotMixinMacOS {
	// TODO: test macOS dev/prod
	// TODO: remove optifine?

	// Inject after saving the image, before accepting the consumer
	@Inject(at = @At("TAIL"), method = "method_1661")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCapturedMac")
	private static void screenshotCapturedMac(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file.getAbsolutePath());
	}

	// A specific Optifine mixin is required, as it messes with the method name and params (warning: MCP!)
	@Inject(at = @At("TAIL"), method = "lambda$saveScreenshotRaw$2")
	@Group(max = 1, min = 1, name = "screenshotclipboard-screenshotCapturedMac")
	private static void screenshotCapturedMacOptifine(NativeImage nativeImage, File file, Object o, Consumer<Text> consumer, CallbackInfo ci) {
		MacOSCompat.doCopyMacOS(file.getAbsolutePath());
	}
}