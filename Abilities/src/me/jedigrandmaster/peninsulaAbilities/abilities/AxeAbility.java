package me.jedigrandmaster.peninsulaAbilities.abilities;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.ItemManager;

public class AxeAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	public AxeAbility(PeninsulaAbilities plugin) {
		super("Axemen");
		this.plugin = plugin;
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()->waterDamage(), 0, 20);
	}
	
	private void waterDamage() {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			if(!this.useAbility(player)) continue;
			
			if(player.isInWater()) player.damage(4);
		}
	}
	
	/*
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		Player player = (Player) e.getDamager();
		Entity entity = e.getEntity();
		
		if(!this.useAbility(player)) return;
		
		entity.setVelocity(entity.getVelocity()
			.add(player.getLocation().getDirection()
				.multiply(new Vector(1, 0, 1)).multiply(2)));
	}
	*/
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(!this.useAbility(player)) return;
		
		if(e.getCause() != DamageCause.LAVA) return;
		
		e.setCancelled(true);
		player.setFireTicks(0);
	}
	
	private static final HashMap<Material, Integer> AXES = new HashMap<Material, Integer>(){{
		put(Material.WOODEN_AXE, 6);
		put(Material.STONE_AXE, 8);
		put(Material.IRON_AXE, 8);
		put(Material.GOLDEN_AXE, 6);
		put(Material.DIAMOND_AXE, 8);
		put(Material.NETHERITE_AXE, 9);
	}};
	
	private ItemManager itemManager = new ItemManager((ItemStack item) -> {
		Material m = item.getType();
		return m == Material.WOODEN_AXE 
			|| m == Material.STONE_AXE
			|| m == Material.IRON_AXE
			|| m == Material.GOLDEN_AXE
			|| m == Material.DIAMOND_AXE
			|| m == Material.NETHERITE_AXE;
	});
	
	@EventHandler
	public void keepAxes(PlayerDeathEvent e) {
		Player player = e.getEntity();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onDeath(e);
	}
	
	@EventHandler
	public void keepAxesRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onRespawn(e);
	}
}
