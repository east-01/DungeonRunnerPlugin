package com.mullen.ethan.dungeonrunner.maps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.bukkit.Material;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.DungeonGenerator;
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
	
	public void reloadMasterImage(int targY) {
		
		// Background image should only be drawn once
		if(backgroundImage == null) {
			backgroundImage = PerlinNoise.generatePerlinNoiseImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS);
		} 
		
		// Create solid block buffer
		this.solidBlockBuffer = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToSolidBlockBuffer(room, targY);
		}
		
		// Create master image
		this.masterImage = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		// Draw rooms
		for(RoomManager room : main.getCurrentDungeon().getDiscoveredRooms()) {
			drawToMasterImage(room, targY);
		}
	
		// Stitch the foreground and background images together
		BufferedImage finalImage = new BufferedImage(2*DungeonGenerator.RADIUS, 2*DungeonGenerator.RADIUS, BufferedImage.TYPE_INT_ARGB);
		Graphics finalImageGraphics = finalImage.getGraphics();
		finalImageGraphics.drawImage(backgroundImage, 0, 0, finalImage.getWidth(), finalImage.getHeight(), null);
		finalImageGraphics.drawImage(masterImage, 0, 0, finalImage.getWidth(), finalImage.getHeight(), null);
		this.masterImage = finalImage;
		
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
											
				boolean isBorder = isBorder(fx, fz);
				Color c = isBorder ? getBorderColor(room) : getRoomColor(room);
				
				try {
					if(!isBorder) {
						masterImage.setRGB(fx, fz, c.getRGB());
					} else {
						drawGlow(fx, fz, c, 2, !shouldBorderGlowOutsideBuffer(room));
					}
				} catch(Exception e) {
					System.out.println("Tried to draw to solid block buffer at (" + fx + ", " + fz + ")");
				}
			}		
		}	
	}

	public BufferedImage getMasterImage(int targY) {
		if(masterImage == null) {
			reloadMasterImage(targY);
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
				if(solidBlockBufferHasBlock(imageX + xOff, imageY + yOff)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Color getRoomColor(RoomManager room) {
		Color c = room.isMobRoom() ? new Color(100, 100, 100) : new Color(150, 150, 150);
		return c;
	}
	
	private Color getBorderColor(RoomManager room) {
		Color c = getRoomColor(room);
		if(room.isCleared()) {
			c = new Color(30, 235, 50);
		} else if(room.hasMobs()) {
			c = new Color(235, 50, 30);
		}
		int shadeAmt = 30;
		c = new Color(Math.max(c.getRed() - shadeAmt, 0), Math.max(c.getGreen() - shadeAmt, 0), Math.max(c.getBlue() - shadeAmt, 0));
		return c;
	}

	private boolean shouldBorderGlowOutsideBuffer(RoomManager room) {
		return room.isMobRoom() && room.hasMobs();
	}
	
	private void drawGlow(int centerX, int centerY, Color color, int radius, boolean useSolidBufferMask) {
		for(int xOff = -radius; xOff < radius; xOff++) {
			for(int yOff = -radius; yOff < radius; yOff++) {
				int fx = centerX + xOff;
				int fy = centerY + yOff;
				if(useSolidBufferMask && !solidBlockBufferHasBlock(fx, fy)) continue;
				// Additive coloring
				Color targColor = new Color(masterImage.getRGB(fx, fy));
				Color finalColor = addColors(targColor, color);
				masterImage.setRGB(fx, fy, finalColor.getRGB());
			}			
		}
	}
	
	private boolean solidBlockBufferHasBlock(int x, int y) {
		return solidBlockBuffer.getRGB(x, y) == new Color(0, 0, 0, 0).getRGB();
	}
	
	private Color addColors(Color a, Color b) {
		return new Color(
			Math.min(255, a.getRed() + b.getRed()),
			Math.min(255, a.getGreen() + b.getGreen()),
			Math.min(255, a.getBlue() + b.getBlue()),
			Math.min(255, a.getAlpha() + b.getAlpha())
		);
	}
	
}
