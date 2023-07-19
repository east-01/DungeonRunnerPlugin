package com.mullen.ethan.dungeonrunner.teleporter;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.mullen.ethan.dungeonrunner.utils.NameLabelUtils;

import net.md_5.bungee.api.ChatColor;

public class Teleporter {
		
	private Random rand;
	
	private String id;
	private ArmorStand armorStand;
	private Location origin;
	private Location destination;
	private String destinationName;
	private ArmorStand[] nameStands;
		
	public Teleporter(String id, String destinationName, Location origin, Location destination) {
		this.rand = new Random();
		this.id = id;
		this.destinationName = destinationName;
		this.origin = origin;
		this.destination = destination;
	}
	
	public void spawn() {
		armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCollidable(false);
		armorStand.setMarker(true);

		nameStands = NameLabelUtils.createTextDisplay(new String[] {
				ChatColor.AQUA + "Teleporter",
				ChatColor.GRAY + destinationName
		}, armorStand.getLocation().clone().add(0, 1.75, 0));
	}
	
	public void remove() {
		if(armorStand != null) armorStand.remove();
		for(ArmorStand nameStand : nameStands) {
			nameStand.remove();
		}
		this.armorStand = null;
	}
	
	public void tick() {
		if(armorStand == null || armorStand.isDead()) return;
		// Particles
		Location armorStandLocation = armorStand.getLocation();
		armorStand.getWorld().spawnParticle(Particle.SPELL, armorStandLocation.getX(), armorStandLocation.getY()+0.1, armorStandLocation.getZ(), 5, rand.nextDouble()/2d, rand.nextDouble()/2d, rand.nextDouble()/2d, 5, null);		
		armorStand.getWorld().spawnParticle(Particle.SPELL, armorStandLocation.getX(), armorStandLocation.getY()+0.9, armorStandLocation.getZ(), 5, rand.nextDouble()/2d, rand.nextDouble()/2d, rand.nextDouble()/2d, 5, null);		
		// Player checks
		for(Player p : armorStand.getWorld().getPlayers()) {
			double px = p.getLocation().getX();
			double py = p.getLocation().getY();
			double pz = p.getLocation().getZ();
			if(Math.abs(px - armorStandLocation.getX()) <= 0.5 &&
					Math.abs(py - armorStandLocation.getY()) <= 0.5 &&
					Math.abs(pz - armorStandLocation.getZ()) <= 0.5) {
				teleport(p);
			}
		}
	}
	
	/**
	 * Teleports the player to the destination
	 * @param p
	 */
	public void teleport(Player p) {
		
		PlayerUseTeleporterEvent event = new PlayerUseTeleporterEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled()) return;
		
		Location destination = event.getDestination();
		
		p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.25f);
		destination.getWorld().spawnParticle(Particle.SPELL, destination.getX(), destination.getY(), destination.getZ(), 5, rand.nextDouble()/2d, rand.nextDouble()/2d, rand.nextDouble()/2d, 5, null);		
		
		p.teleport(destination);
		
	}

	public Location getDestination() {
		return destination;
	}
	
	public String getID() {
		return id;
	}
	
}