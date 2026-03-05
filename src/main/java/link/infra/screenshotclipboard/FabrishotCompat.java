package link.infra.screenshotclipboard;

import me.ramidzkh.fabrishot.event.FramebufferCaptureCallback;
import me.ramidzkh.fabrishot.event.ScreenshotSaveCallback;

public class FabrishotCompat {
	public static void init() {
		FramebufferCaptureCallback.EVENT.register(image ->
			ScreenshotToClipboard.handleScreenshotAWT(image));

		ScreenshotSaveCallback.EVENT.register(file ->
			MacOSCompat.doCopyMacOS(file.toString()));
	}
}
