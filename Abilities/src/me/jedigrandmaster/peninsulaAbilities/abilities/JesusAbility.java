package me.jedigrandmaster.peninsulaAbilities.abilities;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;

public class JesusAbility extends Ability {
	public JesusAbility(PeninsulaAbilities plugin) {
		super("Jesus");
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()->{
			for(Player player : this.jesusing) {
				jesus(player);
			}
		}, 0, 2);
	}
	
	private HashSet<Player> jesusing = new HashSet<Player>();
	
	private void jesus(Player player) {
		Location loc = player.getLocation();
		BlockData air = Bukkit.createBlockData(Material.AIR);
		
		for(int x = -3; x <= 3; x++) {
			for(int y = -2; y <= 2; y++) {
				for(int z = -3; z <= 3; z++) {
					Location newLoc = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
					
					if(!newLoc.getBlock().isLiquid()) return;
					player.sendBlockChange(newLoc, air);
				}
			}
		}
	}
	
	@EventHandler
	public void onAbilityUse(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(e.getAction() != Action.LEFT_CLICK_BLOCK 
			&& e.getAction() != Action.LEFT_CLICK_AIR) return;
		if(!player.isSneaking()) return;
		
		if(this.jesusing.contains(player)) this.jesusing.remove(player);
		else this.jesusing.add(player);
	} 
	/*
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(!this.jesusing.contains(player)) return;
		
		jesus(player);
	}
	*/
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(!this.jesusing.contains(player)) return;
		
		if(e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSwim(EntityToggleSwimEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(!this.jesusing.contains(player)) return;
		
		e.setCancelled(true);
	}
}
