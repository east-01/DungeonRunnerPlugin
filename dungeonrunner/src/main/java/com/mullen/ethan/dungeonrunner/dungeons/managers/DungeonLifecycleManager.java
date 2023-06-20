package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.custommobs.events.CustomMobDeathEvent;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTableGenerator;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomClearEvent;
import com.mullen.ethan.dungeonrunner.utils.MusicBox;

import net.md_5.bungee.api.ChatColor;

public class DungeonLifecycleManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<RoomManager> discoveredRooms;
	private int roomsToComplete;
	private int roomsCompleted;
	
	public DungeonLifecycleManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		this.discoveredRooms = new ArrayList<RoomManager>();
		this.roomsCompleted = 0;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onRoomComplete(DungeonRoomClearEvent event) {
		
		this.roomsCompleted += 1;
		RoomData room = event.getClearedRoom().getRoomData();
		boolean isBossRoom = event.getClearedRoom().getRoomData().getStructureData().getStructureType() == StructureType.BOSS_ROOM;
		Location centerRoom = room.getCube().getCenter().getWorldLocation(main.getDungeonWorld());
		if(!isBossRoom) {
			MusicBox.SMALL_WIN.playSong(main, centerRoom, 1f);
			event.getClearedRoom().unlockDoors();

			// Means we've cleared all other rooms except boss door
			if(roomsCompleted+1 == roomsToComplete) {
				dungeon.sendActionBarMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "Room cleared! Boss door unlocked.");				
				dungeon.getBossDoor().setLocked(false);
			} else {
				dungeon.sendActionBarMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "Room cleared!");								
			}
		} else {
			dungeon.sendTitleMessage(ChatColor.GOLD + "Dungeon complete", "", 20, 20*5, 20);
			MusicBox.LARGE_WIN.playSong(main, centerRoom, 1f);
		}

	}
	
	@EventHandler
	public void onBossDeath(CustomMobDeathEvent event) {
		if(!event.isBoss()) return;
		Location deathLoc = event.getEntity().getLocation();
		// Drop loot item 
		// TODO: Make these more interesting
		Item item = deathLoc.getWorld().dropItem(deathLoc.add(0, 1, 0), LootTableGenerator.getRandomMaxEnchantBook());
		item.setPickupDelay(3*20);
		new BukkitRunnable() {
			public void run() {
				if(item.isDead()) {
					cancel();
					return;
				}
				Location itemLoc = item.getLocation();
				itemLoc.getWorld().spawnParticle(Particle.CRIT_MAGIC, itemLoc, 5);
				if(itemLoc.getBlock().getType() == Material.AIR) itemLoc.getBlock().setType(Material.LIGHT);
			}
		}.runTaskTimer(main, 0L, 2L);
	}

	public float getProgress() {
		return (float)roomsCompleted/(float)roomsToComplete;
	}
	
	public void updateRoomsToComplete() {
		this.roomsToComplete = 1; // Start with 1 to include the boss room
		for(RoomManager rm : dungeon.getRoomManagers()) {
			if(rm.isMobRoom()) roomsToComplete += 1;
		}
	}
	
	public List<RoomManager> getDiscoveredRooms() {
		return discoveredRooms;
	}
	
	public void addDiscoveredRoom(RoomManager rm) {
		discoveredRooms.add(rm);
	}
	
}
