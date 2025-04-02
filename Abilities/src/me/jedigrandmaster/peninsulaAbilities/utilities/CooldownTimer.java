package me.jedigrandmaster.peninsulaAbilities.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CooldownTimer extends BukkitRunnable {
	private JavaPlugin plugin;
	
	private HashMap<Player, Integer> cooldown;
	public CooldownTimer(JavaPlugin plugin) {
		this.cooldown = new HashMap<Player, Integer>();
		super.runTaskTimer(plugin, 0, 20); //once per second
	}
	
	public void run() {
		Set<Player> onCooldown = new HashSet<Player>(this.cooldown.keySet());
		for(Player player : onCooldown) {
			int time = this.cooldown.get(player);
			time--;
			if(time == 0) {
				this.cooldown.remove(player);
			} else {
				this.cooldown.put(player, time);
			}
		}
	}
	
	public void setCooldown(Player player, int time) {
		this.cooldown.put(player, time);
	}
	
	public int getCooldown(Player player) {
		Integer cooldown = this.cooldown.get(player);
		if(cooldown == null) return -1;
		return cooldown;
	}
}
