package com.mullen.ethan.dungeonrunner.maps;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.bukkit.Material;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonDoor;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.PerlinNoise;

public class DungeonMapDrawer {

	private Main main;
	
	private BufferedImage solidBlockBuffer;
	private BufferedImage masterImage;
	private BufferedImage backgroundImage;
	
	public DungeonMapDrawer(Main main) {
		this.main = main;
	}
	
	public void reloadMasterImage() {
		
		// Background image should only be drawn once
		if(backgroundImage == null) {
			backgroundImage = PerlinNoise.generatePerlinNoiseImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS);
		} 
		
		// Create solid block buffer
		this.solidBlockBuffer = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToSolidBlockBuffer(room);
		}
		
		// Create master image
		this.masterImage = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		// Draw rooms
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToMasterImage(room);
		}
		
		// Stitch the foreground and background images together
//		BufferedImage finalImage = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
//		Graphics finalImageGraphics = finalImage.getGraphics();
//		finalImageGraphics.drawImage(backgroundImage, 0, 0, finalImage.getWidth(), finalImage.getHeight(), null);
//		finalImageGraphics.drawImage(masterImage, 0, 0, finalImage.getWidth(), finalImage.getHeight(), null);
//		this.masterImage = finalImage;
		
	}

	private void drawToSolidBlockBuffer(RoomManager room) {
		Cube cube = room.getRoomData().getCube();
//		boolean isInVertical = targY >= cube.getStartY() && targY <= cube.getEndY();
//		if(!isInVertical) return;
		
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
				int fx = worldX + DungeonGenerator.RADIUS;
				int fz = worldZ + DungeonGenerator.RADIUS;
				try {
					solidBlockBuffer.setRGB(fx, fz, Color.BLACK.getRGB());
				} catch(Exception e) {
					System.out.println("Tried to draw to solid block buffer at (" + fx + ", " + fz + ")");
				}
			}		
		}	
	}
	
	private void drawToMasterImage(RoomManager room) {
		
		Cube cube = room.getRoomData().getCube();
//		boolean isInVertical = targY >= cube.getStartY() && targY <= cube.getEndY();
//		if(!isInVertical) return;
		
		for(int worldX = cube.getStartX(); worldX <= cube.getEndX(); worldX++) {
			for(int worldZ = cube.getStartZ(); worldZ <= cube.getEndZ(); worldZ++) {
				// Convert to map coordinates
				int fx = worldX + DungeonGenerator.RADIUS;
				int fz = worldZ + DungeonGenerator.RADIUS;
				if(!solidBlockBufferHasBlock(fx, fz)) continue;							
				
				boolean isBorder = isBorder(fx, fz);
				Color c = isBorder ? getBorderColor(room) : getRoomColor(room);
				
				try {
					masterImage.setRGB(fx, fz, c.getRGB());
				} catch(Exception e) {
					System.out.println("Tried to draw to master image at (" + fx + ", " + fz + ")");
				}					
				
			}		
		}	
				
		for(DungeonDoor door : room.getRoomsDoors()) {
			Cube doorCube = door.getCube();
			for(int worldX = doorCube.getStartX(); worldX <= doorCube.getEndX(); worldX++) {
				for(int worldZ = doorCube.getStartZ(); worldZ <= doorCube.getEndZ(); worldZ++) {
					// Convert to map coordinates
					int fx = worldX + DungeonGenerator.RADIUS;
					int fz = worldZ + DungeonGenerator.RADIUS;
					if(!solidBlockBufferHasBlock(fx, fz)) continue;							
					
					Color c = getDoorColor(door);
					
					try {
						masterImage.setRGB(fx, fz, c.getRGB());
					} catch(Exception e) {
						System.out.println("Tried to draw to master image at (" + fx + ", " + fz + ")");
					}					
					
				}		
			}				
		}
		
	}

	public BufferedImage getMasterImage() {
		if(masterImage == null) {
			reloadMasterImage();
		}
		return masterImage;
	}
	
	/**
	 * Invalidate the master image to force a redraw of the master image. Should be called sparingly
	 */
	public void invalidateMasterImage() {
		this.solidBlockBuffer = null;
		this.masterImage = null;
	}

	private boolean isBorder(int imageX, int imageY) {
		for(int xOff : Arrays.asList(-1, 0, 1)) {
			for(int yOff : Arrays.asList(-1, 0, 1)) {
				if(!solidBlockBufferHasBlock(imageX + xOff, imageY + yOff)) {
					return true;
				}
			}
		}
		return false;
	}

	private Color getRoomColor(RoomManager room) {
		Color c = room.isMobRoom() ? new Color(53, 53, 53) : new Color(132, 132, 132);
		if(room.getRoomData().getStructureData().getStructureType() == StructureType.BOSS_ROOM) c = new Color(153, 51, 51);
		if(room.getRoomData().getStructureData().getStructureType() == StructureType.START_ROOM) c = new Color(65, 109, 132);
		return c;
	}

	private Color getBorderColor(RoomManager room) {
		Color c = getRoomColor(room);
		int shadeAmt = 30;
		c = new Color(Math.max(c.getRed() - shadeAmt, 0), Math.max(c.getGreen() - shadeAmt, 0), Math.max(c.getBlue() - shadeAmt, 0), 200);
		return c;
	}

	private Color getDoorColor(DungeonDoor door) {
		Color c = door.hasBeenOpened() ? new Color(220, 217, 211) : new Color(92, 219, 213);
		if(door.isLocked()) c = new Color(220, 0, 0);
		if(door.equals(main.getCurrentDungeon().getBossDoor())) c = new Color(229, 229, 51);
		return c;
	}
	
	private boolean solidBlockBufferHasBlock(int x, int y) {
		return solidBlockBuffer.getRGB(x, y) == new Color(0, 0, 0, 255).getRGB();
	}
	
}
