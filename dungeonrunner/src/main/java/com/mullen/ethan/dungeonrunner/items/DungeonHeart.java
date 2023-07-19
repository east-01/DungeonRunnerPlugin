package com.mullen.ethan.dungeonrunner.items;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.mullen.ethan.customforge.CustomForge;
import com.mullen.ethan.customforge.customitems.CustomItem;

import net.md_5.bungee.api.ChatColor;

public class DungeonHeart extends CustomItem {

	public String METADATA_KEY = "DUNGEON_HEART";
	public String DISPLAY_NAME = ChatColor.AQUA + "Dungeon Heart";
	public String[] LORE = new String[] {ChatColor.GRAY + "The heart of every dungeon"};
		
	public DungeonHeart(CustomForge main) {
		super(main);			
		Bukkit.getPluginManager().registerEvents(this, main);
	}

	@Override
	public ItemStack createItem() {
		ItemStack item = new ItemStack(Material.END_CRYSTAL);
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
		meta.setDisplayName(DISPLAY_NAME);
		meta.setLore(Arrays.asList(LORE));
		item.setItemMeta(meta);
		return item;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
		ItemStack handItem = event.getItem();
		if(!isItem(handItem)) return;
		event.setCancelled(true);
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
