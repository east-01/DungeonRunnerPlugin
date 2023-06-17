package com.mullen.ethan.dungeonrunner.dungeons.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TieredLootTable {

	private Random rand;
	private List<TieredLootEntry> entries;
	
	private float minTier, maxTier;
	
	public TieredLootTable() {
		this.entries = new ArrayList<TieredLootEntry>();
		this.rand = new Random();
	}
	
	public ItemStack pollEntry(float inputTier) {
		float minTier = clamp(inputTier-1, this.minTier, this.maxTier);
		float maxTier = clamp(inputTier+1, this.minTier, this.maxTier);
		float range = maxTier - minTier;
		// New loot table after tier -> weight calculation is made
		List<TieredLootEntry> newEntries = new ArrayList<TieredLootEntry>();
		for(TieredLootEntry entry : entries) {
			// We don't include entries that aren't in the min-max tier range
			if(entry.getTier() < minTier || entry.getTier() > maxTier) continue;
			// Make the tier fit into the min-max range
			float normalizedTier = entry.getTier() - minTier;
			// Provides a 0-1 mapping of this tier in the range, we can apply this to the function
			float value = normalizedTier / range;
			int newWeight = (int) ((float)entry.getWeight() * applyFunction(value));
			if(newWeight <= 0) newWeight = 1;
			newEntries.add(new TieredLootEntry(entry.getItem(), newWeight, entry.getMinCnt(), entry.getMaxCnt()));
		}
		
		if(newEntries.size() == 1) {
        	int countRange = newEntries.get(0).getMaxCnt() - newEntries.get(0).getMinCnt();
        	ItemStack item = newEntries.get(0).getItem();
        	int randomization = countRange > 0 ? rand.nextInt(countRange) : 0;
        	item.setAmount(newEntries.get(0).getMinCnt() + randomization);
        	return item;
		}
		
        int totalWeight = 0;
        for(TieredLootEntry entry : newEntries) {
        	totalWeight += entry.getWeight();
        }
        int randomWeight = rand.nextInt(totalWeight) + 1;

        for (TieredLootEntry entry : newEntries) {
            randomWeight -= entry.getWeight();
            if (randomWeight <= 0) {
            	int countRange = entry.getMaxCnt() - entry.getMinCnt();
            	ItemStack item = entry.getItem();
            	int randomization = countRange > 0 ? rand.nextInt(countRange) : 0;
            	item.setAmount(entry.getMinCnt() + randomization);
                return item;
            }
        }
        
        return null;

	}
	
	public void addEntry(Material mat, float tier, int weight) {
		addEntry(new ItemStack(mat), tier, weight);
	}
	
	public void addEntry(ItemStack item, float tier, int weight) {
		addEntry(item, tier, weight, 1, 1);
	}
	
	public void addEntry(Material mat, float tier, int weight, int minCnt, int maxCnt) {
		addEntry(new ItemStack(mat), tier, weight, minCnt, maxCnt);
	}
	
	public void addEntry(ItemStack item, float tier, int weight, int minCnt, int maxCnt) {
		if(tier < 0) {
			Bukkit.getLogger().log(Level.SEVERE, "Tiers cannot be negative in TieredLootTables");
			return;
		}
		if(tier < minTier) {
			minTier = tier;
		}
		if(tier > maxTier) {
			maxTier = tier;
		}
		entries.add(new TieredLootEntry(item, tier, weight, minCnt, maxCnt));
	}
	
	private float clamp(float value, float min, float max) {
		if(value < min) {
			return min;
		}
		if(value > max) {
			return max;
		}
		return value;
	}

	public float applyFunction(float value) {
		value = clamp(value, 0, 1);
		return (float) Math.exp(-12*Math.pow(value-0.5, 2));
	}
	
}

class TieredLootEntry {
    private ItemStack item;
    private float tier;
    private int weight;
	private int maxCnt, minCnt;
    
    public TieredLootEntry(ItemStack item, float tier, int weight, int minCnt, int maxCnt) {
        this.item = item;
        this.tier = tier;
        this.weight = weight;
        this.minCnt = minCnt;
        this.maxCnt = maxCnt;
    }

    public TieredLootEntry(ItemStack item, int weight, int minCnt, int maxCnt) {
    	this(item, -1, weight, minCnt, maxCnt);
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public float getTier() {
		return tier;
	}

	public int getMaxCnt() {
		return maxCnt;
	}

	public int getMinCnt() {
		return minCnt;
	}
}
