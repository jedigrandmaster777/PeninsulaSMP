package me.jedigrandmaster.peninsulaAbilities.utilities;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class BlasterBolt {
	public static final double RAD_PER_DEGREE = Math.PI/180;
	public static Location calcPosOffset(Location loc, double distance) {
		double x = loc.getX() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.sin(-loc.getYaw() * RAD_PER_DEGREE);
		double z = loc.getZ() + distance * Math.cos(-loc.getPitch() * RAD_PER_DEGREE) * Math.cos(-loc.getYaw() * RAD_PER_DEGREE);
		double y = loc.getY() + distance * Math.sin(-loc.getPitch() * RAD_PER_DEGREE);
		return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
	}
	
	private Location location;
	private Player shooter;
	
	private AreaEffectCloud entity;
	
	
	public BlasterBolt(Location location, Player shooter) {
		this.location = location;
		this.shooter = shooter;
		
		
		this.entity = (AreaEffectCloud) location.getWorld().spawnEntity(new Location(location.getWorld(), 0, 0, 0), EntityType.AREA_EFFECT_CLOUD);
		this.entity.setRadiusPerTick(0);
		this.entity.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
	}
	
	public AreaEffectCloud getEntity() {
		return this.entity;
	}
	
	private boolean valid = true;
	public boolean isValid() {
		return valid;
	}
	
	public void draw() {
		this.location.getWorld().spawnParticle(Particle.CRIT, this.location, 1, 0, 0, 0, 0.1);
		
		for(double i = 0.1; i < 2; i += 0.1) {
			
			
			Location particle = calcPosOffset(this.location, i);
			
			this.location.getWorld().spawnParticle(Particle.CRIT, particle, 1, 0, 0, 0, 0.1);
			
			//this.location.getWorld().spawnParticle(Particle.CRIT, particle, 1);
			//this.location.getWorld().playEffect(particle, Effect.MOBSPAWNER_FLAMES, 0);
			
			
		}
	}
	
	public void move() {
		this.location = calcPosOffset(this.location, 1);
		
		this.entity.teleport(this.location);
		
		if(this.location.getWorld().getBlockAt(this.location).getType().isSolid()) {
			this.valid = false;
			this.entity.remove();
			return;
		}
		
		this.location = calcPosOffset(this.location, 1);
		
		this.entity.teleport(this.location);
		
		if(this.location.getWorld().getBlockAt(this.location).getType().isSolid()) {
			this.valid = false;
			this.entity.remove();
			return;
		}
	}
	
	public boolean checkImpact() {
		RayTraceResult result = this.location.getWorld().rayTraceEntities(this.location, this.location.getDirection(), 1, 0.5, new Predicate<Entity>() {

			@Override
			public boolean test(Entity e) {
				return e instanceof Damageable;
			}
			
		});
		
		if(result == null) {
			return false;
		}
		if(result.getHitEntity() == this.shooter) return false;
		
		Damageable d = (Damageable) result.getHitEntity();
		
		d.damage(6, shooter); 
	
		this.valid = false;
		this.entity.remove();
		
		return true;
	}
	
	public void invert(Player p) {
		this.shooter = p;
		
		this.location.setDirection(p.getLocation().getDirection().clone());
	}
}
