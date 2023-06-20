package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.mullen.ethan.custommobs.CustomBoss;
import com.mullen.ethan.custommobs.CustomMob;
import com.mullen.ethan.custommobs.includedmobs.IncludedMobsRegister;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonDoor;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomClearEvent;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomPopulateEvent;
import com.mullen.ethan.dungeonrunner.hordes.Horde;
import com.mullen.ethan.dungeonrunner.utils.Cube;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

public class RoomManager {

	private Main main;
	private Dungeon dungeon;
	private RoomData room;
	
	private boolean roomPopulated;
	private List<Entity> roomsMobs;
	private List<DungeonDoor> roomsDoors;
	private boolean isCleared;
	
	private int taskID;
	
	public RoomManager(Main main, Dungeon dungeon, RoomData room) {
		this.main = main;
		this.dungeon = dungeon;
		this.room = room;
		this.roomPopulated = false;
		this.roomsDoors = new ArrayList<DungeonDoor>();
		this.isCleared = false;
		
		// Create DungeonDoors
		for(RoomData child : room.getChildrenMap().keySet()) {
			Vector3 childDoor = room.getChildrenMap().get(child);
			Location doorLoc = room.getLocation().clone().add(childDoor).getWorldLocation(main.getDungeonWorld());
			BlockFace exitDir = room.getDoorExitDirection(childDoor);
			boolean isWest = exitDir == BlockFace.NORTH || exitDir == BlockFace.SOUTH;
			boolean isBossDoor = child.getStructureData().getStructureType() == StructureType.BOSS_ROOM;
			DungeonDoor door = new DungeonDoor(main, this, doorLoc, isWest, isBossDoor);
			roomsDoors.add(door);
			if(isBossDoor) {
				dungeon.setBossDoor(door);
			}
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
					populate();
					break;
				}
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
				this.isCleared = true;
				DungeonRoomClearEvent event = new DungeonRoomClearEvent(this);
				Bukkit.getPluginManager().callEvent(event);
			}
		}
		
	}
	
	public void populate() {
		this.roomPopulated = true;
		this.roomsMobs = new ArrayList<Entity>();
						
		dungeon.addDiscoveredRoom(this);
		
		if(room.getStructureData().getStructureType() == StructureType.BOSS_ROOM) {
			spawnBoss();
		} else if(isMobRoom()) {
			spawnMobs();
			// Lock doors
			if(roomsMobs.size() > 0) {
				lockDoors();
				Location centerRoom = room.getCube().getCenter().getWorldLocation(main.getDungeonWorld());
				centerRoom.getWorld().playSound(centerRoom, Sound.BLOCK_CHEST_LOCKED, 1, 1.5f);
				dungeon.sendActionBarMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Room locked!");
			}
		}

		DungeonRoomPopulateEvent event = new DungeonRoomPopulateEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		
	}
	
	public void spawnMobs() {
		List<Horde> possibleHordes = new ArrayList<Horde>();
		StructureType structureType = room.getStructureData().getStructureType();
		for(String hordeName : dungeon.getDungeonTheme().getHordes(structureType)) {
			Horde horde = main.getHordeManager().getHorde(hordeName);
			if(horde == null) {
				Bukkit.getLogger().warning(ChatColor.RED + "Dungeon theme \"" + dungeon.getDungeonThemeName() + "\" calls for a horde named \"" + hordeName + "\" but it's not registered in the horde manager.");
				continue;
			}
			possibleHordes.add(horde);
		}
		
		if(possibleHordes.size() == 0) {
			Bukkit.getLogger().warning(ChatColor.RED + "Dungeon theme \"" + dungeon.getDungeonThemeName() + "\" has no hordes associated with it.");			
			return;
		}
		
		HashMap<String, Integer> countMap = possibleHordes.get(new Random().nextInt(possibleHordes.size())).generateRandomCountMap();
		for(String id : countMap.keySet()) {
			for(int i = 0; i < countMap.get(id); i++) {
				Location spawnLoc = room.getDesireableSpawnLocation(3).getWorldLocation(main.getDungeonWorld());
				 
				// Check if the id is an EntityType enum
				LivingEntity entity = null;
				try {
					EntityType type = EntityType.valueOf(id.toUpperCase());
					entity = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, type);
				} catch(Exception e) {}
				
				// Check if is registered
				if(main.getCustomBosses().isCustomMobRegistered(id)) {
					CustomMob mob = main.getCustomBosses().getInstance(id);
					entity = mob.spawn(spawnLoc);
				}
				
				if(entity == null) {
					Bukkit.getLogger().log(Level.SEVERE, "Failed to spawn mob with id \"" + id + "\"");
					continue;
				}
				
				addMob(entity);
				
			}
		}
		
	}
	
	public void spawnBoss() {
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
			Bukkit.getLogger().log(Level.SEVERE, "Tried to spawn a boss in the center of \"" + room.getStructureData().getFile().getName() + ",\" but no suitable center was found.");
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
	
	public void lockDoors() {
		for(DungeonDoor childDoor : roomsDoors) {
			childDoor.setLocked(true);
		}
	}
	
	public void unlockDoors() {
		for(DungeonDoor childDoor : roomsDoors) {
			if(childDoor == dungeon.getBossDoor()) continue;
			childDoor.setLocked(false);
		}
	}
	
	public boolean isMobRoom() {
		return room.getChestLocations().size() > 0;
	}
	
	public RoomData getRoomData() {
		return room;
	}
	
	public boolean hasMobs() {
		if(roomsMobs == null) return false;
		return roomsMobs.size() > 0;
	}
	
	public List<DungeonDoor> getRoomsDoors() {
		return roomsDoors;
	}
	
	public void addMob(LivingEntity entity) {
		roomsMobs.add(entity);
	}
	
	public boolean isCleared() {
		return isCleared;
	}
	
	public Vector3 getLocation() { return room.getLocation(); }
	public Vector3 getSize() { return room.getStructureData().getSize(); }
	
}
