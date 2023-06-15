package com.mullen.ethan.dungeonrunner.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

/**
 * A dungeon door class
 */
public class DungeonDoor implements Listener {

	public static Material UNLOCKED_MATERIAL = Material.IRON_BLOCK;
	public static Material LOCKED_MATERIAL = Material.REDSTONE_BLOCK;
	
	/* Door animation time in seconds */
	public static float DOOR_ANIMATION_TIME = 0.4f;
	public static float DOOR_OPEN_TIME = 3f;
	public static int DOOR_HEIGHT = 3;
	
	private Main main;
	private Dungeon dungeon;
	// This location is at the bottom center of the door
	private Location doorLocation;
	private boolean isWest;
	private Cube cube;
	private int timer;
	private boolean locked;
	
	public DungeonDoor(Main main, Location doorLocation, boolean isWest) {
		this.main = main;
		this.dungeon = main.getCurrentDungeon();
		this.doorLocation = doorLocation;
		this.isWest = isWest;
		this.cube = getCube();
		close(false);
		
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public void open(boolean animated) { 
		if(!locked) {
			setOpen(animated, true); 
		} else {
			doorLocation.getWorld().playSound(doorLocation, Sound.BLOCK_CHEST_LOCKED, 0.8f, 0.45f);
		}
	}
	public void close(boolean animated) { setOpen(animated, false); }
	public void setOpen(boolean animated, boolean open) {
		if(animated) doorLocation.getWorld().playSound(doorLocation, open ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.8f, 0.45f);
		// Animation frame, should be the height of the door
		Material mat = open ? Material.AIR : dungeon.getDungeonTheme().getDoorMaterial();
		for(int yOffset = 0; yOffset < DOOR_HEIGHT; yOffset++) {
			// If the door is closing, this means we need to reverse the direction of the animation
			int y = open ? yOffset : (DOOR_HEIGHT-1) - yOffset;
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run() {
					for(int hOffset = -1; hOffset <= 1; hOffset++) {
						// Enable/disable the x/z axes based off of door direction
						int x = isWest ? hOffset : 0;
						int z = isWest ? 0 : hOffset;
						Location loc = new Location(main.getDungeonWorld(), doorLocation.getBlockX() + x, doorLocation.getBlockY() + y, doorLocation.getBlockZ() + z);
						loc.getBlock().setType(loc.equals(doorLocation) && !open ? UNLOCKED_MATERIAL : mat);
					}
				}
			}, (long) (20*yOffset*(DOOR_ANIMATION_TIME/3f)));
		}
	}
	
	public Cube getCube() {
		Vector3 min = new Vector3(isWest ? -1 : 0, 0, isWest ? 0 : -1);
		Vector3 max = new Vector3(isWest ? 1 : 0, DOOR_HEIGHT, isWest ? 0 : 1);
		Vector3 doorLoc = new Vector3(doorLocation.getBlockX(), doorLocation.getBlockY(), doorLocation.getBlockZ());
		Cube c = new Cube(doorLoc.clone().add(min), doorLoc.clone().add(max));
		c.setWorld(doorLocation.getWorld());
		return c;
	}
	
	@EventHandler
	public void onDoorInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null) return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block b = event.getClickedBlock();
		if(!b.getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME)) return;
		if(!cube.contains(b.getLocation())) return;
		
		open(true);
		timer = (int) (DOOR_OPEN_TIME*4); // *4 because, in timer method, 5/20=4. 4 ticks per second
		timer();
		
	}
	
	public void timer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				
				if(timer > 0) {
					timer--;
				} else if(timer <= 0) {
					cancel();
					close(true);
				}
				
				for(Player p : dungeon.getPlayersInDungeon()) {
					Location loc = p.getLocation();
					if(cube.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
						timer = (int) (DOOR_OPEN_TIME*4); // *4 because, in timer method, 5/20=4. 4 ticks per second
						break;
					}
				}
			}
		}.runTaskTimer(main, 0l, 5l);
	}
	
	public void setLocked(boolean locked) {
		doorLocation.getBlock().setType(locked ? LOCKED_MATERIAL : UNLOCKED_MATERIAL);
		this.locked = locked;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
}
