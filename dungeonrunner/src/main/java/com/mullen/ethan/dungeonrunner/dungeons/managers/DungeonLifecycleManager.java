package com.mullen.ethan.dungeonrunner.dungeons.managers;

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
import com.mullen.ethan.dungeonrunner.dungeons.DungeonDoor;
import com.mullen.ethan.dungeonrunner.dungeons.generator.RoomData;
import com.mullen.ethan.dungeonrunner.dungeons.generator.structures.StructureType;
import com.mullen.ethan.dungeonrunner.dungeons.loot.LootTableGenerator;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomClearEvent;
import com.mullen.ethan.dungeonrunner.utils.MusicBox;

public class DungeonLifecycleManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	public DungeonLifecycleManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onRoomComplete(DungeonRoomClearEvent event) {
		
		RoomData room = event.getClearedRoom().getRoomData();
		boolean isBossRoom = event.getClearedRoom().getRoomData().getStructureData().getStructureType() == StructureType.BOSS_ROOM;
		Location centerRoom = room.getCube().getCenter().getWorldLocation(main.getDungeonWorld());
		if(!isBossRoom) {
			MusicBox.SMALL_WIN.playSong(main, centerRoom, 1f);
			for(DungeonDoor childDoor : event.getClearedRoom().getRoomsDoors()) {
				childDoor.setLocked(false);
			}
		} else {
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
	
}
