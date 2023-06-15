package com.mullen.ethan.dungeonrunner.dungeons.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class LootTableGenerator {
	public static LootTable[] populateLootTables() {
		LootTable[] tables = new LootTable[3];
		LootTable tier1 = new LootTable();
		tier1.addItem(new ItemStack(Material.AIR), 80);
		tier1.addItem(new ItemStack(Material.TORCH), 10);
		tier1.addItem(new ItemStack(Material.BREAD, 3), 5);
		tier1.addItem(new ItemStack(Material.WOODEN_SWORD), 2);
		tier1.addItem(new ItemStack(Material.COOKED_BEEF, 3), 2);
		tier1.addItem(new ItemStack(Material.IRON_AXE), 1);
		tables[0] = tier1;
		LootTable tier2 = new LootTable();
		tier2.addItem(new ItemStack(Material.AIR), 60);
		tier2.addItem(new ItemStack(Material.COOKED_PORKCHOP, 3), 5);
		tier2.addItem(new ItemStack(Material.COOKED_BEEF, 3), 5); // 70
		tier2.addItem(new ItemStack(Material.STONE_SWORD), 5);
		tier2.addItem(createSplashPotion(PotionType.INSTANT_HEAL), 2);
		tier2.addItem(new ItemStack(Material.IRON_SWORD), 2);
		tier2.addItem(new ItemStack(Material.LEATHER_CHESTPLATE), 1);
		tier2.addItem(new ItemStack(Material.DIAMOND_AXE), 1);
		tables[1] = tier2;
		LootTable tier3 = new LootTable();
		tier3.addItem(new ItemStack(Material.AIR), 50);
		tier3.addItem(new ItemStack(Material.IRON_SWORD), 5);
		tier3.addItem(createSplashPotion(PotionType.STRENGTH), 2);
		tier3.addItem(createSplashPotion(PotionType.SPEED), 2);
		tier3.addItem(createSplashPotion(PotionType.NIGHT_VISION), 2);
		tier3.addItem(createSplashPotion(PotionType.INVISIBILITY), 2);
		tier3.addItem(createSplashPotion(PotionType.FIRE_RESISTANCE), 2); // 75
		tier3.addItem(new ItemStack(Material.DIAMOND_SWORD), 2);
		tier3.addItem(new ItemStack(Material.IRON_CHESTPLATE), 2);
		tier3.addItem(new ItemStack(Material.IRON_HELMET), 2);
		tier3.addItem(new ItemStack(Material.IRON_LEGGINGS), 2);
		tier3.addItem(new ItemStack(Material.IRON_BOOTS), 2);
		tables[2] = tier3;
		return tables;
	}
	public static ItemStack createSplashPotion(PotionType type) {
		ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION, 1);
		PotionMeta meta = (PotionMeta) splashPotion.getItemMeta();
		meta.setBasePotionData(new PotionData(type));
		splashPotion.setItemMeta(meta);
		return splashPotion;
	}
}
