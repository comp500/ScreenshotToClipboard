package link.infra.screenshotclipboard;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.forgespi.Environment;
import org.apache.commons.lang3.tuple.Pair;

@Mod(ScreenshotToClipboard.MOD_ID)
public class ScreenshotToClipboardForge {
	public ScreenshotToClipboardForge() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		if (Environment.get().getDist() == Dist.CLIENT) {
			ScreenshotToClipboardForgeClient.init();
		}
	}
}
