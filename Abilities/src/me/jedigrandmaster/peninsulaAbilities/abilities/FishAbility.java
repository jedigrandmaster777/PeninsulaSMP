package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;

public class FishAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	public FishAbility(PeninsulaAbilities plugin) {
		super("Fish");
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		if(player.isSwimming()) {
			Vector newVel = player.getLocation().getDirection().multiply(0.4);
			if(newVel.length() > player.getVelocity().length()) {
				player.setVelocity(newVel);
			}
		}
	}
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(!this.useAbility(player)) return;
		
		if(e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
			e.setCancelled(true);
		}
	}
	
	private int getSign(double val) {
		if(val < 0) return -1;
		if(val > 0) return 1;
		return 0;
	}
	
	@EventHandler
	public void onAbilityUse(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(e.getAction() != Action.LEFT_CLICK_BLOCK 
			&& e.getAction() != Action.LEFT_CLICK_AIR) return;
		if(!player.isSneaking()) return;
		
		Location loc = player.getLocation();
		
		//if(loc.getBlock().getType() == Material.AIR
		//	&& loc.add(0, -1, 0).getBlock().getType() == Material.AIR ) return; //no flying
		
		//VULNERABLE TO EXPLOITS
		//if(!player.isOnGround() && !player.isInWater()) return;
		
		if(player.getCooldown(Material.TRIDENT) != 0) return;
		
		player.setCooldown(Material.TRIDENT, 25);
		player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1, 1);
		
		Vector newVel = player.getLocation().getDirection().multiply(2);
		Vector currentVel = player.getVelocity();
		Vector addedVel = new Vector();
		
		if(getSign(currentVel.getX()) != getSign(newVel.getX())) addedVel.setX(currentVel.getX());
		if(getSign(currentVel.getY()) != getSign(newVel.getY())) addedVel.setY(currentVel.getY());
		if(getSign(currentVel.getZ()) != getSign(newVel.getZ())) addedVel.setZ(currentVel.getZ());
		//if(currentVel.getY() < 0) newVel.add(new Vector(0, currentVel.getY(), 0));
		
		newVel = newVel.add(addedVel);
		player.setVelocity(newVel);
		
		//((CraftPlayer) player).getHandle().r(20); // plays the spin animation for 20 ticks
		
		//EntityLiving entity = ((CraftPlayer) p).getHandle();
		
	}
}
