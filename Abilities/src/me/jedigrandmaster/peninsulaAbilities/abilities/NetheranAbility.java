package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.EulerAngle;
import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.CooldownTimer;

public class NetheranAbility extends Ability {
	private PeninsulaAbilities plugin;
	
	private CooldownTimer cooldowns;
	
	public NetheranAbility(PeninsulaAbilities plugin) {
		super("Netheran");
		this.plugin = plugin;
		this.cooldowns = new CooldownTimer(plugin);
	}
	@EventHandler
	public void onAbilityUse(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(!player.isSneaking()) return;
		if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_AIR) return;
		
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), 
				player.getLocation().getDirection(), 50);
		
		if(result == null) return;
		
		int cooldown = this.cooldowns.getCooldown(player);
		if(cooldown != -1) {
			player.sendMessage(ChatColor.RED + "That ability is on cooldown for " 
					+ cooldown
					+ " more seconds");
			
			return;
		}
		this.cooldowns.setCooldown(player, 15);
		
		player.getWorld().strikeLightning(result.getHitPosition().toLocation(player.getWorld()));
	}
	
	@EventHandler
	public void onAxeThrow(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		if(e.getAction() != Action.RIGHT_CLICK_AIR) return;
		
		
		int sel = player.getInventory().getHeldItemSlot();
		ItemStack item = player.getInventory().getItem(sel);
		if(item == null) return;
		
		if(AXES.get(item.getType()) == null) return;
		
		this.handleThrow(e);
	}
	
	private static final HashMap<Material, Integer> AXES = new HashMap<Material, Integer>(){{
		put(Material.WOODEN_AXE, 6);
		put(Material.STONE_AXE, 8);
		put(Material.IRON_AXE, 8);
		put(Material.GOLDEN_AXE, 6);
		put(Material.DIAMOND_AXE, 8);
		put(Material.NETHERITE_AXE, 9);
	}};
	
	public static final double RAD_PER_DEGREE = Math.PI/180;
	
	public static Location calcPosOffset(Location loc, double distance) {
		double x = loc.getX() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.sin(-loc.getYaw() * RAD_PER_DEGREE);
		double z = loc.getZ() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.cos(-loc.getYaw() * RAD_PER_DEGREE);
		double y = loc.getY() + distance * Math.sin(-loc.getPitch() * RAD_PER_DEGREE);
		return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
	}
	
	private HashMap<Player, Boolean> thrown = new HashMap<Player, Boolean>();
	
	public void handleThrow(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if(this.thrown.get(p) != null) return;
		this.thrown.put(p, true);
		
		ItemStack saber = p.getInventory().getItemInMainHand();
		
		p.getInventory().setItemInMainHand(null);
		
		ArmorStand a = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
		a.setVisible(false);
		a.setGravity(false);
		
		a.setArms(true);
		a.getEquipment().setItemInMainHand(saber);
		a.setRightArmPose(new EulerAngle(0, 0, 0));
		
		
		a.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
		a.addEquipmentLock(EquipmentSlot.HEAD, LockType.ADDING_OR_CHANGING);
		a.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
		a.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
		a.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
		
		final float yaw = p.getLocation().getYaw();
		final float pitch = p.getLocation().getPitch();
		final double x = p.getLocation().getX();
		final double y = p.getLocation().getY();
		final double z = p.getLocation().getZ();
		
		a.getLocation().setYaw(p.getLocation().getYaw());
		a.getLocation().setPitch(p.getLocation().getPitch());
		
		
		for(int i = 0; i < 20; i++) {
			final int effectiveFinal = i;
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
				@Override
				public void run() {
					if(a.isDead()) return;
					
					a.setRightArmPose(new EulerAngle(((effectiveFinal * RAD_PER_DEGREE) * 50), 0, 0));
					a.teleport(calcPosOffset(new Location(a.getWorld(), 
						x,
						y,
						z,
						yaw,
						pitch
					), effectiveFinal));
					
					if(a.getWorld().getBlockAt(a.getEyeLocation()).getType().isSolid()) {
						endThrow(p, a, saber);
						
						return;
					}
					
					//a.getWorld().playSound(a.getLocation(), "custom.lightsaberswing", 1, 1);
					
					a.getWorld().rayTraceEntities(a.getEyeLocation(), a.getLocation().getDirection(), 1, 2, new Predicate<Entity>() {
						@Override
						public boolean test(Entity t) {
							if(t == p) return false; //so that lightsabers don't damage their users
							if(t == a) return false;
							
							if(!(t instanceof Damageable)) return false;
							if(!(t instanceof LivingEntity)) return false;
							LivingEntity l = (LivingEntity) t;
							if(l.getNoDamageTicks() != 0) return false;
							
							Damageable d = (Damageable) t;
							
							d.damage(AXES.get(saber.getType()), p);
							/*
							ItemStack mainHand = p.getInventory().getItemInMainHand();
							p.getInventory().setItemInMainHand(saber);
							p.attack(t);
							p.getInventory().setItemInMainHand(mainHand);
							*/
							
							return false;
						}
						
					});
				}
				
			}, i);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			@Override
			public void run() {
				if(a.isDead()) return;
				endThrow(p, a, saber);
			}
			
		}, 21);
	}
	
	private void endThrow(Player p, ArmorStand a, ItemStack saber) {
		this.thrown.remove(p);
		a.remove();
		
		if(p.getInventory().getItemInMainHand().getType() == Material.AIR) {
			p.getInventory().setItemInMainHand(saber);
		} else {
			HashMap<Integer, ItemStack> map = p.getInventory().addItem(saber);
			
			if(map.values().isEmpty()) return;
			
			ItemStack is = map.get(0);
			p.getWorld().dropItem(p.getLocation(), is);
		}
	}
}
