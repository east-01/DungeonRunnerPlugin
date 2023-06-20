package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomClearEvent;
import com.mullen.ethan.dungeonrunner.events.DungeonRoomPopulateEvent;
import com.mullen.ethan.dungeonrunner.maps.DungeonMapRenderer;

public class DungeonMapManager implements Listener {

	private Main main;
	private Dungeon dungeon;
	
	private List<DungeonMapRenderer> renderers;
	
	public DungeonMapManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		this.renderers = new ArrayList<DungeonMapRenderer>();
		
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public void addRenderer(DungeonMapRenderer renderer) {
		renderers.add(renderer);
	}
	
	public void invalidateMasterImages() {
		for(DungeonMapRenderer renderer : renderers) {
			renderer.invalidateMasterImage();
		}
	}
	
	@EventHandler
	public void onRoomPopulate(DungeonRoomPopulateEvent event) {
		invalidateMasterImages();
	}

	@EventHandler
	public void onRoomComplete(DungeonRoomClearEvent event) {
		invalidateMasterImages();
	}

}
