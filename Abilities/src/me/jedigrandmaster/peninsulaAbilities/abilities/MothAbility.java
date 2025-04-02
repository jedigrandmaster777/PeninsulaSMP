package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;

public class MothAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	public MothAbility(PeninsulaAbilities plugin) {
		super("Moth");
		this.plugin = plugin;
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::hunger, 0, 4 * 20);
	}
	
	private void hunger() {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			if(!this.useAbility(player)) return;
			if(!player.isFlying()) return;
			
			if(player.getFoodLevel() > 0) player.setFoodLevel(player.getFoodLevel() - 1);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		player.setAllowFlight(true);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		player.setAllowFlight(true);
	}
	
	
}
