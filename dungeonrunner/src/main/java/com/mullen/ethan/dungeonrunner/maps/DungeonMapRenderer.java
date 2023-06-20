package com.mullen.ethan.dungeonrunner.maps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;

public class DungeonMapRenderer extends MapRenderer {

	private Main main;
	private boolean done;

	// These variables will be loaded for every load() call
	private BufferedImage image;
	private Graphics2D g;
	private int px, py, pz;
	private int cx, cz;
	private int mapSizeX, mapSizeZ;

	private MapCursor focusCursor;
	
	// TODO: Drawing steps
	// - Load all dungeon blocks into a air/solid block buffer
	// - Draw all blocks, using the previous buffer to make borders
	// - Crop into the players location
	
	private BufferedImage solidBlockBuffer;
	private BufferedImage masterImage;
	private int sizeX, sizeZ;
	private int minX, minZ;
	
	public DungeonMapRenderer(Main main) {
		this.main = main;
		this.done = false;
	}
	
	public void reloadMasterImage(int targY) {
		
		Cube bounds = main.getCurrentDungeon().getBounds();
		sizeX = bounds.getEndX() - bounds.getStartX();
		sizeZ = bounds.getEndZ() - bounds.getStartZ();
		minX = bounds.getStartX();
		minZ = bounds.getStartZ();
		
		this.solidBlockBuffer = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToSolidBlockBuffer(room, targY);
		}
		
		this.masterImage = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToMasterImage(room, targY);
		}
		
	}
	
	private void drawToSolidBlockBuffer(RoomManager room, int targY) {
		Cube cube = room.getRoomData().getCube();
		boolean isInVertical = targY >= cube.getStartY() && targY <= cube.getEndY();
		if(!isInVertical) return;
		
		for(int worldX = cube.getStartX(); worldX < cube.getEndX(); worldX++) {
			for(int worldZ = cube.getStartZ(); worldZ < cube.getEndZ(); worldZ++) {
				boolean hasSolidBlock = false;
				for(int y = cube.getEndY(); y >= cube.getStartY(); y--) {
					Material blockType = main.getDungeonWorld().getBlockAt(worldX, y, worldZ).getType();
					if(blockType != Material.AIR && blockType != Material.VOID_AIR) {
						hasSolidBlock = true;
						break;
					}
				}
				if(!hasSolidBlock) continue;					
				// Convert to map coordinates
				int fx = worldX + DungeonGenerator.RADIUS;
				int fz = worldZ + DungeonGenerator.RADIUS;
				try {
					solidBlockBuffer.setRGB(fx, fz, Color.BLACK.getRGB());
				} catch(Exception e) {
					System.out.println("Tried to draw to solid block buffer at (" + fx + ", " + fz + ")");
					System.out.println("worldx/z: " + worldX + ", " + worldZ + "; minX/Z: " + minX + ", " + minZ + "; sizeX/Z: " + sizeX + ", " + sizeZ);
				}
			}		
		}	
	}
	
	private void drawToMasterImage(RoomManager room, int targY) {
		Cube cube = room.getRoomData().getCube();
		boolean isInVertical = targY >= cube.getStartY() && targY <= cube.getEndY();
		if(!isInVertical) return;
		
		for(int worldX = cube.getStartX(); worldX < cube.getEndX(); worldX++) {
			for(int worldZ = cube.getStartZ(); worldZ < cube.getEndZ(); worldZ++) {
				boolean hasSolidBlock = false;
				for(int y = cube.getEndY(); y >= cube.getStartY(); y--) {
					Material blockType = main.getDungeonWorld().getBlockAt(worldX, y, worldZ).getType();
					if(blockType != Material.AIR && blockType != Material.VOID_AIR) {
						hasSolidBlock = true;
						break;
					}
				}
				if(!hasSolidBlock) continue;					
				// Convert to map coordinates
				int fx = worldX + DungeonGenerator.RADIUS;
				int fz = worldZ + DungeonGenerator.RADIUS;
				
				boolean isBorder = false;
				for(int xOff : Arrays.asList(-1, 0, 1)) {
					for(int zOff : Arrays.asList(-1, 0, 1)) {
						if(solidBlockBuffer.getRGB(fx + xOff, fz + zOff) == new Color(0, 0, 0, 0).getRGB()) {
							isBorder = true;
							break;							
						}
					}
				}
				
				Color c = Color.GRAY;
				
				if(isBorder) {
					if(room.isCleared()) {
						c = Color.GREEN;
					}
					if(room.hasMobs() && !room.isCleared()) {
						c = Color.RED;
					}
					int shadeAmt = 30;
					c = new Color(Math.max(c.getRed() - shadeAmt, 0), Math.max(c.getGreen() - shadeAmt, 0), Math.max(c.getBlue() - shadeAmt, 0));
				}
				
				try {
					masterImage.setRGB(fx, fz, c.getRGB());
				} catch(Exception e) {
					System.out.println("Tried to draw to solid block buffer at (" + fx + ", " + fz + ")");
					System.out.println("worldx/z: " + worldX + ", " + worldZ + "; minX/Z: " + minX + ", " + minZ + "; sizeX/Z: " + sizeX + ", " + sizeZ);
				}
			}		
		}	
	}
	
	public void load(Player focus) {
		if(main.getCurrentDungeon() == null) return;
		
		if(this.masterImage == null) {
			reloadMasterImage(this.py);
		}
		
		if(this.focusCursor == null) {
			this.focusCursor = new MapCursor(
	                (byte) 0,    // Unique ID for the cursor
	                (byte) 0,    // X position on the map (will be calculated later)
	                (byte) 0,    // Y position on the map (will be calculated later)
	                MapCursor.Type.RED_POINTER, // Cursor type
	                true       // True if the cursor is visible
	        );
		}
		
		// The size of the "window" into the full map
		int size = 64;
		
		// The players coordinates in image space
		int playerX = focus.getLocation().getBlockX() + DungeonGenerator.RADIUS;
		int playerZ = focus.getLocation().getBlockZ() + DungeonGenerator.RADIUS;
		
		// TODO: Switch to masterImage instead of the solidBlockBuffer
		this.image = safeCrop(masterImage, playerX-(size/2), playerZ-(size/2), size);
		// Expand to fill canvas
		BufferedImage mapImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		mapImage.getGraphics().drawImage(this.image, 0, 0, 128, 128, null);
		this.image = mapImage;
		
		Graphics g = this.image.getGraphics();
		g.setColor(Color.RED);
		
	}
	
	public void drawRoom(RoomManager room) {
		
		Cube cube = room.getRoomData().getCube();
		boolean isInVertical = py >= cube.getStartY() && py <= cube.getEndY();
		if(!isInVertical) return;

		Color c = Color.GRAY;
		if(room.getRoomData().getStructureData().getStructureType() == StructureType.BOSS_ROOM) {
			c = Color.RED;
		} else if(room.getRoomData().getStructureData().getStructureType() == StructureType.START_ROOM) {
			c = Color.GREEN;
		}
						
		for(int worldX = cube.getStartX(); worldX <= cube.getEndX(); worldX++) {
			for(int worldZ = cube.getStartZ(); worldZ <= cube.getEndZ(); worldZ++) {
				boolean hasSolidBlock = false;
				for(int y = cube.getEndY(); y >= cube.getStartY(); y--) {
					Material blockType = main.getDungeonWorld().getBlockAt(worldX, y, worldZ).getType();
					if(blockType != Material.AIR && blockType != Material.VOID_AIR) {
						hasSolidBlock = true;
						break;
					}
				}
				if(!hasSolidBlock) continue;
				
				// Convert to map coordinates
				int fx = cx - (px - worldX);
				int fz = cz - (pz - worldZ);
				if(fx >= 128 || fx < 0 || fz >= 128 || fz < 0) continue;
				
				try {
					image.setRGB(fx, fz, c.getRGB());
				} catch(Exception e) {
					System.out.println("Ran into exception when writing to pixels (" + fx + ", " + fz + ")");
				}
			}			
		}
		
	}
	
	private int renderCounter;
	
	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {		
		this.px = player.getLocation().getBlockX();
		this.py = player.getLocation().getBlockY();
		this.pz = player.getLocation().getBlockZ();
		
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
		done = true;
	}

	private RoomManager findPlayersRoom(Player p) {
		for(RoomManager rm : main.getCurrentDungeon().getRoomManagers()) {
			Cube roomCube = rm.getRoomData().getCube();
			if(roomCube.contains(p.getLocation())) {
				return rm;
			}
		}
		return null;
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
		this.solidBlockBuffer = null;
		this.masterImage = null;
	}
	
}
