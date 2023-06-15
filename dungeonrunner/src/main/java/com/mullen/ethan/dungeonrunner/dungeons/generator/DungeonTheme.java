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
			l.add("cave_start");
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
			l.add("cave_room_1");
			l.add("cave_room_2");
			l.add("cave_room_3");
			l.add("cave_room_4");
			l.add("cave_bigroom_1");
			l.add("cave_bigroom_2");
			l.add("cave_bigroom_3");
			l.add("cave_bigroom_4");
			l.add("cave_hub");
			l.add("cave_hub_2");
			l.add("cave_hall");
			l.add("cave_intersection");
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
			l.add("cave_bossroom");
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
