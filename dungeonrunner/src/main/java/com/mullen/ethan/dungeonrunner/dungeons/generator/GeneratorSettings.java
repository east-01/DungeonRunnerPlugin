package com.mullen.ethan.dungeonrunner.dungeons.generator;

public class GeneratorSettings {

	private DungeonTheme theme;
	private int roomCount;
	private int seed;
	
	public GeneratorSettings(DungeonTheme theme, int roomCount, int seed) {
		this.theme = theme;
		this.roomCount = roomCount;
		this.seed = seed;
	}

	public DungeonTheme getTheme() {
		return theme;
	}

	public void setTheme(DungeonTheme theme) {
		this.theme = theme;
	}

	public int getRoomLimit() {
		return roomCount;
	}

	public void setRoomLimit(int roomCount) {
		this.roomCount = roomCount;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}
	
}
