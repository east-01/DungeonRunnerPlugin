package com.mullen.ethan.dungeonrunner.startwell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mullen.ethan.customforge.customitems.CustomItem;
import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.dungeons.generator.GeneratorSettings;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;
import com.mullen.ethan.dungeonrunner.items.DungeonItemLoader;
import com.mullen.ethan.dungeonrunner.utils.Utils;

public class CraftingAnchorMenu implements Listener {

	public static String MENU_NAME = ChatColor.DARK_PURPLE + "Dungeon crafter";
	
	public static List<Integer> VALID_SLOTS = Arrays.asList(3, 4, 5, 12, 13, 14, 21, 22, 23);
	public static int DUNGEON_HEART_SLOT = 10;
	public static int DIFFICULTY_SLOT = 16;
	
	private Random rand;
	
	private Main main;
	private QueueRoom room;
	
	private CustomItem dungeonHeart;
	private CustomItem dungeonMaterial;
	
	public CraftingAnchorMenu(Main main, QueueRoom room) {
		this.rand = new Random();
		this.main = main;
		this.room = room;
		
		this.dungeonHeart = main.getCustomForge().getCustomItem(DungeonItemLoader.DUNGEON_HEART);
		this.dungeonMaterial = main.getCustomForge().getCustomItem(DungeonItemLoader.DUNGEON_MATERIAL);
		
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public Inventory getCrafterInventory() {
		Inventory inv = getBarrelInventory();
		// Fill glass
		for(int slot = 0; slot < inv.getSize(); slot++) {
			if(!isGlassSlot(slot)) continue;
			ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			ItemMeta meta = glass.getItemMeta();
			meta.setDisplayName(" ");
			glass.setItemMeta(meta);
			inv.setItem(slot, glass);
		}
		if(!isHeartPlaced()) {
			inv.setItem(DUNGEON_HEART_SLOT, getMissingDungeonHeartPane());			
		}
		// TODO: Add changing difficulty (also, isGlassSlot() needs to be changed to include this)
		ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = glass.getItemMeta();
		meta.setDisplayName(" ");
		glass.setItemMeta(meta);
		inv.setItem(DIFFICULTY_SLOT, glass);
		return inv;
	}
	
	public void submitCrafting(Player whoSubmitted) {
		
		if(!isValid()) {
			whoSubmitted.sendMessage(ChatColor.RED + "Invalid crafting configuration. Errors:");
			for(String error : craftingErrors()) {
				whoSubmitted.sendMessage(ChatColor.RED + " - " + error);
			}
			return;
		}
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "Generating dungeon. Expect server lag...");
		room.setAnchorLevel(0);
		for(Player p : room.getPlayersInRoom()) {
			p.playSound(QueueRoom.ANCHOR_LOCATION.getWorldLocation(main.getDungeonWorld()), Sound.BLOCK_ANVIL_USE, 0.8f, 0.25f);
		}
		
		// Load theme weights to equal amounts
		HashMap<DungeonTheme, Integer> themeWeights = new HashMap<>();
		for(String themeString : main.getThemeManager().getThemeNames()) {
			DungeonTheme theme = main.getThemeManager().getTheme(themeString);
			themeWeights.put(theme, 10);
		}
		// Initial starting room count, 4-6 rooms
		int roomCount = 0; 
		
		// Loop through inventory. Influences:
		//   - Dungeon material item: +1 room per material
		//   - Dungeon material blocks: +1 to weight per material
		Inventory inv = getCrafterInventory();
		for(int slot : VALID_SLOTS) {
			ItemStack item = inv.getItem(slot);
			if(item == null || item.getType() == Material.AIR) continue;	
			// Check if it's dungeon material
			if(dungeonMaterial.isItem(item)) {
				roomCount += item.getAmount();
				continue;
			}
			// Check if it's a theme material
			boolean isThemeMaterial = false;
			for(DungeonTheme theme : themeWeights.keySet()) {
				if(theme.getMaterials().contains(item.getType())) {
					themeWeights.put(theme, themeWeights.get(theme) + item.getAmount());
					isThemeMaterial = true;
				}
			}
			// Drop items if they're not being used
			if(!isThemeMaterial) {
				main.getDungeonWorld().dropItemNaturally(QueueRoom.ANCHOR_LOCATION.getWorldLocation(main.getDungeonWorld()).add(0.5, 1.25, 0.5), item);
			}
		}
		
		// Pick theme from weights
		String theme = Utils.pickTheme(themeWeights).getName();
		GeneratorSettings settings = new GeneratorSettings(theme, roomCount, -1);
		Dungeon dungeon = new Dungeon(main, settings);
		dungeon.generate();
		main.setCurrentDungeon(dungeon);
				
		// Clear inventory
		inv.clear();
				
	}
	
	public boolean isValid() {
		return craftingErrors().isEmpty();
	}
	
	/** Returns a list of crafting errors with the current inventory */
	public List<String> craftingErrors() {
		List<String> errors = new ArrayList<String>();
		if(!isHeartPlaced()) errors.add(ChatColor.RED + "No dungeon heart. One required.");
		int roomCount = 0;
		for(int slot : VALID_SLOTS) {
			ItemStack item = getBarrelInventory().getItem(slot);
			if(dungeonMaterial.isItem(item)) roomCount += item.getAmount();
		}
		if(roomCount < 5) errors.add(ChatColor.RED + "Not enough dungeon material. 5 dungeon material minimum");
		return errors;
	}
	
	@EventHandler
	public void onPlayerInteract(InventoryClickEvent event) {
		// Checks the views title and compares to the static menu name
		if(event.getClickedInventory() == null) return;
		if(!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(MENU_NAME))) return;
		if(!(event.getWhoClicked() instanceof Player)) return;
		
		Inventory inv = event.getClickedInventory();
		int slot = event.getSlot();
		boolean clickedTopInventory = event.getClickedInventory().getType() == InventoryType.BARREL;
		
		if(isGlassSlot(event.getSlot()) && clickedTopInventory) {
			event.setCancelled(true);
			return;
		}
		
		if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			if(clickedTopInventory) {
				if(slot == DUNGEON_HEART_SLOT) {
					if(isHeartPlaced()) {
						new BukkitRunnable() {
							public void run() {
								inv.setItem(DUNGEON_HEART_SLOT, getMissingDungeonHeartPane());
							}
						}.runTaskLater(main, 1L);
					} else {
						event.setCancelled(true);
					}
				}
			} else {
				ItemStack item = event.getCurrentItem();
				if(dungeonHeart.isItem(item)) {
					event.setCancelled(true);
					if(!isHeartPlaced()) {
						if(item.getAmount() > 1) {
							item.setAmount(item.getAmount()-1);
						} else {
							event.getClickedInventory().setItem(slot, new ItemStack(Material.AIR));
						}
						event.getInventory().setItem(DUNGEON_HEART_SLOT, dungeonHeart.createItem());
					}
				}
			}
		} else if(event.getAction() == InventoryAction.PICKUP_ALL) {
			if(clickedTopInventory) {
				if(slot == DUNGEON_HEART_SLOT) {
					if(isHeartPlaced()) {
						new BukkitRunnable() {
							public void run() {
								inv.setItem(DUNGEON_HEART_SLOT, getMissingDungeonHeartPane());
							}
						}.runTaskLater(main, 1L);
					} else {
						event.setCancelled(true);
					}
				}
			}
		} else if(event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
			if(clickedTopInventory) {
				ItemStack cursor = event.getCursor();
				if(slot == DUNGEON_HEART_SLOT) {
					if(!isHeartPlaced() && dungeonHeart.isItem(cursor)) {
						event.setCancelled(true);
						inv.setItem(DUNGEON_HEART_SLOT, dungeonHeart.createItem());
						if(cursor.getAmount() > 1) {
							cursor.setAmount(cursor.getAmount()-1);
						} else {
							cursor.setType(Material.AIR);
							event.getWhoClicked().setItemOnCursor(null);
							((Player)event.getWhoClicked()).updateInventory();
						}
						
					} else {
						event.setCancelled(true);
					}
				}
			}
		} else if(event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME) {
			if(clickedTopInventory && slot == DUNGEON_HEART_SLOT) {
				event.setCancelled(true);
			}
		}
		
	}
	
	public boolean isHeartPlaced() {
		Inventory barrel = getBarrelInventory();
		ItemStack item = barrel.getItem(DUNGEON_HEART_SLOT);
		return main.getCustomForge().getCustomItem(DungeonItemLoader.DUNGEON_HEART).isItem(item);
	}
	
	private boolean isGlassSlot(int slot) {
		return !VALID_SLOTS.contains(slot) && slot != DUNGEON_HEART_SLOT /*&& slot != DIFFICULTY_SLOT*/;
	}
	
	private Inventory getBarrelInventory() {
		Block blockBelow = QueueRoom.ANCHOR_LOCATION.getWorldLocation(main.getDungeonWorld()).clone().add(0, -1, 0).getBlock();
		if(blockBelow.getType() != Material.BARREL) {
			blockBelow.setType(Material.BARREL);
		}
		Barrel barrel = (Barrel) blockBelow.getState();
		barrel.setCustomName(MENU_NAME);
		barrel.update();
		return barrel.getInventory();
	}
	
	private ItemStack getMissingDungeonHeartPane() {
		ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta = glass.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Dungeon heart missing!");
		meta.setLore(Arrays.asList(ChatColor.GRAY + "" + ChatColor.ITALIC + "A dungeon heart is required to construct a dungeon"));
		glass.setItemMeta(meta);
		return glass;
	}
	
}
