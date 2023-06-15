package com.mullen.ethan.dungeonrunner.dungeons.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

public class LootTable {
    private List<LootEntry> lootEntries;
    private Random random;

    public LootTable() {
        lootEntries = new ArrayList<>();
        random = new Random();
    }

    public void addItem(ItemStack item, int weight) {
        lootEntries.add(new LootEntry(item, weight));
    }

    public ItemStack pollItem() {
        int totalWeight = lootEntries.stream().mapToInt(LootEntry::getWeight).sum();
        int randomWeight = random.nextInt(totalWeight) + 1;

        for (LootEntry entry : lootEntries) {
            randomWeight -= entry.getWeight();
            if (randomWeight <= 0) {
                return entry.getItem();
            }
        }

        return null; // If no item was selected
    }

    private static class LootEntry {
        private ItemStack item;
        private int weight;

        public LootEntry(ItemStack item, int weight) {
            this.item = item;
            this.weight = weight;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getWeight() {
            return weight;
        }
    }
}
