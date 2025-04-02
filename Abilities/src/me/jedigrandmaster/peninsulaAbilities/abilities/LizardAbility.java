package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;

public class LizardAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	public LizardAbility(PeninsulaAbilities plugin) {
		super("Lizard");
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		applyEffects(e.getPlayer());
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Bukkit.getScheduler().runTaskLater(this.plugin, ()->applyEffects(e.getPlayer()), 1);
	}
	
	
	private void applyEffects(Player player) {
		if(!this.useAbility(player)) return;
		
		player.addPotionEffect(
			new PotionEffect(PotionEffectType.JUMP, 100000, 0, true, false));
		player.addPotionEffect(
			new PotionEffect(PotionEffectType.SPEED, 100000, 1, true, false));
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(e.getCause() != DamageCause.FALL) return;
		
		if(!this.useAbility(player)) return;
		
		e.setDamage(e.getDamage() * 0.5);
	}
}
