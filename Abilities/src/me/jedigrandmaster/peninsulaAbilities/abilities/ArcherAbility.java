package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.ItemManager;

public class ArcherAbility extends Ability {
	private PeninsulaAbilities plugin;
	public ArcherAbility(PeninsulaAbilities plugin) {
		super("Archer");
		this.plugin = plugin;
		
		this.arrowTypeKey = new NamespacedKey(this.plugin, "ArrowType");
		
		try {
			declareRecipes();
		} catch (IllegalStateException e) {
			//recipes have already been declared
		}
	}
	
	private void declareRecipes() {
		ShapedRecipe lightning = new ShapedRecipe(
				new NamespacedKey(this.plugin, "lightning_arrow"), 
				makeArrow("Lightning Arrow", 1));
		lightning.shape("ccc", "cac", "ccc");
		lightning.setIngredient('c', Material.COPPER_INGOT);
		lightning.setIngredient('a', Material.ARROW);
		//lightning.setIngredient('d', Material.DIAMOND);
		
		Bukkit.addRecipe(lightning);
		
		ShapelessRecipe multishot = new ShapelessRecipe(
				new NamespacedKey(this.plugin, "multishot_arrow"),
				makeArrow("Multishot Arrow", 3));
		
		multishot.addIngredient(Material.ARROW);
		multishot.addIngredient(Material.ARROW);
		multishot.addIngredient(Material.ARROW);
		
		Bukkit.addRecipe(multishot);
		
		
	}
	
	private ItemStack makeArrow(String name, int arrowType) {
		ItemStack arrow = new ItemStack(Material.ARROW);
		ItemMeta meta = arrow.getItemMeta();
		meta.getPersistentDataContainer().set(this.arrowTypeKey, 
			PersistentDataType.INTEGER, arrowType);
		meta.setDisplayName(name);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.ARROW_DAMAGE, 0, true);
		arrow.setItemMeta(meta);
		
		return arrow;
	}
	
	@EventHandler
	public void onCraft(CraftItemEvent e) {
		ItemStack item = e.getInventory().getResult();
		PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
		if(container.getOrDefault(this.arrowTypeKey, 
				PersistentDataType.INTEGER, 0).intValue() == 0) return;
		
		Player player = (Player) e.getWhoClicked();
		
		if(!this.useAbility(player)) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
		}
	}
	
	private NamespacedKey arrowTypeKey;
	
	@EventHandler
	public void onShoot(EntityShootBowEvent e) {
		ItemStack consumable = e.getConsumable();
		Entity proj = e.getProjectile();
		Entity ent = e.getEntity();
		
		if(consumable.getItemMeta() == null) return;
		
		int arrowType = consumable.getItemMeta()
				.getPersistentDataContainer()
				.getOrDefault(this.arrowTypeKey, 
					PersistentDataType.INTEGER, 0).intValue();
		
		if(arrowType == 0) return;
		
		proj.getPersistentDataContainer().set(this.arrowTypeKey, 
				PersistentDataType.INTEGER, arrowType);
		
		if(e.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)) {
			consumable.setAmount(consumable.getAmount() - 1);
		}
		
		if(arrowType == 2) {
			proj.addPassenger(ent);
		}
		
		if(arrowType == 3) {
			if(!(ent instanceof ProjectileSource)) return;
			ProjectileSource shooter = (ProjectileSource) ent;
			
			shooter.launchProjectile(Arrow.class, 
				proj.getVelocity().rotateAroundY(Math.PI * 0.25 * 0.25))
				.setPickupStatus(PickupStatus.DISALLOWED);
			
			shooter.launchProjectile(Arrow.class, 
				proj.getVelocity().rotateAroundY(Math.PI * 0.25 * 0.25 * -1))
				.setPickupStatus(PickupStatus.DISALLOWED);
		}
		
		if(arrowType == 4) {
			ent.getWorld().playSound(ent.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1, 1);
			
			Vector newVel = ent.getLocation().getDirection().multiply(2);
			Vector currentVel = ent.getVelocity();
			Vector addedVel = new Vector();
			
			if(getSign(currentVel.getX()) != getSign(newVel.getX())) addedVel.setX(currentVel.getX());
			if(getSign(currentVel.getY()) != getSign(newVel.getY())) addedVel.setY(currentVel.getY());
			if(getSign(currentVel.getZ()) != getSign(newVel.getZ())) addedVel.setZ(currentVel.getZ());
			//if(currentVel.getY() < 0) newVel.add(new Vector(0, currentVel.getY(), 0));
			
			newVel = newVel.add(addedVel);
			ent.setVelocity(newVel);
		}
	}
	
	private int getSign(double val) {
		if(val < 0) return -1;
		if(val > 0) return 1;
		return 0;
	}
	
	@EventHandler
	public void onImpact(ProjectileHitEvent e) {
		Entity projectile = e.getEntity();
		
		int arrowType = projectile.getPersistentDataContainer()
				.getOrDefault(this.arrowTypeKey, 
					PersistentDataType.INTEGER, 0).intValue();
		
		if(arrowType == 0) return;
		
		Location loc;
		if(e.getHitEntity() != null) {
			loc = e.getHitEntity().getLocation();
		} else {
			loc = e.getHitBlock().getLocation();
		}
		
		if(arrowType == 1) {
			projectile.getWorld().strikeLightning(projectile.getLocation());
		}
		
		if(arrowType == 5) {
			FallingBlock anvil = projectile.getWorld().spawnFallingBlock(
				loc.add(0, 2, 0),
				Material.ANVIL, (byte) 0);
			
			anvil.setHurtEntities(true);
		}
	}
	
	private ItemManager itemManager = new ItemManager((ItemStack item) -> {
		return item.getType() == Material.BOW && item.getItemMeta().hasCustomModelData();
	});
	@EventHandler
	public void keepItems(PlayerDeathEvent e) {
		Player player = e.getEntity();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onDeath(e);
	}
	
	@EventHandler
	public void keepItemsRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if(!this.useAbility(player)) return;
		
		this.itemManager.onRespawn(e);
	}
}
