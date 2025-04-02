package me.jedigrandmaster.peninsulaAbilities;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CreateItemCommand implements CommandExecutor {

	private PeninsulaAbilities plugin;
	
	public CreateItemCommand(PeninsulaAbilities plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		
		if(!sender.hasPermission("peninsula.makeitem")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do this");
			return true;
		}
		
		ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
		
		meta.getPersistentDataContainer()
			.set(new NamespacedKey(this.plugin, args[0]), 
				PersistentDataType.INTEGER, Integer.parseInt(args[1])); 
		
		player.getInventory().getItemInMainHand().setItemMeta(meta);
		
		sender.sendMessage(ChatColor.GREEN + "Changed data of held item");
		
		return true;
	}
	
}
