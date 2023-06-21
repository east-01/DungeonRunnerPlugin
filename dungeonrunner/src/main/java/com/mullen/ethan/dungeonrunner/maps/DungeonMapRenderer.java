package com.mullen.ethan.dungeonrunner.maps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;

public class DungeonMapRenderer extends MapRenderer {

	private Main main;
	private DungeonMapDrawer drawer;
	
	// These variables will be loaded for every load() call
	private BufferedImage image;
	private int py;

	private MapCursor focusCursor;
		
	public DungeonMapRenderer(Main main) {
		this.main = main;
		this.drawer = new DungeonMapDrawer(main);
	}
			
	public void load(Player focus) {
		if(main.getCurrentDungeon() == null) return;
				
		if(this.focusCursor == null) {
			this.focusCursor = new MapCursor(
	                (byte) 0,    // Unique ID for the cursor
	                (byte) 0,    // X position on the map (will be calculated later)
	                (byte) 0,    // Y position on the map (will be calculated later)
	                MapCursor.Type.RED_POINTER, // Cursor type
	                true       // True if the cursor is visible
	        );
		}
		
		// Gets the master image. The drawer will load the image if necessary
		BufferedImage masterImage = drawer.getMasterImage(py);
		
		// The size of the "window" into the full map
		int size = 64;
		
		// The players coordinates in image space
		int playerX = focus.getLocation().getBlockX() + DungeonGenerator.RADIUS;
		int playerZ = focus.getLocation().getBlockZ() + DungeonGenerator.RADIUS;
		
		this.image = safeCrop(masterImage, playerX-(size/2), playerZ-(size/2), size);

		// Expand to fill canvas
		BufferedImage mapImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		mapImage.getGraphics().drawImage(this.image, 0, 0, 128, 128, null);
		this.image = mapImage;
		
		Graphics g = this.image.getGraphics();
		g.setColor(Color.RED);
		
	}
		
	private int renderCounter;
	
	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {		
		this.py = player.getLocation().getBlockY();
		
		load(player);

		MapCursorCollection cursors = canvas.getCursors();
		if(focusCursor != null && cursors.size() == 0) {
			cursors.addCursor(focusCursor);
		}

        focusCursor.setX((byte) map.getCenterX());
        focusCursor.setY((byte) map.getCenterZ());
        double cursorYaw = (player.getLocation().getYaw() - 90.0) % 360.0;
        byte direction = (byte) Math.floor(cursorYaw / 22.5);
        if (direction < 0) {
            direction += 16;
        }		
        focusCursor.setDirection(direction);
        
		canvas.drawImage(0, 0, image);
	}

	private static BufferedImage rotate(BufferedImage bimg, double angle) {

	    int w = bimg.getWidth();    
	    int h = bimg.getHeight();

	    BufferedImage rotated = new BufferedImage(w, h, bimg.getType());  
	    Graphics2D graphic = rotated.createGraphics();
	    graphic.rotate(Math.toRadians(angle), w/2, h/2);
	    graphic.drawImage(bimg, null, 0, 0);
	    graphic.dispose();
	    return rotated;
	}
	
	private BufferedImage safeCrop(BufferedImage input, int startX, int startY, int size) {
		int endX = startX + size;
		int endY = startY + size;
		if(startX < 0) startX = 0;
		if(startY < 0) startY = 0;
		if(endX >= input.getWidth()) startX = input.getWidth() - size;
		if(endY >= input.getHeight()) startY = input.getHeight() - size;
		if(input.getWidth() < size) {
			startX = 0;
			endX = startX+size;
		}
		if(input.getHeight() < size) {
			startY = 0;
			endY = startY+size;
		}
		return input.getSubimage(startX, startY, endX-startX, endY-startY);
	}

	/**
	 * Invalidate the master image to force a redraw of the master image. Should be called sparingly
	 */
	public void invalidateMasterImage() {
		drawer.invalidateMasterImage();
	}
	
}
