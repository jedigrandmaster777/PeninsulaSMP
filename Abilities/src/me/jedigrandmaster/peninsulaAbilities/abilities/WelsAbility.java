package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.level.World;

public class WelsAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	public WelsAbility(PeninsulaAbilities plugin) {
		super("Wels");
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent e) {
		if(!(e.getTarget() instanceof Player)) return;
		Player player = (Player) e.getTarget();
		if(!this.useAbility(player)) return;
		if(e.getReason() != TargetReason.CLOSEST_PLAYER
			&& e.getReason() != TargetReason.RANDOM_TARGET) return;
		
		if(e.getEntityType() != EntityType.ENDERMAN
			&& e.getEntityType() != EntityType.ENDERMITE
			&& e.getEntityType() != EntityType.SHULKER
			&& e.getEntityType() != EntityType.PHANTOM) return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if(e.getCause() != DamageCause.FALL) return;
		
		if(!this.useAbility(player)) return;
		
		e.setDamage(e.getDamage()/10);
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(e.getAction() != Action.RIGHT_CLICK_AIR) return;
			
		ItemStack item = player.getInventory().getItemInMainHand();
		if(item == null) return;
		
		if(item.getType() != Material.PAPER) return;
		
		item.setAmount(item.getAmount() - 1);
		
		
		CraftHumanEntity nmsPlayer = (CraftHumanEntity) player;
		World world =  ((CraftWorld) player.getWorld()).getHandle(); //NMS world
		ItemStack fireworkItemStack = new ItemStack(Material.FIREWORK_ROCKET);
		
		EntityFireworks firework = new EntityFireworks(
				world, 
				CraftItemStack.asNMSCopy(fireworkItemStack), 
				nmsPlayer.getHandle());
		
		world.addFreshEntity(firework, SpawnReason.CUSTOM);
		
		/*
		Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		CraftHumanEntity nmsPlayer = (CraftHumanEntity) player;
		*/
		/*
		CraftFirework nmsFirework = (CraftFirework) firework;
		EntityFireworks handle = nmsFirework.getHandle();
		
		
		Field field = handle.getClass().getField("ao");
		field.setAccessible(true);
		field.set(handle, ((CraftPlayer) player).getHandle());
		*/
		/*
		for(Field field : handle.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				field.set(handle, ((CraftPlayer) player).getHandle());
				this.plugin.getLogger().info(field.getName());
				this.plugin.getLogger().info(field.getType().toString());
			} catch (Exception ex) {}
		}
		*/
	}
}
