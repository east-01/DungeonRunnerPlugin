package com.mullen.ethan.dungeonrunner.dungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.mullen.ethan.custombosses.bosstypes.BossType;
import com.mullen.ethan.custombosses.bosstypes.CustomBoss;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.MusicBox;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class RoomManager {

	private Main main;
	private Dungeon dungeon;
	private RoomData room;
	
	private boolean roomPopulated;
	private List<Entity> roomsMobs;
	private List<DungeonDoor> roomsDoors;
	
	private int taskID;
	
	public RoomManager(Main main, Dungeon dungeon, RoomData room) {
		this.main = main;
		this.dungeon = dungeon;
		this.room = room;
		this.roomPopulated = false;
		this.roomsDoors = new ArrayList<DungeonDoor>();
		
		// Create DungeonDoors
		for(Vector3 childDoor : room.getChildrenMap().values()) {
			Location doorLoc = room.getLocation().clone().add(childDoor).getWorldLocation(main.getDungeonWorld());
			BlockFace exitDir = room.getDoorExitDirection(childDoor);
			boolean isWest = exitDir == BlockFace.NORTH || exitDir == BlockFace.SOUTH;
			DungeonDoor door = new DungeonDoor(main, doorLoc, isWest);
			roomsDoors.add(door);
		}
		
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, 0L, 5L);
		
	}
	
	public void tick() {
		// Populate the room
		if(!roomPopulated) {
			Cube cube = room.getCube();
			for(Player p : dungeon.getPlayersInDungeon()) {
				Location loc = p.getLocation();
				if(cube.contains((float)loc.getX(), (float)loc.getY(), (float)loc.getZ())) {
					if(room.getStructureData().getName().toLowerCase().contains("boss")) {
						spawnBoss();
					} else {
						populate();
					}
				}
				break;
			}		
		}
		
		// Check if mobs are still alive
		if(roomPopulated && roomsMobs != null && roomsMobs.size() > 0) {
			List<Entity> toRemove = new ArrayList<Entity>();
			for(Entity e : roomsMobs) {
				if(e.isDead()) toRemove.add(e);
			}
			roomsMobs.removeAll(toRemove);
			// Room cleared, do something
			if(roomsMobs.size() == 0) {
				MusicBox.SMALL_WIN.playSong(main, room.getCube().getCenter().getWorldLocation(main.getDungeonWorld()), 1f);
				for(DungeonDoor childDoor : roomsDoors) {
					childDoor.setLocked(false);
				}
			}
		}
		
	}
	
	public void populate() {
		Random rand = new Random();
		this.roomPopulated = true;
		this.roomsMobs = new ArrayList<Entity>();
		if(room.getChestLocations().size() > 0) {			
			// Spawn mobs
			int spawnCount = 3+rand.nextInt(5);
			for(int i = 0; i < spawnCount && spawnCount < 100; i++) {
				Vector3 spawnLoc = room.getDesireableSpawnLocation(2); // 2 is mob height
				// Means we couldn't find a locatio, add 1 to try again
				if(spawnLoc == null) {
					spawnCount++;
					continue;
				}
				float spawnX = spawnLoc.x;
				float spawnY = spawnLoc.y;
				float spawnZ = spawnLoc.z;
				Entity e = main.getDungeonWorld().spawnEntity(new Vector3(spawnX + 0.5f, spawnY, spawnZ + 0.5f).getWorldLocation(main.getDungeonWorld()), EntityType.ZOMBIE);
				roomsMobs.add(e);
			}
			// Lock doors
			for(DungeonDoor childDoor : roomsDoors) {
				childDoor.setLocked(true);
			}
		}
	}
	
	public void spawnBoss() {
		this.roomPopulated = true;
		BossType bossType = BossType.values()[new Random().nextInt(BossType.values().length)];
		CustomBoss cb = bossType.getBossClass(main.getCustomBosses());
		Cube roomCube = room.getCube();
		Vector3 spawnLoc = room.getDesireableSpawnLocation(3,  roomCube.getCenter().x, roomCube.getCenter().z);
		// Means we couldn't find a locatio, add 1 to try again
		if(spawnLoc == null) {
			Bukkit.getLogger().log(Level.SEVERE, "Tried to spawn a boss in the center of \"" + room.getStructureData().getName() + ",\" but no suitable center was found.");
			return;
		}
		Location spawnWorldLoc = spawnLoc.getWorldLocation(main.getDungeonWorld()).add(0, 1, 0);
		for(Player p : dungeon.getPlayersInDungeon()) {
			p.sendMessage(ChatColor.AQUA + "The boss has spawned.");
			p.playSound(spawnWorldLoc, Sound.ENTITY_WITHER_SPAWN, (float)0.8, (float)0.35);
		}
		
		cb.spawn(spawnWorldLoc);
	}
	
	public List<DungeonDoor> getRoomsDoors() {
		return roomsDoors;
	}
	
}
