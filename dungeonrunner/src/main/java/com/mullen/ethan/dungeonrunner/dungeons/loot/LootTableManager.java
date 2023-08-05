package com.mullen.ethan.dungeonrunner.dungeons.loot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mullen.ethan.customforge.includedmobs.IncludedMobsRegister;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class LootTableManager {

	public static int REGULAR_TIER_COUNT = 4;
	public static int BOSS_MINOR_TIER_COUNT = 1;
	public static int BOSS_MAJOR_TIER_COUNT = 3;

	private Random rand;
	
	private Main main;
	private HashMap<LootType, LootTable[]> typedTables;
	
	public LootTableManager(Main main) {
		this.rand = new Random();
		this.main = main;
		this.typedTables = new HashMap<LootType, LootTable[]>();
		
		generateTables();
	}
	
	public void populateChest(Chest chest, float progress) {
		int tier = (int) Math.floor(progress*REGULAR_TIER_COUNT);
		if(tier >= REGULAR_TIER_COUNT) tier = REGULAR_TIER_COUNT-1;
		populateChest(chest, false, tier);
	}
	
	public void populateBossChest(Chest chest, int tier) {
		populateChest(chest, true, tier);
	}
	
	public void populateChest(Chest chest, boolean isBoss, int tier) {

		chest.setCustomName(ChatColor.DARK_GRAY + "Tier: " + Arrays.asList(ChatColor.DARK_GRAY, ChatColor.GREEN, ChatColor.BLUE, ChatColor.GOLD).get(tier) + (tier+1));
		chest.update();

		Inventory inv = chest.getInventory();
		inv.clear();

		// Generate big-ticket item
		if(isBoss) {
			ItemStack item = typedTables.get(LootType.BOSS_MAJOR_LOOT)[tier].pollItem();
			inv.setItem(inv.getSize()/2, item);
		}
		
		List<LootType> lowTierTypes = Arrays.asList(LootType.JUNK, LootType.MATERIALS, LootType.FOOD); // Should be the <=50% of the items
		List<LootType> midTierTypes = Arrays.asList(LootType.UTILITY, LootType.ARMOR); // Will be the other >50% (if no high tier)
		LootType highTierType = LootType.WEAPONS; // Will appear if the amount of items is >=5
				
		int itemAmount = 5+rand.nextInt(5); // 5-10 items per chest
		for(int i = 0; i < itemAmount; i++) {
			
			// Figure out the type
			LootType type = null;
			if(!isBoss) {
				type = midTierTypes.get(rand.nextInt(midTierTypes.size()));
				if(i < itemAmount/2.0) {
					type = lowTierTypes.get(rand.nextInt(lowTierTypes.size()));
				} if(i == itemAmount-1 && itemAmount >= 5) {
					type = highTierType;
				}
			} else {
				type = LootType.BOSS_MINOR_LOOT;
				tier = 0; // Set the tier to 0 since there's only one tier
			}
			
			// Find open slot
			int slot = Utils.findRandomOpenSlot(inv);
			
			// Set item polled from the LootType hashmap, then checking the correct tier
			ItemStack item = null;
			if(type == LootType.MATERIALS) {
				List<Material> materialList = main.getCurrentDungeon().getDungeonTheme().getMaterials();
				item = new ItemStack(materialList.get(rand.nextInt(materialList.size())), 1+rand.nextInt(5));
			} else {
				item = typedTables.get(type)[tier].pollItem();
			}
			inv.setItem(slot, item);
		}
	}
	
	public void generateTables() {
		
		// Create linear rarity weights
		int[] lrr6 = createLinearRarityWeights(6);
		
		// Pre-create all of the loottables
		for(LootType type : LootType.values()) {
			int tierAmt = REGULAR_TIER_COUNT;
			if(type == LootType.BOSS_MINOR_LOOT) tierAmt = BOSS_MINOR_TIER_COUNT;
			if(type == LootType.BOSS_MAJOR_LOOT) tierAmt = BOSS_MAJOR_TIER_COUNT;
			typedTables.put(type, new LootTable[tierAmt]);
			for(int i = 0; i < tierAmt; i++) {
				typedTables.get(type)[i] = new LootTable(main);
			}
		}
		
		typedTables.get(LootType.JUNK)[0].addEntry(Material.WHEAT_SEEDS, lrr6[0], 1, 4);
		typedTables.get(LootType.JUNK)[0].addEntry(Material.ROTTEN_FLESH, lrr6[1], 1, 4);
		typedTables.get(LootType.JUNK)[0].addEntry(Material.SUGAR_CANE, lrr6[2], 1, 4);
		typedTables.get(LootType.JUNK)[0].addEntry(Material.BONE_MEAL, lrr6[3], 1, 4);
		typedTables.get(LootType.JUNK)[0].addEntry(Material.SPIDER_EYE, lrr6[4], 1, 2);
		typedTables.get(LootType.JUNK)[0].addEntry(Material.SUGAR, lrr6[5], 1, 3);

		typedTables.get(LootType.JUNK)[1].addEntry(Material.POTATO, lrr6[0], 1, 4);
		typedTables.get(LootType.JUNK)[1].addEntry(Material.BONE, lrr6[1], 1, 4);
		typedTables.get(LootType.JUNK)[1].addEntry(Material.GUNPOWDER, lrr6[2], 1, 4);
		typedTables.get(LootType.JUNK)[1].addEntry(Material.BROWN_MUSHROOM, lrr6[3], 1, 4);
		typedTables.get(LootType.JUNK)[1].addEntry(Material.RED_MUSHROOM, lrr6[4], 1, 4);
		typedTables.get(LootType.JUNK)[1].addEntry(Material.GOLD_NUGGET, lrr6[5], 1, 2);

		typedTables.get(LootType.JUNK)[2].addEntry(Material.BONE, lrr6[0], 1, 4);
		typedTables.get(LootType.JUNK)[2].addEntry(Material.GUNPOWDER, lrr6[1], 1, 4);
		typedTables.get(LootType.JUNK)[2].addEntry(Material.COAL, lrr6[2], 1, 4);
		typedTables.get(LootType.JUNK)[2].addEntry(Material.BOWL, lrr6[3]);
		typedTables.get(LootType.JUNK)[2].addEntry(Material.MELON_SEEDS, lrr6[4], 1, 2);
		typedTables.get(LootType.JUNK)[2].addEntry(Material.PUMPKIN_SEEDS, lrr6[5], 1, 2);

		typedTables.get(LootType.JUNK)[3].addEntry(Material.GUNPOWDER, lrr6[0], 3, 7);
		typedTables.get(LootType.JUNK)[3].addEntry(Material.QUARTZ, lrr6[1], 1, 4);
		typedTables.get(LootType.JUNK)[3].addEntry(Material.COAL, lrr6[2], 2, 4);
		typedTables.get(LootType.JUNK)[3].addEntry(Material.NETHER_WART, lrr6[3], 1, 4);
		typedTables.get(LootType.JUNK)[3].addEntry(Material.EXPERIENCE_BOTTLE, lrr6[4], 1, 3);
		typedTables.get(LootType.JUNK)[3].addEntry(Material.BLAZE_POWDER, lrr6[5], 1, 2);

		typedTables.get(LootType.FOOD)[0].addEntry(Material.COOKIE, lrr6[0], 1, 4);
		typedTables.get(LootType.FOOD)[0].addEntry(Material.BAKED_POTATO, lrr6[1], 1, 4);
		typedTables.get(LootType.FOOD)[0].addEntry(Material.BREAD, lrr6[2], 1, 4);
		typedTables.get(LootType.FOOD)[0].addEntry(Material.PUMPKIN_PIE, lrr6[3], 1, 2);
		typedTables.get(LootType.FOOD)[0].addEntry(Material.COOKED_CHICKEN, lrr6[4], 1, 2);
		typedTables.get(LootType.FOOD)[0].addEntry(Material.COOKED_BEEF, lrr6[5], 1, 3);

		typedTables.get(LootType.FOOD)[1].addEntry(Material.BAKED_POTATO, lrr6[0], 3, 5);
		typedTables.get(LootType.FOOD)[1].addEntry(Material.BREAD, lrr6[1], 3, 5);
		typedTables.get(LootType.FOOD)[1].addEntry(Material.GLISTERING_MELON_SLICE, lrr6[2], 1, 2);
		typedTables.get(LootType.FOOD)[1].addEntry(Material.GOLDEN_CARROT, lrr6[3], 1, 2);
		typedTables.get(LootType.FOOD)[1].addEntry(Material.COOKED_BEEF, lrr6[4], 1, 2);
		typedTables.get(LootType.FOOD)[1].addEntry(Material.REDSTONE, lrr6[5], 1, 3);

		typedTables.get(LootType.FOOD)[2].addEntry(Material.COOKED_PORKCHOP, lrr6[0], 3, 5);
		typedTables.get(LootType.FOOD)[2].addEntry(Material.LAPIS_LAZULI, lrr6[1], 1, 3);
		typedTables.get(LootType.FOOD)[2].addEntry(Material.IRON_INGOT, lrr6[2], 1, 3);
		typedTables.get(LootType.FOOD)[2].addEntry(Material.GLISTERING_MELON_SLICE, lrr6[3], 3, 5);
		typedTables.get(LootType.FOOD)[2].addEntry(Material.GOLDEN_CARROT, lrr6[4], 3, 5);
		typedTables.get(LootType.FOOD)[2].addEntry(Material.EMERALD, lrr6[5], 1, 3);

		typedTables.get(LootType.FOOD)[3].addEntry(Material.IRON_INGOT, lrr6[0], 2, 4);
		typedTables.get(LootType.FOOD)[3].addEntry(Material.FIRE_CHARGE, lrr6[1], 1, 3);
		typedTables.get(LootType.FOOD)[3].addEntry(Material.BLAZE_ROD, lrr6[2], 1, 2);
		typedTables.get(LootType.FOOD)[3].addEntry(Material.ENDER_PEARL, lrr6[3], 1, 3);
		typedTables.get(LootType.FOOD)[3].addEntry(Material.DIAMOND, lrr6[4]);
		typedTables.get(LootType.FOOD)[3].addEntry("DUNGEON_MATERIAL", lrr6[5], 1, 2);

		typedTables.get(LootType.UTILITY)[0].addEntry(Material.ARROW, 1, 1, 2);
		
		typedTables.get(LootType.UTILITY)[1].addEntry(Material.ARROW, 40, 2, 4);
		typedTables.get(LootType.UTILITY)[1].addEntry(Material.SHIELD, 10);
		typedTables.get(LootType.UTILITY)[1].addEntry(createSplashPotion(PotionType.INSTANT_HEAL), 8);
		typedTables.get(LootType.UTILITY)[1].addEntry(Material.GOLDEN_APPLE, 3);

		typedTables.get(LootType.UTILITY)[2].addEntry(Material.ARROW, 40, 2, 4);
		typedTables.get(LootType.UTILITY)[2].addEntry(createArrow(PotionType.INSTANT_HEAL), 10);
		typedTables.get(LootType.UTILITY)[2].addEntry(createSplashPotion(PotionType.REGEN), 8);
		typedTables.get(LootType.UTILITY)[3].addEntry(IncludedMobsRegister.CI_FREEZE_POTION, 5);
		typedTables.get(LootType.UTILITY)[2].addEntry(Material.SPECTRAL_ARROW, 3, 1, 3);

		typedTables.get(LootType.UTILITY)[3].addEntry(Material.SPECTRAL_ARROW, 1, 2, 4);
		typedTables.get(LootType.UTILITY)[3].addEntry(Material.GOLDEN_APPLE, 1);
		typedTables.get(LootType.UTILITY)[3].addEntry(createArrow(PotionType.SLOWNESS), 1);
		typedTables.get(LootType.UTILITY)[3].addEntry(IncludedMobsRegister.CI_FREEZE_POTION, 2);
		
		typedTables.get(LootType.ARMOR)[0].addEntry(Material.LEATHER_BOOTS, 1);
		typedTables.get(LootType.ARMOR)[0].addEntry(Material.LEATHER_LEGGINGS, 1);
		typedTables.get(LootType.ARMOR)[0].addEntry(Material.LEATHER_CHESTPLATE, 1);
		typedTables.get(LootType.ARMOR)[0].addEntry(Material.LEATHER_HELMET, 1);

		typedTables.get(LootType.ARMOR)[1].addEntry(Material.LEATHER_BOOTS, 6);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.LEATHER_LEGGINGS, 6);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.LEATHER_CHESTPLATE, 6);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.LEATHER_HELMET, 6);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.GOLDEN_BOOTS, 1);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.GOLDEN_LEGGINGS, 1);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.GOLDEN_CHESTPLATE, 1);
		typedTables.get(LootType.ARMOR)[1].addEntry(Material.GOLDEN_HELMET, 1);

		typedTables.get(LootType.ARMOR)[2].addEntry(Material.GOLDEN_BOOTS, 6);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.GOLDEN_LEGGINGS, 6);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.GOLDEN_CHESTPLATE, 6);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.GOLDEN_HELMET, 6);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.CHAINMAIL_BOOTS, 1);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.CHAINMAIL_LEGGINGS, 1);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.CHAINMAIL_CHESTPLATE, 1);
		typedTables.get(LootType.ARMOR)[2].addEntry(Material.CHAINMAIL_HELMET, 1);

		typedTables.get(LootType.ARMOR)[3].addEntry(Material.CHAINMAIL_BOOTS, 4);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.CHAINMAIL_LEGGINGS, 4);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.CHAINMAIL_CHESTPLATE, 4);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.CHAINMAIL_HELMET, 4);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.IRON_BOOTS, 1);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.IRON_LEGGINGS, 1);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.IRON_CHESTPLATE, 1);
		typedTables.get(LootType.ARMOR)[3].addEntry(Material.IRON_HELMET, 1);

		typedTables.get(LootType.WEAPONS)[0].addEntry(Material.WOODEN_AXE, 20);
		typedTables.get(LootType.WEAPONS)[0].addEntry(Material.WOODEN_SWORD, 13);
		typedTables.get(LootType.WEAPONS)[0].addEntry(Material.STONE_AXE, 6);
		typedTables.get(LootType.WEAPONS)[0].addEntry(Material.STONE_SWORD, 1);

		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.STONE_AXE, 30);
		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.STONE_SWORD, 25);
		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.BOW, 10);
		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.GOLDEN_AXE, 8);
		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.GOLDEN_SWORD, 6);
		typedTables.get(LootType.WEAPONS)[1].addEntry(Material.IRON_AXE, 1);
		
		typedTables.get(LootType.WEAPONS)[2].addEntry(Material.GOLDEN_SWORD, 15);
		typedTables.get(LootType.WEAPONS)[2].addEntry(Material.BOW, 15);
		typedTables.get(LootType.WEAPONS)[2].addEntry(Material.IRON_AXE, 6);
		typedTables.get(LootType.WEAPONS)[2].addEntry(Material.IRON_SWORD, 1);		

		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.IRON_AXE, 35);
		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.IRON_SWORD, 35);
		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.CROSSBOW, 10);
		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.DIAMOND_AXE, 8);
		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.DIAMOND_SWORD, 3);
		typedTables.get(LootType.WEAPONS)[3].addEntry(Material.TRIDENT, 1);

		// If more than one tier gets added make sure to remove "tier = 0;" in populate chest. Use ctrl+F to find it.
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(Material.IRON_INGOT, 20, 2, 5);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(Material.DIAMOND, 10, 2, 5);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(Material.ENDER_PEARL, 8, 1, 3);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry("DUNGEON_MATERIAL", 5, 1, 4);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(Material.ANCIENT_DEBRIS, 1, 1, 2);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(Material.SHULKER_SHELL, 1);
		typedTables.get(LootType.BOSS_MINOR_LOOT)[0].addEntry(IncludedMobsRegister.CI_MINERS_LANTERN, 5);
		
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.DIAMOND_SWORD, 10);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.DIAMOND_PICKAXE, 10);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.DIAMOND_SHOVEL, 10);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.DIAMOND_AXE, 10);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.NETHERITE_PICKAXE, 2);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[0].addEntry(Material.NETHERITE_SWORD, 2);
		
		ItemStack goodDiamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		goodDiamondPickaxe.addEnchantment(Enchantment.DIG_SPEED, 5);
		goodDiamondPickaxe.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
		goodDiamondPickaxe.addEnchantment(Enchantment.DURABILITY, 3);		
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(goodDiamondPickaxe, 10);
		ItemStack goodNetheritePickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
		goodNetheritePickaxe.addEnchantment(Enchantment.DIG_SPEED, 5);
		goodNetheritePickaxe.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
		goodNetheritePickaxe.addEnchantment(Enchantment.DURABILITY, 3);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(goodNetheritePickaxe, 10);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(createMaxEnchantBook(Enchantment.MENDING), 5);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(createMaxEnchantBook(Enchantment.DIG_SPEED), 5);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(createMaxEnchantBook(Enchantment.DURABILITY), 5);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(createMaxEnchantBook(Enchantment.DAMAGE_ALL), 5);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(createMaxEnchantBook(Enchantment.PROTECTION_ENVIRONMENTAL), 5);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[1].addEntry(IncludedMobsRegister.CI_TREEFELLING_AXE, 1);
		
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[2].addEntry(IncludedMobsRegister.CI_EXPLOSIVE_PICK, 1);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[2].addEntry(IncludedMobsRegister.CI_SMELTERS_PICK, 1);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[2].addEntry(IncludedMobsRegister.CI_TREEFELLING_AXE, 2);
		typedTables.get(LootType.BOSS_MAJOR_LOOT)[2].addEntry(IncludedMobsRegister.CI_TELEPORTER_CRYSTAL, 2);		
	}
	
	public static ItemStack createSplashPotion(PotionType type) {
		ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION, 1);
		PotionMeta meta = (PotionMeta) splashPotion.getItemMeta();
		meta.setBasePotionData(new PotionData(type));
		splashPotion.setItemMeta(meta);
		return splashPotion;
	}
	public static ItemStack createArrow(PotionType type) {
		ItemStack splashPotion = new ItemStack(Material.TIPPED_ARROW, 1);
		PotionMeta meta = (PotionMeta) splashPotion.getItemMeta();
		meta.setBasePotionData(new PotionData(type));
		splashPotion.setItemMeta(meta);
		return splashPotion;
	}
	public static ItemStack createMaxEnchantBook(Enchantment ench) {
		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(ench, ench.getMaxLevel(), false);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack getRandomMaxEnchantBook() {
		return createMaxEnchantBook(Enchantment.values()[new Random().nextInt(Enchantment.values().length)]);
	}	
	private int[] createLinearRarityWeights(int amount) {
		int[] arr = new int[amount];
		for(int i = 0; i < amount; i++) {
			arr[i] = -2*i + 20;
		}
		return arr;
	}
	
}
