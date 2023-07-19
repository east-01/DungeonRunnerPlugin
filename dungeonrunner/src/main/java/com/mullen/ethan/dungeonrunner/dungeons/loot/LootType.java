package com.mullen.ethan.dungeonrunner.dungeons.loot;

public enum LootType {
//  ---------L1----------.........L2.......---L3---
	JUNK, MATERIALS, FOOD, UTILITY, ARMOR, WEAPONS,
	// Minor loot in the chest that spawns around the single major item
	BOSS_MINOR_LOOT, 
	// One item that is the largest rewards, only one will be spawned at a time
	BOSS_MAJOR_LOOT;
}
