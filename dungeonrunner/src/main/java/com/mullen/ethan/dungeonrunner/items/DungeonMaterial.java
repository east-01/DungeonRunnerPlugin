package com.mullen.ethan.dungeonrunner.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.mullen.ethan.customforge.CustomForge;
import com.mullen.ethan.customforge.customitems.CustomItem;

import net.md_5.bungee.api.ChatColor;

public class DungeonMaterial extends CustomItem {

	public String METADATA_KEY = "DUNGEON_MATERIAL";
	public String DISPLAY_NAME = ChatColor.WHITE + "Dungeon Material";
	public String[] LORE = new String[] {ChatColor.GRAY + "Gives one dungeon room"};
	
	public DungeonMaterial(CustomForge main) {
		super(main);				
	}

	@Override
	public ItemStack createItem() {
		ItemStack item = new ItemStack(Material.FIREWORK_STAR);
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
		meta.setDisplayName(DISPLAY_NAME);
		meta.setLore(Arrays.asList(LORE));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean isItem(ItemStack item) {
		if(item == null) return false;
		if(!item.hasItemMeta()) return false;
		return item.getItemMeta().getPersistentDataContainer().has(getKey(), PersistentDataType.BOOLEAN);
	}
	
	public NamespacedKey getKey() {
		return new NamespacedKey(main, METADATA_KEY);
	}
	
}
