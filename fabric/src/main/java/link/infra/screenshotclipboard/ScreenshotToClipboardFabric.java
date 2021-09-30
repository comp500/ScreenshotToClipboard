package link.infra.screenshotclipboard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ScreenshotToClipboardFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ScreenshotToClipboard.init();

		if (FabricLoader.getInstance().isModLoaded("fabrishot")) {
			FabrishotCompat.init();
		}
	}
}
