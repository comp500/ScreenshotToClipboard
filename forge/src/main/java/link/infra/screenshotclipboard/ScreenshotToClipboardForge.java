package link.infra.screenshotclipboard;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(ScreenshotToClipboard.MOD_ID)
public class ScreenshotToClipboardForge {
	public ScreenshotToClipboardForge() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			MinecraftForge.EVENT_BUS.register(this);
			ScreenshotToClipboard.init();
		});
	}

	@SubscribeEvent
	public void handleScreenshot(ScreenshotEvent event) {
		ScreenshotToClipboard.handleScreenshotAWT(event.getImage());
	}
}
