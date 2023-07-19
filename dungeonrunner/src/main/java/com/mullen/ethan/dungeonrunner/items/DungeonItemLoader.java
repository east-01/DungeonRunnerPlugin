package com.mullen.ethan.dungeonrunner.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import com.mullen.ethan.customforge.CustomForge;
import com.mullen.ethan.dungeonrunner.Main;

public class DungeonItemLoader {
	public static String DUNGEON_MATERIAL = "DUNGEON_MATERIAL";
	public static String DUNGEON_HEART = "DUNGEON_HEART";
	
	public static void loadItemsAndRecipes(Main main) {
		CustomForge customForge = main.getCustomForge();
		try {
			customForge.registerCustomItem(DUNGEON_MATERIAL, new DungeonMaterial(customForge));
			customForge.registerCustomItem(DUNGEON_HEART, new DungeonHeart(customForge));
			// Add dungeon material recipe
			ShapedRecipe materialRecipe = new ShapedRecipe(new NamespacedKey(main, DUNGEON_MATERIAL + "_RECIPE"), customForge.getCustomItem(DUNGEON_MATERIAL).createItem());
			materialRecipe.shape("*c*", "cgc", "*c*");
			materialRecipe.setIngredient('*', Material.COPPER_BLOCK);
			materialRecipe.setIngredient('c', Material.COBBLESTONE);
			materialRecipe.setIngredient('g', Material.GOLD_INGOT);
			Bukkit.getServer().addRecipe(materialRecipe);
			
			ShapedRecipe heartRecipe = new ShapedRecipe(new NamespacedKey(main, DUNGEON_HEART + "_RECIPE"), customForge.getCustomItem(DUNGEON_HEART).createItem());
			heartRecipe.shape("ici", "ddd", "ici");
			heartRecipe.setIngredient('i', Material.IRON_INGOT);
			heartRecipe.setIngredient('c', Material.COBBLESTONE);
			heartRecipe.setIngredient('d', Material.DIAMOND);
			Bukkit.getServer().addRecipe(heartRecipe);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
