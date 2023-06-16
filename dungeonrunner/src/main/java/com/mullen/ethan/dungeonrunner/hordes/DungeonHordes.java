package com.mullen.ethan.dungeonrunner.hordes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.mullen.ethan.custommobs.CustomMob;
import com.mullen.ethan.custommobs.includedmobs.IncludedMobsRegister;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.managers.RoomManager;

public class DungeonHordes {

	// The basic hordes used for smaller rooms
	private static Horde zombies;
	private static Horde skeletons;
	
	// Advanced hordes for large rooms
	private static Horde zombie_unit;
	private static Horde skeleton_unit;
		
	public static void populate() {
		zombies = new Horde();
		zombies.addMob(EntityType.ZOMBIE.toString(), 1, 3, 5);

		skeletons = new Horde();
		skeletons.addMob(EntityType.ZOMBIE.toString(), 1, 3, 5);

		zombie_unit = new Horde();
		zombie_unit.addMob(IncludedMobsRegister.CM_ELITE_ZOMBIE, 10, 1, 2);
		zombie_unit.addMob(EntityType.ZOMBIE.toString(), 90, 5, 7);

		skeleton_unit = new Horde();
		skeleton_unit.addMob(IncludedMobsRegister.CM_ELITE_SKELETON, 10, 1, 2);
		skeleton_unit.addMob(EntityType.SKELETON.toString(), 90, 3, 5);		
	}
	
	public static void populateRoom(Main main, RoomManager room) {
		if(zombies == null) {
			populate();
		}
		
		RoomData data = room.getRoomData();
		HashMap<String, Integer> countMap = getMobCounts(data.getStructureData().getStructureType());
		for(String id : countMap.keySet()) {
			for(int i = 0; i < countMap.get(id); i++) {
				Location spawnLoc = room.getRoomData().getDesireableSpawnLocation(3).getWorldLocation(main.getDungeonWorld());
				 
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
				
				room.addMob(entity);
				
			}
		}
		
	}
	
	public static HashMap<String, Integer> getMobCounts(StructureType type) {
		List<Horde> possibleHordes = getHordesForStructureType(type);
		return possibleHordes.get(new Random().nextInt(possibleHordes.size())).generateRandomCountMap();
	}

	public static List<Horde> getHordesForStructureType(StructureType type) {
		if(type == StructureType.SMALL_ROOM) {
			return Arrays.asList(zombies, skeletons);
		} else if(type == StructureType.LARGE_ROOM) {
			return Arrays.asList(zombie_unit, skeleton_unit);
		} else {
			return new ArrayList<Horde>();
		}
	}
	
}
