package com.mullen.ethan.dungeonrunner.startwell;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.mullen.ethan.dungeonrunner.Main;
import com.mullen.ethan.dungeonrunner.dungeons.Dungeon;
import com.mullen.ethan.dungeonrunner.fileloading.DungeonTheme;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class DungeonTome implements Listener {

	public static int LANDING_PAGE_NUMBER = 0;
	public static int CRAFTER_PAGE_NUMBER = 1;
	public static int HEART_PAGE_NUMBER = 3;
	public static int MATERIAL_PAGE_NUMBER = 4;
	public static int THEMES_PAGE_NUMBER = 5;
	
	private static String HEADER = "     " + ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "Dungeon Tome\n\n";
	
	private Main main;
	private ItemStack book;
	
	public DungeonTome(Main main) {
		this.main = main;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public void openBook(Player p) {
		
		createBook();
		p.openBook(book);
		
	}
	
	public void createBook() {
		
		book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setAuthor("Eastin");
		meta.setTitle("Blank");
		// Add pages to the book
		meta.spigot().addPage(new BaseComponent[] {landingPage()});
		meta.spigot().addPage(new BaseComponent[] {crafterPageOne()});
		meta.spigot().addPage(new BaseComponent[] {crafterPageTwo()});
		meta.spigot().addPage(new BaseComponent[] {heartPage()});
		meta.spigot().addPage(new BaseComponent[] {materialPage()});
		meta.spigot().addPage(new BaseComponent[] {themeBasePage()});
		for(String themeName : main.getThemeManager().getThemeNames()) {
			meta.spigot().addPage(new BaseComponent[] {createThemePage(main.getThemeManager().getTheme(themeName))});			
		}
		
		book.setItemMeta(meta);
		
	}
	
	public BaseComponent landingPage() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Current dungeon:\n");
		Dungeon dungeon = main.getCurrentDungeon();
		if(dungeon == null) {
			component.addExtra(ChatColor.RED + "None" + "\n");
			component.addExtra("\n\n\n");
		} else {
			component.addExtra(ChatColor.DARK_GRAY + "Theme: " + ChatColor.GOLD + capitalizeFirstLetter(dungeon.getDungeonTheme().getName()) + "\n");
			component.addExtra(ChatColor.DARK_GRAY + "Difficulty: " + ChatColor.GOLD + "Normal" + "\n");
			component.addExtra(ChatColor.DARK_GRAY + "Size: " + ChatColor.GOLD + capitalizeFirstLetter(dungeon.getSize().toString().toLowerCase()) + "\n");
			component.addExtra("\n");
		}

		component.addExtra(ChatColor.BOLD + "Table of contents:\n");
		component.addExtra("(" + (CRAFTER_PAGE_NUMBER+1) + ") " + ChatColor.DARK_GRAY + "Dungeon Crafter\n");
		component.addExtra("(" + (HEART_PAGE_NUMBER+1) + ") " + ChatColor.DARK_GRAY + "Dungeon Heart\n");
		component.addExtra("(" + (MATERIAL_PAGE_NUMBER+1) + ") " + ChatColor.DARK_GRAY + "Dungeon Material\n");
		component.addExtra("(" + (THEMES_PAGE_NUMBER+1) + ") " + ChatColor.DARK_GRAY + "Dungeon Themes\n");
		return component;
	}
	
	public BaseComponent crafterPageOne() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Dungeon crafter:\n");
		component.addExtra(ChatColor.DARK_GRAY + "A tool for players to create dungeons. "
				+ "Certain materials have effects on the resulting dungeon. "
				+ "See next page for crafting materials. "
				+ "You can see themed materials in the Dungeon themes section.\n");
		return component;
	}	

	public BaseComponent crafterPageTwo() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BLACK + "Crafting Materials:\n");
		component.addExtra(ChatColor.DARK_GRAY + "Dungeon heart:\n");
		component.addExtra(ChatColor.RED + " Mandatory\n");
		component.addExtra(ChatColor.DARK_GRAY + "Dungeon material:\n");
		component.addExtra(ChatColor.GOLD + " +1 room\n");
		component.addExtra(ChatColor.DARK_GRAY + "Themed materials:\n");
		component.addExtra(ChatColor.GOLD + " +1 theme weight\n");
		return component;
	}	

	public BaseComponent heartPage() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Dungeon heart:\n");
		component.addExtra(ChatColor.DARK_GRAY + "The heart of every dungeon. Craftable:\n\n");
		component.addExtra(ChatColor.GRAY + " # " + ChatColor.BLACK + " |" + ChatColor.DARK_GRAY + " C " + ChatColor.BLACK + "| " + ChatColor.GRAY + " #\n");
		component.addExtra(ChatColor.BLACK + "---------\n");
		component.addExtra(ChatColor.AQUA + " D " + ChatColor.BLACK + " |" + ChatColor.AQUA + " D " + ChatColor.BLACK + "| " + ChatColor.AQUA + " D\n");
		component.addExtra(ChatColor.BLACK + "---------\n");
		component.addExtra(ChatColor.GRAY + " # " + ChatColor.BLACK + " |" + ChatColor.DARK_GRAY + " C " + ChatColor.BLACK + "| " + ChatColor.GRAY + " #\n");
		component.addExtra(ChatColor.GRAY + "# " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Iron ingot\n");
		component.addExtra(ChatColor.DARK_GRAY + "C " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Cobblestone\n");
		component.addExtra(ChatColor.AQUA + "D " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Diamond");
		return component;		
	}
	
	public BaseComponent materialPage() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Dungeon material:\n");
		component.addExtra(ChatColor.DARK_GRAY + "An item used to build rooms. Craftable:\n\n");
		component.addExtra(ChatColor.GOLD + " # " + ChatColor.BLACK + " |" + ChatColor.DARK_GRAY + " C " + ChatColor.BLACK + "| " + ChatColor.GOLD + " #\n");
		component.addExtra(ChatColor.BLACK + "---------\n");
		component.addExtra(ChatColor.DARK_GRAY + " C " + ChatColor.BLACK + " |" + ChatColor.YELLOW + " G " + ChatColor.BLACK + "| " + ChatColor.DARK_GRAY + " C\n");
		component.addExtra(ChatColor.BLACK + "---------\n");
		component.addExtra(ChatColor.GOLD + " # " + ChatColor.BLACK + " |" + ChatColor.DARK_GRAY + " C " + ChatColor.BLACK + "| " + ChatColor.GOLD + " #\n");
		component.addExtra(ChatColor.GOLD + "# " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Copper block\n");
		component.addExtra(ChatColor.DARK_GRAY + "C " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Cobblestone\n");
		component.addExtra(ChatColor.YELLOW + "G " + ChatColor.BLACK + "= " + ChatColor.DARK_GRAY + "Gold ingot");
		return component;		
	}
	
	public BaseComponent themeBasePage() {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Dungeon Themes:\n");
		component.addExtra(ChatColor.DARK_GRAY + "Each dungeon is themed. "
				+ "Each theme has materials you can use in the crafter to increase chances of getting that theme. "
				+ "The following pages list each theme.\n");
		return component;
	}
	
	public BaseComponent createThemePage(DungeonTheme theme) {
		BaseComponent component = new TextComponent();
		component.addExtra(HEADER);
		component.addExtra(ChatColor.BOLD + "Theme: " + ChatColor.GOLD + capitalizeFirstLetter(theme.getName()) + "\n\n");
		component.addExtra(ChatColor.BLACK + "Theme Materials:\n");
		
		StringBuilder sb = new StringBuilder("");
		for(Material mat : theme.getMaterials()) {
			sb.append(mat.name().replaceAll("_", " ") + ", ");
		}
		String materials = capitalizeFirstLetter(sb.toString().substring(0, sb.toString().length()-2).toLowerCase());
		
		component.addExtra(ChatColor.DARK_GRAY + materials);
		return component;
	}
	
	public String capitalizeFirstLetter(String sentence) {
	    if (sentence == null || sentence.isEmpty()) return sentence;
	    char firstChar = sentence.charAt(0);
	    if (Character.isLowerCase(firstChar)) {
	        firstChar = Character.toUpperCase(firstChar);
	        return firstChar + sentence.substring(1);
	    }
	    return sentence;
	}
	
}
