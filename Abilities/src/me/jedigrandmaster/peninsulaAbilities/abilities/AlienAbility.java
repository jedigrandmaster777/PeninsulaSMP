/*

	private void serializeInventory(Player player, Inventory inv) {
		try {
			File save = new File("plugins/abilities/thirdarm/" 
				+ player.getUniqueId().toString() + ".ser");
			
			save.createNewFile();
			
			FileOutputStream fileOut = new FileOutputStream(save);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			
			for(ItemStack item : inv.getContents()) {
				if(item == null) continue;
				contents.add(item.serialize());
			}
			
			out.writeObject(contents);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			player.sendMessage(ChatColor.RED 
					+ "Error while serializing inventory: " + e);
		}
	}
	
	private Inventory deserializeInventory(Player player) {
		try {
			FileInputStream fileIn = new FileInputStream("plugins/abilities/thirdarm/" 
				+ player.getUniqueId().toString() + ".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			Inventory inv = Bukkit.createInventory(player, 9, "Third Arm");
			List<Map<String, Object>> contents = (List<Map<String, Object>>) in.readObject();
			
			for(Map<String, Object> item : contents) {
				if(item == null) continue;
				inv.addItem(ItemStack.deserialize(item));
			}
			
			in.close();
			fileIn.close();
			
			return inv;
		} catch (IOException | ClassNotFoundException e) {
			player.sendMessage(ChatColor.RED 
					+ "Error while deserializing inventory: " + e);
		}
		
		return Bukkit.createInventory(player, 9, "Third Arm (error)");
	} 

*/

package me.jedigrandmaster.peninsulaAbilities.abilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import me.jedigrandmaster.peninsulaAbilities.Ability;
import me.jedigrandmaster.peninsulaAbilities.PeninsulaAbilities;
import me.jedigrandmaster.peninsulaAbilities.utilities.BlasterBolt;
import me.jedigrandmaster.peninsulaAbilities.utilities.ItemManager;

public class AlienAbility extends Ability implements CommandExecutor {
	private PeninsulaAbilities plugin;
	
	public AlienAbility(PeninsulaAbilities plugin) {
		super("Alien");
		this.plugin = plugin;
		
		this.m4Key = new NamespacedKey(plugin, "m4-rifle");
		
		plugin.getCommand("thirdarm").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		
		if(!(sender instanceof Player)) return true;
		
		if(command.getName().equalsIgnoreCase("thirdarm")) 
			return thirdArm(sender, command, label, args);
		
		return true;
	}
	private NamespacedKey m4Key;
	
	private HashMap<Player, Boolean> isOnCooldown = new HashMap<Player, Boolean>();
	
	private HashMap<AreaEffectCloud, BlasterBolt> bolts = new HashMap<AreaEffectCloud, BlasterBolt>();
	public BlasterBolt getBolt(AreaEffectCloud a) {
		return this.bolts.get(a);
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if(!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		
		Player p = e.getPlayer();
		ItemStack stack = p.getInventory().getItemInMainHand();
		
		if(!this.useAbility(p)) return;
		
		if(stack.getItemMeta() == null) return;
		
		if(stack.getItemMeta()
			.getPersistentDataContainer()
			.getOrDefault(this.m4Key, 
				PersistentDataType.INTEGER, 0).intValue() == 0) return;
		
		e.setCancelled(true);
		
		if(isOnCooldown.get(p) != null) return;
		isOnCooldown.put(p, true);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

			@Override
			public void run() {
				isOnCooldown.remove(p);
			}
			
		}, 15);
		
		p.getWorld().playSound(p.getLocation(), "entity.firework_rocket.blast_far", 0.5f, 0);
		
		BlasterBolt bolt = new BlasterBolt(p.getEyeLocation().clone(), p);
		this.bolts.put(bolt.getEntity(), bolt);
		
		bolt.move();
		
		for(int i = 1; i < 400; i++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

				@Override
				public void run() {
					if(!bolt.isValid()) return;
					
					bolt.draw();
					bolt.move();
					
					bolt.checkImpact();
					
					if(!bolt.isValid()) { //if the bolt is invalid after move is called
						bolts.remove(bolt.getEntity());
					}
				}
				
			}, i);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

			@Override
			public void run() {
				if(!bolt.isValid()) return;
				
				bolts.remove(bolt.getEntity());
				
				bolt.getEntity().remove();
			}
			
		}, 400);
		
	}
	
	private ItemManager itemManager = new ItemManager((ItemStack item) -> {
		return item.getItemMeta()
				.getPersistentDataContainer()
				.getOrDefault(m4Key, 
					PersistentDataType.INTEGER, 0).intValue() != 0;
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
	
	private HashMap<Player, Inventory> inventories = new HashMap<Player, Inventory>();
	private boolean thirdArm(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		
		if(!this.useAbility(player)) return true;
		
		Inventory inv = deserializeInventory(player);
		this.inventories.put(player, inv);
		
		
		player.openInventory(inv);
		
		return true;
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		Inventory inv = this.inventories.get(player);
		if(inv == null) return;
		this.inventories.remove(player);
		
		serializeInventory(player, inv);
	} 
	
	private void serializeInventory(Player player, Inventory inv) {
		try {
			File file = new File("plugins/abilities/thirdarm/"
					+ player.getUniqueId() + ".yml");
			file.createNewFile();
			
			FileConfiguration config = new YamlConfiguration();
			config.set("inventory", Arrays.asList(inv.getContents()));
			config.save(file);
		} catch (IOException e) {
			player.sendMessage(ChatColor.RED 
					+ "Error while serializing inventory: " + e);
		}
		
	}
	
	private Inventory deserializeInventory(Player player) {		
		try {
			File file = new File("plugins/abilities/thirdarm/"
				+ player.getUniqueId() + ".yml");
			FileConfiguration config = new YamlConfiguration();
			config.load(file);
			
			Inventory inv = Bukkit.createInventory(player, 9, "Third Arm");
			for(Object item : config.getList("inventory")) {
				if(item == null) continue;
				inv.addItem((ItemStack) item);
			}
			
			return inv;
		} catch (IOException | InvalidConfigurationException e) {
			player.sendMessage(ChatColor.RED 
					+ "Error while deserializing inventory: " + e);
		}
		
		return Bukkit.createInventory(player, 9, "Third Arm (Error)");
	}
}
