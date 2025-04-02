package me.jedigrandmaster.peninsulaAbilities;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Ability implements Listener {
	private AbilityManager manager;
	private String name;
	
	//package private
	final Ability setManager(AbilityManager manager) {
		this.manager = manager;
		return this;
	}
	final String getName() {
		return this.name;
	}
		
	//API METHODS
	protected Ability(String name) {
		this.name = name;
	}
	
	protected boolean useAbility(Player player) {
		if(this.manager == null) return false;
		return this.manager.getAbilities(player).contains(this.name);
	}
	
	
}
