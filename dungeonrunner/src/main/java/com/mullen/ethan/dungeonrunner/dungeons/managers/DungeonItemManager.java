package com.mullen.ethan.dungeonrunner.dungeons.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.maps.DungeonMapRenderer;

import net.md_5.bungee.api.ChatColor;

public class DungeonItemManager implements Listener {

	public static String DUNGEON_ONLY_KEY = "DUNGEON_ONLY";
	public static String UNDROPPABLE_KEY = "UNDROPPABLE";
	
	private Main main;
	private Dungeon dungeon;
	
	public DungeonItemManager(Main main, Dungeon dungeon) {
		this.main = main;
		this.dungeon = dungeon;
		
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public ItemStack createDungeonMap() {
		
		MapView view = Bukkit.createMap(main.getDungeonWorld());
		// Clear other map renderers
		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}
		DungeonMapRenderer renderer = new DungeonMapRenderer(main);
		dungeon.addMapRenderer(renderer);
		view.addRenderer(renderer);
		
		ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta) mapItem.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Dungeon Map");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "" + ChatColor.ITALIC + "Un-droppable"));
		meta.setMapView(view);
		mapItem.setItemMeta(meta);
		
		mapItem = applyKey(mapItem, DUNGEON_ONLY_KEY);
		mapItem = applyKey(mapItem, UNDROPPABLE_KEY);
		
		return mapItem;
	}
	
	public ItemStack applyKey(ItemStack item, String key) {
		ItemMeta meta = item.getItemMeta();
		NamespacedKey nsKey = new NamespacedKey(main, key);		
		meta.getPersistentDataContainer().set(nsKey, PersistentDataType.BOOLEAN, true);
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean hasKey(ItemStack item, String key) {
		if(item == null) return false;
		if(!item.hasItemMeta()) return false;
		NamespacedKey nsKey = new NamespacedKey(main, key);	
		return item.getItemMeta().getPersistentDataContainer().has(nsKey, PersistentDataType.BOOLEAN);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		if(!hasKey(event.getItemDrop().getItemStack(), UNDROPPABLE_KEY)) return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onShiftClick(InventoryClickEvent event) {
		if(event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
		ItemStack item = event.getCurrentItem();
		if(!hasKey(item, UNDROPPABLE_KEY)) return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryMove(InventoryClickEvent event) {
		boolean isPlaceAction = event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME;
		if(!isPlaceAction) return;
		if(event.getClickedInventory().getType() == InventoryType.PLAYER) return;
		ItemStack item = event.getCursor();
		if(!hasKey(item, UNDROPPABLE_KEY)) return;
		event.setCancelled(true);
	}
	
	// Remove dungeon maps from players who died
	@EventHandler
	public void playerDeathEvent(PlayerDeathEvent event) {
		List<ItemStack> toRemove = new ArrayList<>();
		for(ItemStack item : event.getDrops()) {
			if(item.getType() != Material.FILLED_MAP) continue;
			if(!hasKey(item, DUNGEON_ONLY_KEY)) continue;
			toRemove.add(item);
		}
		event.getDrops().removeAll(toRemove);
	}
	
	public void clearDungeonItems(Player p) {
		PlayerInventory inv = p.getInventory();
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if(hasKey(item, DUNGEON_ONLY_KEY)) {
				inv.setItem(i, new ItemStack(Material.AIR));
			}
		}
	}
	
}
