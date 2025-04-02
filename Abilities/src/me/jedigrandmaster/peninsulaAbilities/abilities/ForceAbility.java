package me.jedigrandmaster.peninsulaAbilities.abilities;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.ItemManager;
import net.md_5.bungee.api.ChatColor;

public class ForceAbility extends Ability{
	private PeninsulaAbilities plugin;
	public ForceAbility(PeninsulaAbilities plugin) {
		super("Force");
		this.plugin = plugin;
		
		this.entities = new HashMap<String, Entity>();
		this.distance = new HashMap<String, Integer>();
		this.players = new HashMap<Entity, String>();
		
		this.hasUsedForceDuringTick = new HashMap<String, Boolean>();
		
		this.thrownBlocks = new HashMap<FallingBlock, Player>();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
			public void run() {
				handleThrownBlocks();
			}
		}, 0, 1);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
			public void run() {
				updateEntities();
			}
		}, 0, 20);
	}
	
	private static final double RAD_PER_DEGREE = Math.PI/180;
	
	private static Location calcPosOffset(Location loc, double distance) {
		double x = loc.getX() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.sin(-loc.getYaw() * RAD_PER_DEGREE);
		double z = loc.getZ() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.cos(-loc.getYaw() * RAD_PER_DEGREE);
		double y = loc.getY() + distance * Math.sin(-loc.getPitch() * RAD_PER_DEGREE) + 1.5; //added 1.5 so it's casted from the player's head
		return new Location(loc.getWorld(), x, y, z);
	}
	
	private final int range = 15;
	
	private HashMap<String, Entity> entities;
	private HashMap<String, Integer> distance;
	private HashMap<Entity, String> players;
	
	private HashMap<FallingBlock, Player> thrownBlocks;
	
	private HashMap<String, Boolean> hasUsedForceDuringTick;
	
	public void restoreGravity() { //should only be called on disable of the plugin
		for(Entity e : this.entities.values()) {
			e.setGravity(true);
			//e.setGlowing(false); //GLOWING marker
		}
	}
	
	public void updateEntities() {
		//this.plugin.getLogger().info("" + this.players.values()); //DEBUG
		//this.plugin.getLogger().info("" + this.entities.values()); //DEBUG
		Collection<String> players = this.players.values();
		
		
		for(String p : players) {
		
			Player player = Bukkit.getPlayer(p);
			Entity entity = this.entities.get(p);
		
			Location loc = calcPosOffset(player.getLocation(), this.distance.get(p));
			loc.setYaw(entity.getLocation().getYaw());
			loc.setPitch(entity.getLocation().getPitch());
		
			entity.teleport(loc);
			
			if(entity instanceof FallingBlock) {
				entity.setTicksLived(1); //stop it from disappearing
			}
			
			if(entity.isDead()) {
				//this.plugin.getLogger().info("Removed dead entity from " + player.getName());
				this.players.remove(this.entities.get(player.getName())); //PLAYER marker
				this.entities.remove(player.getName());
				this.distance.remove(player.getName());
			}
		}
	}
	
	private void handleThrownBlocks() {
		Set<FallingBlock> set = thrownBlocks.keySet();
		
		ArrayList<FallingBlock> blocks = new ArrayList<FallingBlock>(); 
		
		for(FallingBlock f : set) {
			blocks.add(f);
		}
		
		for(FallingBlock f : blocks) {
			if(!f.isValid()) {
				thrownBlocks.remove(f);
				continue;
			}
			Vector v = f.getVelocity();
			if(v.getX() == 0 && v.getY() == 0 && v.getZ() == 0) {
				thrownBlocks.remove(f);
				continue;
			}
			try {
				Location block = f.getLocation();
				Vector motion = f.getVelocity();
				
				HashMap<Entity, Boolean> hasBeenDamaged = new HashMap<Entity, Boolean>();
				
				f.getWorld().rayTraceEntities(block, motion, 2, 1, new Predicate<Entity>() { 
					@Override
					public boolean test(Entity e) {
						if(hasBeenDamaged.get(e) != null) {
							return false;
						}
						
						if(!(e instanceof Damageable)) return false;
						
						if(e == thrownBlocks.get(f)) return false;
						
						Damageable d = (Damageable) e;
						
						if(thrownBlocks.get(f).isSneaking()) {
							thrownBlocks.get(f).setSneaking(false);
							d.damage(4, thrownBlocks.get(f));
							thrownBlocks.get(f).setSneaking(true);
						} else {
							d.damage(4, thrownBlocks.get(f));
						}
						
						
						
						
						hasBeenDamaged.put(e, true);
						
						return false;
					}
				});
			} catch (IllegalArgumentException e) {}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!this.useAbility(e.getPlayer())) return;
		
		if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
			Player p = e.getPlayer();
			
			if(p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.CREATIVE) return;
			
			if(this.hasUsedForceDuringTick.get(p.getName()) != null) return;
			
			this.hasUsedForceDuringTick.put(p.getName(), true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
				public void run() {
					ForceAbility.this.hasUsedForceDuringTick.remove(p.getName());
				}
			});
			
			if(this.entities.get(p.getName()) != null){
				Entity entity = this.entities.get(p.getName());
				
				this.players.remove(this.entities.get(p.getName())); //PLAYERS marker
				this.entities.remove(p.getName());
				this.distance.remove(p.getName());
			
				entity.setGravity(true);
				//entity.setGlowing(false); //GLOWING marker
				
				//summon fallingblock marker
				
				if(p.isSneaking()){
					Location loc = calcPosOffset(p.getLocation(), 2);
					entity.setVelocity(new Vector(
						loc.getX() - p.getLocation().getX(),
						loc.getY() - (p.getLocation().getY() + 1.5),
						loc.getZ() - p.getLocation().getZ()
					));
					
					if(entity instanceof FallingBlock) {
						thrownBlocks.put((FallingBlock) entity, p);
					}
				}
				return;
			}
			
			if(!p.isSneaking()) return;
			
			e.setCancelled(true); //prevent the normal left click action
			
			int range = this.range;
			double entityStrength = 50; //default is 15, change maybe
			
			ArrayList<Entity> nearPlayer = new ArrayList<Entity>();
			
			for(Entity entity : p.getWorld().getEntities()) {
				if(Math.abs(p.getLocation().getX() - entity.getLocation().getX()) <= range) {
					if(Math.abs(p.getLocation().getY() - entity.getLocation().getY()) <= range) {
						if(Math.abs(p.getLocation().getZ() - entity.getLocation().getZ()) <= range) {
							nearPlayer.add(entity);
						}
					}
				}
			}
						
			for(int i = 0; i < range; i++) {
				
				//loop through all entities and see if any of them occupy the current block
				for(Entity entity : nearPlayer) {
					if(entity instanceof Player) continue;
					
					for(double offset = 1; offset > 0; offset -= 0.1) {
						Location loc1 = calcPosOffset(p.getLocation(), i - offset);
						Vector min = entity.getBoundingBox().getMin();
						Vector max = entity.getBoundingBox().getMax();
						
						/*
						if(entity instanceof Item || entity instanceof ExperienceOrb) { //TODO make it work with xp orbs
							min.add(new Vector(-0.5, -0.5, -0.5));
							max.add(new Vector(0.5, 0.5, 0.5));
						}
						*/
						if(entity.getBoundingBox().getVolume() < 1) {
							min.add(new Vector(-0.5, -0.5, -0.5));
							max.add(new Vector(0.5, 0.5, 0.5));
						}
						
						if(min.getX() < loc1.getX() && max.getX() > loc1.getX()) {
							if(min.getY() < loc1.getY() && max.getY() > loc1.getY()) {
								if(min.getZ() < loc1.getZ() && max.getZ() > loc1.getZ()) {
									if(this.players.get(entity) == null) {
										try {
											if(entityStrength < ((Damageable) entity).getMaxHealth()) {
												p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN + "Force" + ChatColor.WHITE + "] That entity is too heavy");
												return;
											}
										} catch (ClassCastException ex) {
											//empty catch block
										}
										
										if(entity instanceof Item || entity instanceof ExperienceOrb) {
											/*
											if(Math.sqrt(
													(
														(p.getLocation().getX() - entity.getLocation().getX()) * 
														(p.getLocation().getX() - entity.getLocation().getX())
													)
													+
													(
														(p.getLocation().getY() - entity.getLocation().getY()) * 
														(p.getLocation().getY() - entity.getLocation().getY())
													)
													+
													(
														(p.getLocation().getZ() - entity.getLocation().getZ()) * 
														(p.getLocation().getZ() - entity.getLocation().getZ())
													)
													
											) <= 3) {
												entity.setVelocity(new Vector(
													(p.getEyeLocation().getX() - entity.getLocation().getX()),
													(p.getEyeLocation().getY() - entity.getLocation().getY()),
													(p.getEyeLocation().getZ() - entity.getLocation().getZ())
												).multiply(0.25));
												return;
											}
											*/
											/*
											entity.setVelocity(new Vector(
												(p.getEyeLocation().getX() - entity.getLocation().getX()),
												(p.getEyeLocation().getY() - entity.getLocation().getY()),
												(p.getEyeLocation().getZ() - entity.getLocation().getZ())
											).multiply(0.25));
											*/
											entity.setVelocity(
												p.getEyeLocation().toVector().subtract(entity.getLocation().toVector()).multiply(0.5)
											);
											
											for(int index = 0; index < 10; index++) { //DEBUG
												int effectiveFinal = index;
												
												Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
													@Override
													public void run() {
														if(entity.isValid()) {
															entity.setVelocity(
																p.getEyeLocation().toVector().subtract(entity.getLocation().toVector()).multiply(0.5)
															);
														}
													}
													
												}, effectiveFinal);
											}
											/*
											Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

												@Override
												public void run() {
													if(entity.isValid()) {
														entity.teleport(p);
													}
												}
												
											}, 10);
											*/
											return;
										}
										
										this.entities.put(p.getName(), entity);
										this.players.put(entity, p.getName()); //PLAYERS marker
										this.distance.put(p.getName(), i);
										
										entity.setGravity(false);
										//entity.setGlowing(true); //GLOWING marker
										
										return;
									}
								}
							}
						}
					}
				}
				
				Location loc = calcPosOffset(p.getLocation(), i);//prevent lava dupe exploit
				
				if(p.getWorld().getBlockAt(loc).isLiquid()) continue;
				
				if(p.getWorld().getBlockAt(loc).getType().getHardness() == -1) {
					p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN + "Force" + ChatColor.WHITE + "] That block is too heavy");
					return;
				}
				
				if(p.getWorld().getBlockAt(loc).getType() != Material.AIR) { 
					//TODO block strength nerf
					
					/*
					if(b.getDrops(new ItemStack(Material.AIR)).isEmpty()) {
						p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN + "Force" + ChatColor.WHITE + "] That block is too heavy");
						return;
					}
					*/
					
					//p.sendMessage("DEBUG: " + p.getWorld().getBlockAt(loc).getType().getHardness()); //DEBUG
					
					FallingBlock fallingblock = p.getWorld().spawnFallingBlock(loc, p.getWorld().getBlockAt(loc).getBlockData());
					fallingblock.setGravity(false);
					//fallingblock.setGlowing(true); //GLOWING marker
					fallingblock.setDropItem(false); //not the best solution, but it prevents any possible item dupe glitches
					
					p.getWorld().getBlockAt(loc).setType(Material.AIR);
					
					this.entities.put(p.getName(), fallingblock);
					
					this.players.put(fallingblock, p.getName()); //PLAYER marker
					
					this.distance.put(p.getName(), i);
					
					
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPunch(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		Player p = (Player) e.getDamager();
		Entity entity = e.getEntity();
		
		if(e.getCause() != DamageCause.ENTITY_ATTACK) return;
		
		if(this.players.get(entity) != null) {
			return;
		}
		if(entity instanceof Player) return;
		
		if(!this.useAbility(p)) return;
		
		if(this.hasUsedForceDuringTick.get(p.getName()) != null) return;
		
		this.hasUsedForceDuringTick.put(p.getName(), true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				ForceAbility.this.hasUsedForceDuringTick.remove(p.getName());
			}
		});
		
		if(p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.CREATIVE) return;
		
		int range = this.range;
		double entityStrength = 50;
		
		if(this.entities.get(p.getName()) != null) {
			Entity ent = this.entities.get(p.getName());
			
			this.players.remove(this.entities.get(p.getName())); //PLAYERS marker
			this.entities.remove(p.getName());
			this.distance.remove(p.getName());
		
			ent.setGravity(true);
			//ent.setGlowing(false); //GLOWING marker
			
			//summon fallingBlock marker
			if(p.isSneaking()){
				Location loc = calcPosOffset(p.getLocation(), 2);
				ent.setVelocity(new Vector(
					loc.getX() - p.getLocation().getX(),
					loc.getY() - (p.getLocation().getY() + 1.5),
					loc.getZ() - p.getLocation().getZ()
				));
				
				if(entity instanceof FallingBlock) {
					thrownBlocks.put((FallingBlock) entity, p);
				}
			}
			return;
		}
		
		if(this.entities.get(p.getName()) != null) return;
		if(!p.isSneaking()) return;
		
		
		e.setCancelled(true); //prevent the normal left click action
		
		try {
			if(((Damageable) entity).getMaxHealth() > entityStrength) {
				p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN + "Force" + ChatColor.WHITE + "] That entity is too heavy");
				
				return;
			}
		} catch (ClassCastException ex) {
			//empty catch block
		}
		
		this.entities.put(p.getName(), entity);
		this.players.put(entity, p.getName()); //PLAYERS marker
		
		double xOffset = p.getLocation().getX() - entity.getLocation().getX();
		double yOffset = p.getLocation().getY() - entity.getLocation().getY();
		double zOffset = p.getLocation().getZ() - entity.getLocation().getZ();
		
		double distance = Math.sqrt(Math.pow(xOffset, 2) + Math.pow(yOffset, 2) + Math.pow(zOffset, 2));
		
		this.distance.put(p.getName(), (int) distance);
		
		entity.setGravity(false);
		//entity.setGlowing(true); //GLOWING marker
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Entity entity = this.entities.get(e.getPlayer().getName());
		if(entity == null) return;
		
		Location loc = calcPosOffset(e.getPlayer().getLocation(), distance.get(e.getPlayer().getName()));
		
		if(entity instanceof FallingBlock) {
			entity.remove();
			
			FallingBlock fallingblock = e.getPlayer().getWorld().spawnFallingBlock(loc, ((FallingBlock) entity).getBlockData());
			fallingblock.setGravity(false);
			//fallingblock.setGlowing(true);//GLOWING marker
			fallingblock.setDropItem(false); //not the best solution, but it prevents any possible item dupe glitches
			
			this.entities.put(e.getPlayer().getName(), fallingblock);
			this.players.remove(entity);
			this.players.put(fallingblock, e.getPlayer().getName()); //PLAYERS marker
		} else {
			entity.teleport(loc);
		}
		
		if(e.getPlayer().getWorld().getBlockAt(loc).getType() != Material.AIR && e.getPlayer().getWorld().getBlockAt(loc).getType().isTransparent() != true && e.getPlayer().getWorld().getBlockAt(loc).isLiquid() != true) { //!= true instead of false because it's deprecated, it may return null or something. This is probably not the case but better safe than sorry. This is the longest one line comment i've ever written
			this.entities.get(e.getPlayer().getName()).teleport(new Location(e.getPlayer().getWorld(), loc.getX(), loc.getY() + 2, loc.getZ()));
			this.entities.get(e.getPlayer().getName()).setGravity(true);
			//this.entities.get(e.getPlayer().getName()).setGlowing(false); //GLOWING marker
			
			//this.players.remove(entity); //PLAYER marker
			this.players.remove(this.entities.get(e.getPlayer().getName())); //PLAYER marker
			this.entities.remove(e.getPlayer().getName()); //prevent objects from phasing through stuff
			this.distance.remove(e.getPlayer().getName());
			
			//summon fallingblock marker
		}
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if(this.entities.get(e.getPlayer().getName()) != null) {
			this.entities.get(e.getPlayer().getName()).setGravity(true);
			//this.entities.get(e.getPlayer().getName()).setGlowing(false); //GLOWING marker
			this.players.remove(this.entities.get(e.getPlayer().getName())); //PLAYER marker
			this.entities.remove(e.getPlayer().getName());
			this.distance.remove(e.getPlayer().getName());
			
			//summon fallingblock marker
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(this.entities.get(e.getEntity().getName()) != null) {
			this.entities.get(e.getEntity().getName()).setGravity(true);
			//this.entities.get(e.getEntity().getName()).setGlowing(false); //GLOWING marker
			this.players.remove(this.entities.get(e.getEntity().getName())); //PLAYER marker
			this.entities.remove(e.getEntity().getName());
			this.distance.remove(e.getEntity().getName());
			
			//summon fallingblock marker
		}
	}
	
	
	private HashMap<Material, Material> handles = new HashMap<Material, Material>(){{
		put(Material.BAT_SPAWN_EGG, Material.PIG_SPAWN_EGG);
		put(Material.COW_SPAWN_EGG, Material.CHICKEN_SPAWN_EGG);
		put(Material.COD_SPAWN_EGG, Material.SALMON_SPAWN_EGG);
		put(Material.SHEEP_SPAWN_EGG, Material.RABBIT_SPAWN_EGG);
		put(Material.SKELETON_SPAWN_EGG, Material.ZOMBIE_SPAWN_EGG);
		put(Material.CREEPER_SPAWN_EGG, Material.SPIDER_SPAWN_EGG);
		put(Material.MULE_SPAWN_EGG, Material.SQUID_SPAWN_EGG);
		put(Material.HUSK_SPAWN_EGG, Material.STRAY_SPAWN_EGG);
	}};
	private HashMap<Material, Material> sabers = new HashMap<Material, Material>(){{
		put(Material.PIG_SPAWN_EGG, Material.BAT_SPAWN_EGG);
		put(Material.CHICKEN_SPAWN_EGG, Material.COW_SPAWN_EGG);
		put(Material.SALMON_SPAWN_EGG, Material.COD_SPAWN_EGG);
		put(Material.RABBIT_SPAWN_EGG, Material.SHEEP_SPAWN_EGG);
		put(Material.ZOMBIE_SPAWN_EGG, Material.SKELETON_SPAWN_EGG);
		put(Material.SPIDER_SPAWN_EGG, Material.CREEPER_SPAWN_EGG);
		put(Material.SQUID_SPAWN_EGG, Material.MULE_SPAWN_EGG);
		put(Material.STRAY_SPAWN_EGG, Material.HUSK_SPAWN_EGG);
	}};
	
	private ItemManager itemManager = new ItemManager(new Predicate<ItemStack>() {
		public boolean test(ItemStack item) {
			return sabers.get(item.getType()) != null 
					|| handles.get(item.getType()) != null; 
		}
	});
	
	@EventHandler
	public void keepLightsabers(PlayerDeathEvent e) {
		Player player = e.getEntity();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onDeath(e);
	}
	
	@EventHandler
	public void keepLightsabersRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onRespawn(e);
	}
}

