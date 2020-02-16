package com.locydragon.locyguarder.util;

import com.locydragon.locyguarder.async.AsyncPacketSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import java.awt.image.BufferedImage;

public class PictureRender extends MapRenderer {
	private BufferedImage picture = null;

	public PictureRender(BufferedImage input, AsyncPacketSender packet) {
		this.picture = MapPalette.resizeImage(input);
	}

	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
		byte[] imageData = MapPalette.imageToBytes(this.picture);
		for (int i = 0;i < mapCanvas.getCursors().size();i++) {
			mapCanvas.getCursors().removeCursor(mapCanvas.getCursors().getCursor(i));
		}
		for (int x = 0;x < this.picture.getWidth();x++) {
			for (int y = 0;y < this.picture.getHeight();y++) {
				mapCanvas.setPixel(x, y, imageData[(y * this.picture.getWidth() + x)]);
			}
		}
		this.picture.flush();
	}
}