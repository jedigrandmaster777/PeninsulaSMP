package me.jedigrandmaster.peninsulaAbilities.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.CooldownTimer;
import me.jedigrandmaster.peninsulaAbilities.utilities.MindControlMap;
import net.minecraft.world.entity.EntityInsentient;

public class CreeperAbility extends Ability {
	private PeninsulaAbilities plugin;
	private CooldownTimer timer;
	private MindControlMap map;
	
	public CreeperAbility(PeninsulaAbilities plugin) {
		super("Creeper");
		
		this.plugin = plugin;
		
		this.map = new MindControlMap();
		this.timer = new CooldownTimer(plugin);
	}
	
	@EventHandler
	public void onAbilityUse(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(e.getAction() != Action.LEFT_CLICK_BLOCK 
			&& e.getAction() != Action.LEFT_CLICK_AIR) return;
		if(!player.isSneaking()) return;
		
		int cooldown = this.timer.getCooldown(player);
		if(cooldown != -1) {
			player.sendMessage(ChatColor.RED + "That ability is on cooldown for " 
					+ cooldown
					+ " more seconds");
			
			return;
		}
		this.timer.setCooldown(player, 5);
		
		Creeper creeper = (Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER);
		creeper.setPersistent(false);
		
		creeper.setMaxFuseTicks(15);
		creeper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(
			new AttributeModifier("Ability", 1.1, Operation.ADD_SCALAR));
		//creeper.setExplosionRadius(6);
		
		
		this.map.setPlayerOfEntity(creeper, player);
		creeper.setVelocity(player.getLocation().getDirection().multiply(1.2));
		
		AtomicInteger taskID = new AtomicInteger();
		
		taskID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if(!creeper.isValid()) {
				Bukkit.getScheduler().cancelTask(taskID.get());
				return;
			}
			if(!creeper.isOnGround()) return;
			Bukkit.getScheduler().cancelTask(taskID.get());
			
			CreeperAbility.this.creeperTarget(creeper);
		}, 0, 5));
	}
	
	private void creeperTarget(Creeper creeper) {
		List<Entity> near = creeper.getNearbyEntities(10, 10, 10);
		
		LivingEntity closest = null;
		
		for(Entity ent : near) {
			if(!(ent instanceof LivingEntity)) continue;
			if(this.map.getPlayerOfEntity(creeper) == ent) continue;
			if(ent instanceof Creature &&
				this.map.getPlayerOfEntity(creeper) == this.map.getPlayerOfEntity((Creature) ent)) continue;
			
			if(closest == null) {
				closest = (LivingEntity) ent;
				continue;
			}
			
			double closestDist = closest.getLocation().distance(creeper.getLocation());
			double currDist = ent.getLocation().distance(creeper.getLocation());
			
			if(currDist < closestDist) closest = (LivingEntity) ent;
		}
		
		if(closest == null) return;
		
		creeper.setTarget(closest);
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent e) {
		if(!(e.getTarget() instanceof Player)) return;
		if(!(e.getEntity() instanceof Creature)) return;
		
		Player p = (Player) e.getTarget();
		Creature c = (Creature) e.getEntity();
		
		if(map.getPlayerOfEntity(c) == p) e.setCancelled(true);
	}
	
	@EventHandler 
	public void onDeath(EntityDeathEvent e) {
		if(!(e.getEntity() instanceof Creature)) return;
		
		Creature c = (Creature) e.getEntity();
		if(map.getPlayerOfEntity(c) == null) return;
		
		e.getDrops().clear();
		
		map.removeEntity(c);
	}
	
	@EventHandler
	public void onCreeperAttack(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Creeper)) return;
		if(!(e.getEntity() instanceof Player)) return;
		
		Player player = (Player) e.getEntity();
		Creeper creeper = (Creeper) e.getDamager();
		
		if(this.map.getPlayerOfEntity(creeper) == player) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent e) {
		
		if(!(e.getDamager() instanceof Player)) return;
		if(!(e.getEntity() instanceof Creature)) return;
		
		Player p = (Player) e.getDamager();
		
		Creature r = (Creature) e.getEntity();
		
		if(map.getListOfPlayer(p) == null) return;
		
		ArrayList<Creature> a = new ArrayList<Creature>(map.getListOfPlayer(p));
		
		for(int i = 0; i < a.size(); i++) {
			if(a.get(i).isDead()) {
				map.removeEntity(a.get(i));
				continue;
			}
			
			a.get(i).setTarget((LivingEntity) e.getEntity());
			//e.getEntity().getWorld().spawnParticle(Particle.REDSTONE, e.getEntity().getLocation().add(0, 1, 0), 50, 0.25, 0.25, 0.25, new Particle.DustOptions(Color.RED, 1));
		}
	}
}
