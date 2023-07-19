package com.mullen.ethan.dungeonrunner.teleporter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.dungeonrunner.Main;

public class TeleporterManager {

	private Main main;
	private List<Teleporter> teleporters;
	
	public TeleporterManager(Main main) {
		this.main = main;
		this.teleporters = new ArrayList<Teleporter>();
		
		new BukkitRunnable() {
			public void run() {
				for(Teleporter porter : teleporters) {
					porter.tick();
				}
			}
		}.runTaskTimer(main, 0L, 5L);
	}
	
	public Teleporter getTeleporter(String id) {
		for(Teleporter porter : teleporters) {
			if(porter.getID().equals(id)) {
				return porter;
			}
		}
		return null;
	}
	
	public Teleporter spawnTeleporter(String id, Location origin, String destinationName, Location destination) {
		Teleporter porter = new Teleporter(id, destinationName, origin, destination);
		porter.spawn();
		teleporters.add(porter);
		return porter;
	}
	
	public void clearTeleporters() {
		for(Teleporter porter : teleporters) {
			porter.remove();
		}
		this.teleporters = new ArrayList<>();
	}
	
}