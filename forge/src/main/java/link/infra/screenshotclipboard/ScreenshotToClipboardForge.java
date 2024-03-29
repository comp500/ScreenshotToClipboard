package link.infra.screenshotclipboard;

import link.infra.screenshotclipboard.common.ScreenshotToClipboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.network.NetworkConstants;

@Mod(ScreenshotToClipboard.MOD_ID)
public class ScreenshotToClipboardForge {
	public ScreenshotToClipboardForge() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		if (Environment.get().getDist() == Dist.CLIENT) {
			ScreenshotToClipboardForgeClient.init();
		}
	}
}
