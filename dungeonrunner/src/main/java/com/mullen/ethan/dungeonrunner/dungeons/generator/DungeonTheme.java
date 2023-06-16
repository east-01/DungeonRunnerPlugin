package com.mullen.ethan.dungeonrunner.dungeons.generator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public enum DungeonTheme {
	TEST, CAVE;
	public Material getDoorMaterial() {
		switch(this) {
		case CAVE:
			return Material.DEEPSLATE_BRICKS;
		case TEST:
			return Material.STONE_BRICKS;
		}
		return Material.AIR;
	}
	public List<String> getStartRooms() {
		List<String> l = new ArrayList<>();
		switch(this) {
		case CAVE:
			l.add("cave_start_room");
			break;
		case TEST:
			l.add("stone_start");
			break;
		default:
			break;
		}
		return l;
	}
	public List<String> getRooms() {
		List<String> l = new ArrayList<>();
		switch(this) {
		case CAVE:
			l.add("cave_small_room_1");
			l.add("cave_small_room_2");
			l.add("cave_small_room_3");
			l.add("cave_small_room_4");
			l.add("cave_large_room_1");
			l.add("cave_large_room_2");
			l.add("cave_large_room_3");
			l.add("cave_large_room_4");
			l.add("cave_hallway_hub_1");
			l.add("cave_hallway_hub_2");
			l.add("cave_hallway_1");
			l.add("cave_hallway_intersection");
			break;
		case TEST:
			l.add("stone_room");
			break;
		default:
			break;
		}
		return l;
	}
	public List<String> getBossRooms() {
		List<String> l = new ArrayList<>();
		switch(this) {
		case CAVE:
			l.add("cave_boss_room");
			break;
		case TEST:
			l.add("stone_bossroom");
			break;
		default:
			break;
		}
		return l;
	}
}
