package link.infra.screenshotclipboard.mixin;

import link.infra.screenshotclipboard.MacOSCompat;
import link.infra.screenshotclipboard.ScreenshotToClipboard;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixinAWT {
	// Wrap the Consumer<NativeImage> callback passed to takeScreenshot within saveScreenshot,
	// so we can copy the image to clipboard before it gets saved to disk
	@ModifyArg(
		method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ScreenshotRecorder;takeScreenshot(Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V"),
		index = 2
	)
	private static Consumer<NativeImage> wrapScreenshotCallback(Consumer<NativeImage> original) {
		return image -> {
			if (Util.getOperatingSystem() == Util.OperatingSystem.OSX) {
				// On macOS, AWT and GLFW conflict, so write to a temp file
				// and use the Objective-C bridge to copy to pasteboard
				original.accept(image);
				try {
					File tempFile = File.createTempFile("screenshot_clipboard_", ".png");
					tempFile.deleteOnExit();
					image.writeTo(tempFile);
					MacOSCompat.doCopyMacOS(tempFile.getAbsolutePath());
				} catch (IOException e) {
					// Silently fail - the image still gets saved normally by Minecraft
				}
			} else {
				// On Windows/Linux, copy image data to clipboard via AWT
				ScreenshotToClipboard.handleScreenshotAWT(image);
				original.accept(image);
			}
		};
	}
}
