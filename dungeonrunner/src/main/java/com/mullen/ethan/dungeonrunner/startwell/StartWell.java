package com.mullen.ethan.dungeonrunner.startwell;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureManager;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Vector2;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class StartWell implements Listener {

	private static int WELL_SIZE = 9;
	
	private Main main;
	private Semaphore sem;
	
	private World wellWorld;
	private Cube wellCube;
	private int x, z;
	private int topY;
		
	public StartWell(Main instance) {
		
		this.main = instance;
		this.wellWorld = Bukkit.getWorld("world");
		
		Bukkit.getPluginManager().registerEvents(this, instance);
		
		Vector2 wellLoc = getWellLocation();
		if(wellLoc != null) {
			this.x = (int) wellLoc.x;
			this.z = (int) wellLoc.y;
		}

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
									main.getQueueRoom().sendPlayerToRoom(p, true);									
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
			Material mat = wellWorld.getBlockAt(0, y, (int) (-StartWell.WELL_SIZE/2f)).getType();
			boolean isAir = mat == Material.AIR || mat == Material.VOID_AIR;
			if(!isAir) break;
		}
		if(p.getGameMode() == GameMode.ADVENTURE) p.setGameMode(GameMode.SURVIVAL);
		p.teleport(new Location(wellWorld, x-0.5f, y+1, z-StartWell.WELL_SIZE/2f - 0.5f));
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if(!event.getBlock().getWorld().getUID().equals(wellWorld.getUID())) return;
		if(wellCube.contains(event.getBlock().getLocation())) {
			event.setCancelled(true);
		}
	}
	
	public void generate() {

		Random rand = new Random(wellWorld.getSeed());
		int spawnRadius = 50;
		this.x = wellWorld.getSpawnLocation().getBlockX() - spawnRadius + rand.nextInt(spawnRadius*2);
		this.z = wellWorld.getSpawnLocation().getBlockZ() - spawnRadius + rand.nextInt(spawnRadius*2);
		main.getConfig().set("welllocation.x", x);
		main.getConfig().set("welllocation.z", z);
		main.saveConfig();
		
		int y;
		for(y = wellWorld.getMaxHeight(); y >= wellWorld.getMinHeight(); y--) {
			Material mat = wellWorld.getBlockAt(x, y, z).getType();
			boolean isAir = mat == Material.AIR || mat == Material.VOID_AIR;
			if(!isAir) break;
		}
		
		Location loc = new Location(wellWorld, x-Math.floor(WELL_SIZE/2), y+1, z-Math.floor(WELL_SIZE/2));
		File wellFile = main.getFileLoader().loadResourceFile("resources/well.nbt");
		StructureManager.generateStructure(main, wellFile, loc, StructureRotation.NONE, new Runnable() {
			@Override
			public void run() {
				calculateTopY();
			}
		});
		
		for( ; y >= wellWorld.getMinHeight(); y--) {
			for(int xOff = -2; xOff <= 2; xOff++) {
				for(int zOff = -2; zOff <= 2; zOff++) {
					new Location(wellWorld, x-xOff, y, z-zOff).getBlock().setType(Material.BEDROCK);
				}
			}
			for(int xOff = -1; xOff <= 1; xOff++) {
				for(int zOff = -1; zOff <= 1; zOff++) {
					new Location(wellWorld, x-xOff, y, z-zOff).getBlock().setType(Material.AIR);
				}
			}
		}
		
		Cube c = new Cube(new Vector3(x-1, wellWorld.getMinHeight(), z-1), new Vector3(x+1, wellWorld.getMinHeight(), z+1));
		c.setWorld(wellWorld);
		c.fill(Material.OBSIDIAN);
		
	}
	
	public void calculateTopY() {
		int y;
		for(y = wellWorld.getMinHeight(); y < wellWorld.getMaxHeight(); y++) {
			if(wellWorld.getBlockAt(x+2, y, z).getType() != Material.BEDROCK) break;
		}
		this.topY = y;
		this.wellCube = new Cube(new Vector3(x-1, wellWorld.getMinHeight(), z-1), new Vector3(x+1, topY, z+1));
	}
	
	public boolean isWellGenerated() {
		World w = Bukkit.getWorlds().get(0);
		return new Location(w, x, w.getMinHeight(), z).getBlock().getType() == Material.OBSIDIAN;
	}
	
	public Vector2 getWellLocation() {
		FileConfiguration config = main.getConfig();
		if(config == null) return null;
		if(!config.contains("welllocation.x") || !config.contains("welllocation.z")) return null;
		int x = config.getInt("welllocation.x");
		int z = config.getInt("welllocation.z");
		return new Vector2(x, z);
	}
	
	public void clearInsideWell() {
		Vector3 start = new Vector3(wellCube.getStartX(), wellCube.getStartY()+1, wellCube.getStartZ());
		Vector3 end = new Vector3(wellCube.getEndX(), wellCube.getEndY(), wellCube.getEndZ());
		Cube newCube = new Cube(start, end);
		newCube.setWorld(wellWorld);
		newCube.fill(Material.AIR);
	}
	
	public Cube getCube() {
		return wellCube;
	}
	
}
