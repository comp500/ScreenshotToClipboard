package link.infra.screenshotclipboard;

import link.infra.screenshotclipboard.common.MacOSCompat;
import link.infra.screenshotclipboard.common.ScreenshotToClipboard;
import me.ramidzkh.fabrishot.event.FramebufferCaptureCallback;
import me.ramidzkh.fabrishot.event.ScreenshotSaveCallback;

public class FabrishotCompat {
	public static void init() {
		FramebufferCaptureCallback.EVENT.register((dim, buffer) ->
			ScreenshotToClipboard.handleScreenshotAWT(buffer, dim.width, dim.height, 3));

		ScreenshotSaveCallback.EVENT.register(file ->
			MacOSCompat.doCopyMacOS(file.toString()));
	}
}
