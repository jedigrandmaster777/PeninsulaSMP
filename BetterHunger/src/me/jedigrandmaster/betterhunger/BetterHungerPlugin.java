package me.jedigrandmaster.betterhunger;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class BetterHungerPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::doRegen, 0, 10 * 20);
	}
	
	private void removeHunger(Player player, int amount) {
		if(player.getFoodLevel() < 1) return;
		player.setFoodLevel(player.getFoodLevel() - amount);
	}
	
	@EventHandler
	public void onHunger(FoodLevelChangeEvent e) {
		if(e.getEntity().hasPotionEffect(PotionEffectType.HUNGER)) return;
		if(e.getItem() != null) return;
		if(e.getFoodLevel() > e.getEntity().getFoodLevel()) return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onStarve(EntityDamageEvent e) {
		if(e.getCause() == DamageCause.STARVATION) e.setCancelled(true);
	}
	
	@EventHandler
	public void onRegen(EntityRegainHealthEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(e.getRegainReason() != RegainReason.SATIATED) return;
		Player player = (Player) e.getEntity();
		removeHunger(player, 1);
	}
	
	private void doRegen() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			
			if(player.getHealth() >= maxHealth) continue;
			if(player.getFoodLevel() < 1) continue;
			
			removeHunger(player, 1);
			player.setHealth(Math.min(player.getHealth() + 1, maxHealth));
		}
	}
	
	private Random rand = new Random();
	@EventHandler
	public void onSprint(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(!player.isSprinting()) return;
		if(this.rand.nextInt(200) == 10) removeHunger(player, 1);
	}
}
