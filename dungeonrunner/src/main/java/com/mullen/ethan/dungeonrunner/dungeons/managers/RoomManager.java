package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.mullen.ethan.custommobs.CustomBoss;
import com.mullen.ethan.custommobs.includedmobs.IncludedMobsRegister;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonDoor;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomClearEvent;
import com.mullen.ethan.dungeonrunner.hordes.DungeonHordes;
import com.mullen.ethan.dungeonrunner.utils.Cube;
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
				DungeonRoomClearEvent event = new DungeonRoomClearEvent(this);
				Bukkit.getPluginManager().callEvent(event);
			}
		}
		
	}
	
	public void populate() {
		this.roomPopulated = true;
		this.roomsMobs = new ArrayList<Entity>();
		if(room.getChestLocations().size() <= 0) return;	
		
		DungeonHordes.populateRoom(main, this);
		
		// Lock doors
		for(DungeonDoor childDoor : roomsDoors) {
			childDoor.setLocked(true);
		}
	}
	
	public void spawnBoss() {
		this.roomPopulated = true;
		this.roomsMobs = new ArrayList<Entity>();
		List<String> bosses = Arrays.asList(IncludedMobsRegister.CM_EXALTED_BLAZE, 
				IncludedMobsRegister.CM_EXALTED_WITHER_SKELETON, 
				IncludedMobsRegister.CM_EXALTED_ZOMBIE, 
				IncludedMobsRegister.CM_EXALTED_VINDICATOR, 
				IncludedMobsRegister.CM_EXALTED_MAGMA_CUBE);
		
		String id = bosses.get(new Random().nextInt(bosses.size()));		
		CustomBoss cb = (CustomBoss) main.getCustomBosses().getInstance(id);
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
		
		LivingEntity boss = cb.spawn(spawnWorldLoc);
		addMob(boss);
		
	}
		
	public RoomData getRoomData() {
		return room;
	}
	
	public List<DungeonDoor> getRoomsDoors() {
		return roomsDoors;
	}
	
	public void addMob(LivingEntity entity) {
		roomsMobs.add(entity);
	}
	
}
