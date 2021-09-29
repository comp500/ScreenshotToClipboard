package link.infra.screenshotclipboard;

import net.fabricmc.api.ClientModInitializer;

public class ScreenshotToClipboardFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ScreenshotToClipboard.init();
	}
}
