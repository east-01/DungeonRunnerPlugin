package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.customforge.events.CustomMobDeathEvent;
import com.mullen.ethan.customforge.events.CustomMobSpawnEvent;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.DungeonSize;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.utils.Vector3;

import net.md_5.bungee.api.ChatColor;

public class DungeonMobManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<Entity> dungeonMobs;
	private boolean bossSpawned;
	private boolean bossDefeated;
	private Entity boss;
	
	public DungeonMobManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		
		this.dungeonMobs = new ArrayList<Entity>();
		this.bossSpawned = false;
		this.bossDefeated = false;
		this.boss = null;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, main);
	}

	public void addMob(Entity e) {
		dungeonMobs.add(e);
	}
	
	public void clearMobs() {
		for(Entity e : dungeonMobs) {
			e.remove();
		}
	}
	
	public List<Entity> getMobs() {
		return dungeonMobs;
	}
	
	public boolean isBossSpawned() {
		return bossSpawned;
	}

	public boolean isBossDefeated() {
		return bossDefeated;
	}

	public Entity getBoss() {
		return boss;
	}
	
	@EventHandler
	public void onCustomBossMobSpawn(CustomMobSpawnEvent event) {
		if(!event.getSpawnLocation().getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME)) return;
		if(event.isBoss()) {
			this.bossSpawned = true;
			this.boss = event.getEntity();
			dungeon.updateScoreboardValues();
		}
		addMob(event.getEntity());
	}
		
	@EventHandler
	public void onBossDeath(CustomMobDeathEvent event) {
		if(!event.getMobData().isBoss()) return;
		
		RoomManager bossRoom = null;
		for(RoomManager rm : dungeon.getRoomManagers()) {
			if(rm.getType() != StructureType.BOSS_ROOM) continue;
			bossRoom = rm;
		}
		
		if(bossRoom == null) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to find boss room on boss death event.");
			return;
		}

		this.bossDefeated = true;
		dungeon.updateScoreboardValues();

		Vector3 center = bossRoom.getRoomData().centerRoomFloor(1);
		Location chestLoc = center.getWorldLocation(main.getDungeonWorld());
		Block chestBlock = chestLoc.getBlock();
		chestBlock.setType(Material.CHEST);
		Chest chest = (Chest) chestBlock.getState();
		int tier = 0;
		if(dungeon.getSize() == DungeonSize.MEDIUM) {
			tier = 1;
		} else if(dungeon.getSize() == DungeonSize.LARGE) {
			tier = 2;
		}
		main.getLootTableManager().populateBossChest(chest, tier);
		
		Directional chestDirection = (Directional) chestBlock.getBlockData();
		BlockFace dir = bossRoom.getRoomData().getCube().getFace(new Vector3(dungeon.getBossDoor().getLocation()));
		chestDirection.setFacing(dir);
		chestBlock.setBlockData(chestDirection);
		
		new BukkitRunnable() {
			public void run() {
				if(chestBlock.getType() != Material.CHEST) {
					cancel();
					return;
				}
				chestLoc.getWorld().spawnParticle(Particle.CRIT_MAGIC, chestLoc.clone().add(0.5, 0.5, 0.5), 5);
			}
		}.runTaskTimer(main, 0L, 2L);
		
		chestLoc.clone().add(0, 1, 0).getBlock().setType(Material.LIGHT);
		
		List<Vector3> teleporterLocations = bossRoom.getRoomData().getTeleporterLocations();
		if(!teleporterLocations.isEmpty()) {
			Vector3 teleporterOffset = teleporterLocations.get(new Random().nextInt(teleporterLocations.size()));
			Location teleporterLoc = bossRoom.getLocation().add(teleporterOffset).getWorldLocation(main.getDungeonWorld()).add(0.5f, 0, 0.5f);
			dungeon.addTeleporter(main.getTeleporterManager().spawnTeleporter("BOSS_ROOM_TO_START_TELEPORTER", teleporterLoc, ChatColor.GRAY + "Back to start", dungeon.getStartLocation()));
		} else {
			Bukkit.getLogger().warning("Boss room structure " + bossRoom.getRoomData().getStructureData().getFile().getName() + " doesn't have any teleporters. It needs teleporters.");
		}
			
	}
			
}
