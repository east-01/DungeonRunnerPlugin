package com.mullen.ethan.dungeonrunner.dungeons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mullen.ethan.custombosses.events.CustomBossMobSpawnEvent;
import com.mullen.ethan.dungeonrunner.Main;

public class DungeonMobManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<Entity> dungeonMobs;
	private boolean bossSpawned;
	private Entity boss;
	
	public DungeonMobManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		
		this.dungeonMobs = new ArrayList<Entity>();
		this.bossSpawned = false;
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

	public Entity getBoss() {
		return boss;
	}
	
	@EventHandler
	public void onCustomBossMobSpawn(CustomBossMobSpawnEvent event) {
		if(!event.getSpawnLocation().getWorld().getName().equals(DungeonWorldManager.DUNGEON_WORLD_NAME)) return;
		if(event.isBoss()) {
			this.bossSpawned = true;
			this.boss = event.getCustomMob();
		}
		addMob(event.getCustomMob());
	}
		
}
