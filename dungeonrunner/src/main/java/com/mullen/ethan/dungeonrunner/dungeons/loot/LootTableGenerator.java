package com.mullen.ethan.dungeonrunner.dungeons.loot;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class LootTableGenerator {
	public static float MAX_TIER = 4.5f;
	public static TieredLootTable getTieredLootTable() {
		TieredLootTable t = new TieredLootTable();
		t.addEntry(Material.BREAD, 0, 50, 1, 6);
		t.addEntry(Material.COOKED_PORKCHOP, 0.5f, 50, 1, 5);
		t.addEntry(Material.COOKED_BEEF, 0.5f, 50, 1, 5);
		t.addEntry(Material.WOODEN_SWORD, 1f, 20);
		t.addEntry(Material.WOODEN_AXE, 1f, 40);
		t.addEntry(createSplashPotion(PotionType.INSTANT_HEAL, 20, 2), 1f, 10);
		t.addEntry(Material.LEATHER_HELMET, 1f, 15);
		t.addEntry(Material.LEATHER_CHESTPLATE, 1f, 10);
		t.addEntry(Material.LEATHER_LEGGINGS, 1f, 10);
		t.addEntry(Material.LEATHER_BOOTS, 1f, 15);
		t.addEntry(Material.STICK, 1.5f, 30, 1, 5);
		t.addEntry(Material.STONE_SWORD, 2f, 20);
		t.addEntry(Material.STONE_AXE, 2f, 40);
		t.addEntry(Material.CHAINMAIL_HELMET, 2f, 15);
		t.addEntry(Material.CHAINMAIL_CHESTPLATE, 2f, 10);
		t.addEntry(Material.CHAINMAIL_LEGGINGS, 2f, 10);
		t.addEntry(Material.CHAINMAIL_BOOTS, 2f, 15);
		t.addEntry(Material.STONE_SWORD, 2.5f, 10);
		t.addEntry(Material.STONE_AXE, 2.5f, 10);
		t.addEntry(Material.IRON_INGOT, 2.5f, 20, 3, 7);
		t.addEntry(Material.SHIELD, 2.5f, 10);
		t.addEntry(Material.GOLDEN_APPLE, 2.5f, 5, 1, 2);
		t.addEntry(Material.IRON_SWORD, 3f, 20);
		t.addEntry(Material.IRON_AXE, 3f, 40);
		t.addEntry(Material.GOLDEN_HELMET, 3f, 15);
		t.addEntry(Material.GOLDEN_CHESTPLATE, 3f, 10);
		t.addEntry(Material.GOLDEN_LEGGINGS, 3f, 10);
		t.addEntry(Material.GOLDEN_BOOTS, 3f, 15);
		t.addEntry(createSplashPotion(PotionType.INSTANT_HEAL, 20, 3), 3.5f, 5);
		t.addEntry(createSplashPotion(PotionType.REGEN, 20*15, 1), 3.5f, 5);
		t.addEntry(createSplashPotion(PotionType.SPEED, 20*15, 2), 3.5f, 5);
		t.addEntry(Material.DIAMOND_AXE, 4f, 10);
		t.addEntry(Material.DIAMOND_SWORD, 4.5f, 5);
		
		return t;
	}
	public static ItemStack createSplashPotion(PotionType type, int duration, int lvl) {
		ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION, 1);
		PotionMeta meta = (PotionMeta) splashPotion.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, duration, lvl), false);
		splashPotion.setItemMeta(meta);
		return splashPotion;
	}
	public static ItemStack getRandomMaxEnchantBook() {
		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = item.getItemMeta();
		Enchantment ench = Enchantment.values()[new Random().nextInt(Enchantment.values().length)];
		meta.addEnchant(ench, ench.getMaxLevel(), false);
		item.setItemMeta(meta);
		return item;
	}
}
