package com.mullen.ethan.dungeonrunner.dungeons.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.mullen.ethan.dungeonrunner.Main;

public class LootTable {
	private Main main;
	private List<LootEntry> lootEntries;
	private Random random;

	public LootTable(Main main) {
		this.main = main;
		lootEntries = new ArrayList<>();
		random = new Random();
	}

	public void addItem(ItemStack item, int weight, int minAmt, int maxAmt) {
		lootEntries.add(new LootEntry(item, weight, minAmt, maxAmt));
	}

	public ItemStack pollItem() {
		int totalWeight = lootEntries.stream().mapToInt(LootEntry::getWeight).sum();
		int randomWeight = random.nextInt(totalWeight) + 1;

		for (LootEntry entry : lootEntries) {
			randomWeight -= entry.getWeight();
			if (randomWeight <= 0) {
				ItemStack item = entry.getItem();
				int amtRange = entry.getMaxAmt()-entry.getMinAmt();
				int randomization = 0;
				if(amtRange > 0) {
					// Multiply by float^3 to make it rarer to get the higher amount of items
//					randomization = (int) (Math.pow(random.nextFloat(), 3)*random.nextInt(amtRange));
					randomization = random.nextInt(amtRange);
				}
				item.setAmount(entry.getMinAmt() + randomization);
				return item;
			}
		}

		return null; // If no item was selected
	}

	public void addEntry(Material mat, int weight) {
		addEntry(new ItemStack(mat), weight);
	}
	
	public void addEntry(ItemStack item, int weight) {
		addEntry(item, weight, 1, 1);
	}
	
	public void addEntry(String itemID, int weight) {
		addEntry(itemID, weight, 1, 1);
	}
	
	public void addEntry(Material mat, int weight, int minCnt, int maxCnt) {
		addEntry(new ItemStack(mat), weight, minCnt, maxCnt);
	}
		
	public void addEntry(ItemStack item, int weight, int minCnt, int maxCnt) {
		lootEntries.add(new LootEntry(item, weight, minCnt, maxCnt));
	}
	
	public void addEntry(String itemID, int weight, int minCnt, int maxCnt) {
		if(!main.getCustomForge().isCustomItemRegistered(itemID)) {
			Bukkit.getLogger().log(Level.SEVERE, "Tried to add item id \"" + itemID + "\" to loot table but it's not registered.");
			return;
		}
		addEntry(main.getCustomForge().getCustomItem(itemID).createItem(), weight, minCnt, maxCnt);
	}

	private static class LootEntry {
		private ItemStack item;
		private int weight;
		private int minAmt;
		private int maxAmt;

		public LootEntry(ItemStack item, int weight, int minAmt, int maxAmt) {
			this.item = item;
			this.weight = weight;
			this.minAmt = minAmt;
			this.maxAmt = maxAmt;
		}

		public ItemStack getItem() {
			return item;
		}

		public int getWeight() {
			return weight;
		}
		
		public int getMinAmt() {
			return minAmt;
		}
	
		public int getMaxAmt() {
			return maxAmt;
		}
	}
}
