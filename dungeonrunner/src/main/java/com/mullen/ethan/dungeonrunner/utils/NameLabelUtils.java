package com.mullen.ethan.dungeonrunner.utils;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class NameLabelUtils {

	public static ArmorStand createTextDisplay(String text, Location location) {
		ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		as.setCustomName(text);
		as.setCustomNameVisible(true);
		as.setGravity(false);
		as.setVisible(false);
		as.setCollidable(false);
		as.setMarker(true);
		return as;
	}
	
	public static ArmorStand[] createTextDisplay(String textList[], Location location) {
		ArmorStand[] as = new ArmorStand[textList.length];
		for(int i = 0; i < textList.length; i++) {
			ArmorStand stand = createTextDisplay(textList[i], location.clone().add(0, -0.3*i, 0));
			as[i] = stand;
		}
		return as;
	}
	
}
