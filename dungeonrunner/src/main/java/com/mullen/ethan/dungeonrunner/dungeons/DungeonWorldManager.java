package com.mullen.ethan.dungeonrunner.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import com.mullen.ethan.dungeonrunner.Main;

public class DungeonWorldManager {

	public static String DUNGEON_WORLD_NAME = "DungeonWorld";

	private Main main;
	public static World world;

	public DungeonWorldManager(Main instance) {

		this.main = instance;
		world = Bukkit.getWorld(DUNGEON_WORLD_NAME);

		if(world == null) {
			System.out.println("Dungeon world not found, generating...");
			world = generateWorld();
		}

		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, 0l, 10l);
		
	}

	private void tick() {
		if(world == null) return;
		// Convert survival players to adventure mode
		for(Player p : world.getPlayers()) {
			if(p.getGameMode() == GameMode.SURVIVAL) {
				p.setGameMode(GameMode.ADVENTURE);
			}
		}
	}
	
	private World generateWorld() {
		WorldCreator wc = new WorldCreator(DUNGEON_WORLD_NAME);
		wc.generator(new VoidChunkGenerator());
		World world = wc.createWorld();
	    world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
	    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
	    world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
	    world.setGameRule(GameRule.DO_FIRE_TICK, false);
	    world.setGameRule(GameRule.MOB_GRIEFING, false);
	    return world;
	}

	public World getWorld() {
		return world;
	}

}

class VoidChunkGenerator extends ChunkGenerator {}