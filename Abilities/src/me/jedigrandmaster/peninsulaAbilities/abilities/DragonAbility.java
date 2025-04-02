package me.jedigrandmaster.peninsulaAbilities.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;

public class DragonAbility extends Ability {
	//TODO refactor to use CooldownTimer
	
	private PeninsulaAbilities plugin;
	
	public DragonAbility(PeninsulaAbilities plugin) {
		super("Dragon");
		this.plugin = plugin;
		
		this.dragonTime = new HashMap<Player, Integer>(); 		this.cooldown = new HashMap<Player, Integer>();
		this.fireballCooldown = new HashMap<Player, Integer>();
		
		new BukkitRunnable() {
			public void run() {
				DragonAbility.this.runDragons();
				DragonAbility.this.runCooldown();
			}
		}.runTaskTimer(this.plugin, 0, 1);
	}
	
	private static final int DRAGON_TIME = 10 * 20;
	private static final int DRAGON_COOLDOWN = 20 * 20;
	private static final int FIREBALL_COOLDOWN = 10;
	
	private HashMap<Player, Integer> dragonTime;
	private HashMap<Player, Integer> cooldown;
	
	private void runDragons() {
		Set<Player> dragons = new HashSet<Player>(this.dragonTime.keySet());
		for(Player player : dragons) {
			int time = this.dragonTime.get(player);
			time--;
			if(time == 0) {
				this.dragonTime.remove(player);
				this.cooldown.put(player, DRAGON_COOLDOWN);
			} else {
				this.dragonTime.put(player, time);
			}
			
			player.setVelocity(player.getLocation().getDirection());
			if(time % 20 == 0 || (time <= 40 && time % 10 == 0)) {
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
			}
		}
	}
	private void runCooldown() {
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
		
		Set<Player> onFireballCooldown = new HashSet<Player>(this.fireballCooldown.keySet());
		for(Player player : onFireballCooldown) {
			int time = this.fireballCooldown.get(player);
			time--;
			if(time == 0) {
				this.fireballCooldown.remove(player);
			} else {
				this.fireballCooldown.put(player, time);
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
		
		
		if(this.cooldown.get(player) != null) {
			player.sendMessage(ChatColor.RED + "That ability is on cooldown for " 
				+ this.cooldown.get(player)/20 
				+ " more seconds");
			return;
		}
		
		if(this.dragonTime.get(player) != null) {
			this.dragonTime.remove(player);
			this.cooldown.put(player, DRAGON_COOLDOWN);
			
			return;
		}
		
		this.dragonTime.put(player, DRAGON_TIME);
	}
	
	private HashMap<Player, Integer> fireballCooldown;
	@EventHandler
	public void onFireball(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(e.getAction() != Action.LEFT_CLICK_BLOCK 
			&& e.getAction() != Action.LEFT_CLICK_AIR) return;
		if(player.isSneaking()) return;
		
		if(this.dragonTime.get(player) == null) return;
		if(this.fireballCooldown.get(player) != null) return;
		
		this.fireballCooldown.put(player, FIREBALL_COOLDOWN);
		
		Vector velocity = player.getLocation().getDirection().multiply(2);
		Location spawnLoc = player.getLocation().add(velocity);
		
		player.getWorld().playSound(spawnLoc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1);
		
		Fireball fb = player.getWorld().spawn(spawnLoc, Fireball.class);
		//Fireball fb = player.launchProjectile(Fireball.class);
		fb.setVelocity(velocity);
		fb.setShooter(player);
		fb.setYield(1);
		fb.setIsIncendiary(true);
		
	}
}
