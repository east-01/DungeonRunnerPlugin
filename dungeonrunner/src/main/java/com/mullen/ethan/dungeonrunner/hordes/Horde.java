package com.mullen.ethan.dungeonrunner.hordes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Horde {

	private Random rand;
	
	private String name;
	
	private HashMap<String, Integer> maxAmt;
	private HashMap<String, Integer> minAmt;
	private HashMap<String, Integer> weights; 
	
	// A summation of the minAmt and maxAmt hash maps
	private int minimumMobCount, maximumMobCount;
	
	public Horde(String name) {
		this.rand = new Random();
		
		this.name = name;
		this.maxAmt = new HashMap<String, Integer>();
		this.minAmt = new HashMap<String, Integer>();
		this.weights = new HashMap<String, Integer>();
	}
	
	/**
	 * Add a mob to this horde, specify the maximum and minimum amounts of mobs who can spawn
	 * @param id A mob id, can either be a custom mob id or an EntityType
	 * @param min The minimum amount of mobs that can spawn
	 * @param max The maximum amount of mobs that can spawn
	 */
	public void addMob(String id, int weight, int min, int max) {		
		maxAmt.put(id, max);
		minAmt.put(id, min);
		weights.put(id, weight);

		minimumMobCount = 0;
		maximumMobCount = 0;
		for(String idString : maxAmt.keySet()) {
			minimumMobCount += minAmt.get(idString);
			maximumMobCount += maxAmt.get(idString);
		}
	}
		
	public HashMap<String, Integer> generateRandomCountMap() {
		int mobsToSpawn = minimumMobCount + rand.nextInt(maximumMobCount - minimumMobCount);		
		return generateRandomCountMap(mobsToSpawn);
	}
	
	public HashMap<String, Integer> generateRandomCountMap(int mobsToSpawn) {
		
		// The counts of each mob being populated
		HashMap<String, Integer> counts = new HashMap<String, Integer>(); 
		List<String> eligibleIDs = new ArrayList<String>();
		eligibleIDs.addAll(maxAmt.keySet());
		
		// Complete minimum requirements
		for(String id : minAmt.keySet()) {
			int minCount = minAmt.get(id);
			if(minCount == 0) continue;
			// If the minimum amount is the same as the maximum amount, we need to remove it from the eligibleIDs
			if(minCount == maxAmt.get(id)) {
				eligibleIDs.remove(id);
			}
			counts.put(id, minCount);
			mobsToSpawn -= minCount;
		}
		
		// Spawn other mobs that aren't apart of the minimum requirements
		for(int i = 0; i < mobsToSpawn; i++) {
			if(eligibleIDs.size() == 0) break;
			
			// Get the total weight
			int totalWeight = 0;
			for(String id : eligibleIDs) {
				totalWeight += weights.get(id);
			}
			
			// Pick the roll
			int roll = rand.nextInt(totalWeight);
			String rolledID = null;
			// The weight that is added up as a part of checking the roll process
			int currentWeight = 0;
			for(String id : eligibleIDs) {
				currentWeight += weights.get(id);
				if(currentWeight >= roll) {
					rolledID = id;
					break;
				}
			}
			if(rolledID == null) {
				rolledID = eligibleIDs.get(rand.nextInt(eligibleIDs.size()));
			}
			
			// Increment the count of this mob
			if(counts.containsKey(rolledID)) {
				counts.put(rolledID, counts.get(rolledID) + 1);
			} else {
				counts.put(rolledID, 1);
			}
			
			// If we've reached the maximum amount, remove it from eligibility
			if(counts.get(rolledID) >= maxAmt.get(rolledID)) {
				eligibleIDs.remove(rolledID);
			}
			
		}

		return counts;
		
	}
	
	public String getName() {
		return name;
	}
	
}
