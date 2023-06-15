package com.mullen.ethan.dungeonrunner.startwell;

import java.util.EventListener;
import java.util.concurrent.Semaphore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.structures.StructureManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class StartWell implements EventListener {

	private static int ROOM_SIZE = 9;
	
	private Main main;
	private Semaphore sem;
	
	private World wellWorld;
	private Cube wellCube;
	private int topY;
	
	public StartWell(Main instance) {
		this.main = instance;
		this.wellWorld = Bukkit.getWorld("world");
		
		if(!isWellGenerated()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Generating start well...");
			generate();
		}
		
		calculateTopY();
				
		// Check for players in the well
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
			@Override
			public void run() {
				if(wellCube != null) {
					for(Player p : wellWorld.getPlayers()) {
						// The resistance effect is a tag for people who are going to the queueroom
						if(p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) continue;
						Location pLoc = p.getLocation();
						if(wellCube.contains((float)pLoc.getBlockX(), (float)pLoc.getBlockY(), (float)pLoc.getBlockZ())) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 255));
							Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
								@Override
								public void run() {
									main.getQueueRoom().sendPlayerToRoom(p);									
								}
							}, 15L);
						}
					}
				}
			}
		}, 0L, 10L);
		
	}

	public void sendPlayerToWell(Player p) {
		int y;
		for(y = wellWorld.getMaxHeight(); y >= wellWorld.getMinHeight(); y--) {
			Material mat = wellWorld.getBlockAt(0, y, (int) (-StartWell.ROOM_SIZE/2f)).getType();
			boolean isAir = mat == Material.AIR || mat == Material.VOID_AIR;
			if(!isAir) break;
		}
		p.teleport(new Location(wellWorld, -0.5f, y+1, -StartWell.ROOM_SIZE/2f - 0.5f));
	}
	
	public void generate() {

		int y;
		for(y = wellWorld.getMaxHeight(); y >= wellWorld.getMinHeight(); y--) {
			Material mat = wellWorld.getBlockAt(0, y, 0).getType();
			boolean isAir = mat == Material.AIR || mat == Material.VOID_AIR;
			if(!isAir) break;
		}
		
		Location loc = new Location(wellWorld, -Math.floor(ROOM_SIZE/2), y+1, -Math.floor(ROOM_SIZE/2));
		StructureManager.generateStructure(main, "well", loc, StructureRotation.NONE, new Runnable() {
			@Override
			public void run() {
				calculateTopY();
			}
		});
		
		for( ; y >= wellWorld.getMinHeight(); y--) {
			for(int x = -2; x <= 2; x++) {
				for(int z = -2; z <= 2; z++) {
					new Location(wellWorld, x, y, z).getBlock().setType(Material.BEDROCK);
				}
			}
			for(int x = -1; x <= 1; x++) {
				for(int z = -1; z <= 1; z++) {
					new Location(wellWorld, x, y, z).getBlock().setType(Material.AIR);
				}
			}
		}
		
		Cube c = new Cube(new Vector3(-1, wellWorld.getMinHeight(), -1), new Vector3(1, wellWorld.getMinHeight(), 1));
		c.setWorld(wellWorld);
		c.fill(Material.OBSIDIAN);
		
	}
	
	public void calculateTopY() {
		World w = Bukkit.getWorld("world");
		int y;
		for(y = w.getMinHeight(); y < w.getMaxHeight(); y++) {
			if(w.getBlockAt(2, y, 0).getType() != Material.BEDROCK) break;
		}
		this.topY = y;
		this.wellCube = new Cube(new Vector3(-1, w.getMinHeight(), -1), new Vector3(1, topY, 1));
	}
	
	public boolean isWellGenerated() {
		World w = Bukkit.getWorlds().get(0);
		return new Location(w, 0, w.getMinHeight(), 0).getBlock().getType() == Material.OBSIDIAN;
	}
	
}
