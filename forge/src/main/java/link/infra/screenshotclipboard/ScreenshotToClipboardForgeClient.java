package link.infra.screenshotclipboard;

import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;

public class ScreenshotToClipboardForgeClient {
	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(ScreenshotToClipboardForgeClient::handleScreenshot);
		ScreenshotToClipboard.init();
	}

	public static void handleScreenshot(ScreenshotEvent event) {
		ScreenshotToClipboard.handleScreenshotAWT(event.getImage());
	}
}
