package com.locydragon.locyguarder.util;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class PictureRender extends MapRenderer {
	private BufferedImage picture = null;

	public PictureRender(File input) {
		try {
			InputStream stream = new FileInputStream(input);
			this.picture = MapPalette.resizeImage(ImageIO.read(stream));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
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
	}
}